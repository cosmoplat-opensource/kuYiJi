/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai.sql;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDailyMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成 SQL 通道（通道 C）：LLM 生成 → 过闸 → 执行 → 事实摘要 → 审计落库。
 *
 * <p>信任分级：本通道产出的结论标记为 <b>探索性</b>（{@code exploratory=true}），
 * 上层必须在答案与依据里如实标注，且当同一问题存在"权威口径"（登记指标/算子）时以权威为准。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedSqlChannel {

    private final LlmSqlGenerator generator;
    private final MicroAiDailyMapper dailyMapper;
    private final SchemaProvider schemaProvider;

    /** 通道结果 */
    @Data
    public static class Outcome {
        private boolean ok;
        /** 探索性标记：true=LLM 生成 SQL 得出的结果，未经口径校验 */
        private boolean exploratory = true;
        /** 给 LLM 润色用的事实摘要 */
        private String brief;
        /** 结构化结果（每条 SQL 一行结果摘要 + 明细行） */
        private List<Map<String, Object>> results = new ArrayList<>();
        private String purpose;
        /** 失败原因（如实告知用户用） */
        private String reason;
        private String api;
        private Map<String, Object> params = new LinkedHashMap<>();
        private long elapsedMs;
    }

    /** 多步生成的最大轮次（第 1 轮结果不足时才进第 2 轮；硬上限，避免无限取数） */
    private static final int MAX_ROUNDS = 2;

    /**
     * 尝试用生成 SQL 回答问题（**多步**：第 1 轮结果不足 → 带结果再生成一轮，最多 {@value #MAX_ROUNDS} 轮）。
     *
     * @param question   用户问题
     * @param tenantCode 租户
     * @param sessionId  会话（审计）
     * @param intentCode 识别到的指标（审计）
     */
    public Outcome run(String question, String tenantCode, String sessionId, String intentCode) {
        Outcome outcome = new Outcome();
        long t0 = System.currentTimeMillis();
        // 业务化表述：这是给用户看的"数据来源"，不暴露接口路径与技术标签
        outcome.setApi("系统只读查询");

        StringBuilder brief = new StringBuilder();
        String previousContext = null;
        String firstError = null;
        String purposeText = null;
        int totalRows = 0;
        int totalSql = 0;
        int round = 0;
        int lastGuardIndexBase = 0;

        while (round < MAX_ROUNDS) {
            round++;
            LlmSqlGenerator.GeneratedSql gen = generator.generate(question, tenantCode, previousContext);
            if (!gen.isOk()) {
                // 生成阶段失败（LLM 超时/未返回）属瞬时可恢复错误 → 第 1 轮失败时重试一次
                // （与"结果不足再生成一轮"共用同一套多步机制；闸拒绝不在此列，那是模型的语义错误，重试无益）
                if (round == 1 && gen.isLlmFailed()) {
                    previousContext = "上一轮生成调用超时/未返回内容，没有产出任何 SQL。请直接重新生成（最多 3 条 SELECT）。";
                    log.info("[AI-SQL] 第 1 轮生成失败（{}）→ 重试一次", gen.getReason());
                    continue;
                }
                if (round == 1) {
                    outcome.ok = false;
                    outcome.reason = gen.getReason();
                    outcome.elapsedMs = System.currentTimeMillis() - t0;
                    audit(question, tenantCode, sessionId, intentCode, gen, 0, null, "REJECTED", gen.getReason(), 0, outcome.elapsedMs);
                    return outcome;
                }
                // 第 2 轮生成失败：第一轮已有结果就带着用，没有才如实失败
                if (totalRows > 0) {
                    log.info("[AI-SQL] 第 2 轮生成失败（{}），沿用第 1 轮结果", gen.getReason());
                    break;
                }
                outcome.ok = false;
                outcome.reason = gen.getReason();
                outcome.elapsedMs = System.currentTimeMillis() - t0;
                audit(question, tenantCode, sessionId, intentCode, gen, 0, null, "REJECTED", gen.getReason(), 0, outcome.elapsedMs);
                return outcome;
            }
            if (gen.getSqls().isEmpty()) {
                // 模型看过上一轮结果后判定"已足够"，不再取数（省一次执行）
                log.info("[AI-SQL] 第 {} 轮：模型判定上一轮结果已足够，停止取数", round);
                if (StringUtils.hasText(gen.getPurpose())) {
                    purposeText = normalizePurpose(gen.getPurpose());
                }
                break;
            }
            if (purposeText == null) {
                purposeText = normalizePurpose(gen.getPurpose());
            }

            // 执行本轮
            int roundRows = 0;
            int roundFailed = 0;
            for (int i = 0; i < gen.getSqls().size(); i++) {
                String sql = gen.getSqls().get(i);
                long t1 = System.currentTimeMillis();
                try {
                    List<Map<String, Object>> rows = dailyMapper.execGuardedSql(sql);
                    long cost = System.currentTimeMillis() - t1;
                    int count = rows == null ? 0 : rows.size();
                    roundRows += count;
                    totalRows += count;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("round", round);
                    item.put("sqlIndex", totalSql + i + 1);
                    item.put("rows", count);
                    item.put("elapsedMs", cost);
                    item.put("data", rows == null ? new ArrayList<>() : rows);
                    outcome.results.add(item);
                    brief.append("查询").append(totalSql + i + 1)
                            .append(round > 1 ? "（第 " + round + " 轮补充）" : "")
                            .append("返回 ").append(count).append(" 行：").append(renderRows(rows)).append("\n")
                            // 全为 NULL 的聚合结果（无匹配行）：必须点明"未取到数值"，
                            // 否则润色会把它写成"该员工没有报工/无数据"这类**错误结论**
                            .append(allNull(rows)
                                    ? "※ 该查询没有匹配到任何数据（聚合值为空），**不代表业务上不存在**——"
                                    + "请如实说明未能取到该指标，不要下“没有记录/没有数据”的结论。\n"
                                    : "");
                    audit(question, tenantCode, sessionId, intentCode, gen, i, sql, "SUCCESS", null, count, cost);
                } catch (Exception e) {
                    // 表不存在/超时等确定性错误：不重试，如实告知（与 Agent 循环的"确定性错误不重试"一致）
                    String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                    firstError = msg;
                    roundFailed++;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("round", round);
                    item.put("sqlIndex", totalSql + i + 1);
                    item.put("error", msg);
                    outcome.results.add(item);
                    brief.append("查询").append(totalSql + i + 1).append("执行失败：").append(msg)
                            .append("（**这是查询失败，不是“没有数据”**：请如实说明未能取到结果，"
                                    + "禁止输出“无记录/无数据/没有报工”这类业务结论）\n");
                    audit(question, tenantCode, sessionId, intentCode, gen, i, sql, "FAILED", msg, 0,
                            System.currentTimeMillis() - t1);
                }
            }
            totalSql += gen.getSqls().size();
            lastGuardIndexBase += gen.getGuards() == null ? 0 : gen.getGuards().size();

            // 结果不足（0 行）才进第二轮：把本轮 SQL + 行数 + 结果摘要回喂，让模型补充/修正
            boolean insufficient = roundRows == 0;
            if (!insufficient || round >= MAX_ROUNDS) {
                break;
            }
            previousContext = buildRoundContext(gen, outcome, roundFailed);
            log.info("[AI-SQL] 第 {} 轮结果不足（{} 条 SQL / {} 行{}）→ 带结果再生成一轮",
                    round, gen.getSqls().size(), roundRows,
                    roundFailed == gen.getSqls().size() ? "，全部执行失败" : "，均为 0 行");
        }

        outcome.elapsedMs = System.currentTimeMillis() - t0;
        if (totalRows == 0 && firstError != null) {
            outcome.ok = false;
            outcome.reason = "生成的查询执行失败：" + firstError;
            return outcome;
        }
        outcome.ok = true;
        outcome.purpose = purposeText;
        outcome.brief = "（以下结果由系统生成的查询直接得出，属探索性结果，未经登记口径校验）\n"
                + brief.toString().trim();
        outcome.params.put("purpose", outcome.purpose);
        outcome.params.put("sqlCount", totalSql);
        outcome.params.put("rounds", round);
        outcome.params.put("rowCount", totalRows);
        log.info("[AI-SQL] 通道C 执行完成：{} 轮 / {} 条 SQL，{} 行，耗时 {}ms", round, totalSql, totalRows, outcome.elapsedMs);
        return outcome;
    }

    /**
     * 构造"上一轮结果摘要"回喂给模型（多步生成的关键输入）。
     *
     * <p>内容 = 上一轮 SQL + 行数 + 结果前几行（人话渲染，截断）。模型据此判断"是否已能回答"，
     * 从而决定"不再查询"或"补充维度/换口径/放宽条件"。
     */
    private String buildRoundContext(LlmSqlGenerator.GeneratedSql gen, Outcome outcome, int failedCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("本轮共 ").append(gen.getSqls().size()).append(" 条查询，其中执行失败 ").append(failedCount).append(" 条。\n");
        int idx = 0;
        for (Map<String, Object> item : outcome.results) {
            if (!Integer.valueOf(1).equals(item.get("round"))) {
                continue;
            }
            idx++;
            sb.append("SQL").append(idx).append("：").append(abbreviate(String.valueOf(
                    gen.getRawSqls().size() >= idx ? gen.getRawSqls().get(idx - 1) : "")));
            if (item.get("error") != null) {
                sb.append("\n  执行失败：").append(item.get("error"));
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) item.get("data");
                sb.append("\n  返回 ").append(item.get("rows")).append(" 行");
                if (data != null && !data.isEmpty()) {
                    sb.append("，示例：\n").append(renderRows(data.size() > 3 ? data.subList(0, 3) : data));
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /** 供上层判断通道是否可用 */
    public boolean enabled() {
        return schemaProvider.policy() != null && StringUtils.hasText(schemaProvider.schemaBrief());
    }

    // ------------------------------------------------------------------ 结果渲染（不依赖 LLM 也要能读懂）

    /** 列名 → 人话（LLM 生成 SQL 的别名千奇百怪，这里做一层兜底翻译；未收录的保留原名） */
    private static final Map<String, String> LABELS = new HashMap<>();

    static {
        LABELS.put("pass_rate", "良品率");
        LABELS.put("ng_rate", "不良率");
        LABELS.put("pass_num", "良品数");
        LABELS.put("ng_num", "不良数");
        LABELS.put("check_pass_num", "良品数");
        LABELS.put("check_ng_num", "不良数");
        LABELS.put("total_num", "总数");
        LABELS.put("total_cnt", "总数");
        LABELS.put("submit_cnt", "记录数");
        LABELS.put("cnt", "数量");
        LABELS.put("submit_day", "日期");
        LABELS.put("day", "日期");
        LABELS.put("process_name", "工序");
        LABELS.put("process_code", "工序编码");
        LABELS.put("product_name", "产品");
        LABELS.put("product_code", "产品编码");
        LABELS.put("nick_name", "员工");
        LABELS.put("user_name", "账号");
        LABELS.put("repair_num", "返修数");
        LABELS.put("abandoned_num", "报废数");
    }

    /** 比率类列（值按百分比展示） */
    private static final java.util.Set<String> RATE_KEYS = new java.util.HashSet<>(
            java.util.Arrays.asList("pass_rate", "ng_rate", "rate", "ratio"));

    /**
     * 把查询结果渲染成人能读的文本。
     *
     * <p>**这是探索性结果的"最低可读标准"**：即使 LLM 润色失败（超时/未配置/数字校验不过），
     * 用户看到的也必须是人话，而不是原始 JSON —— 历史问题：直接把 [{...}] 吐给用户。
     */
    /** 结果行是否"全为 null"（聚合无匹配行的典型形态）→ 用于区分"没有数据"与"查询没取到值" */
    private boolean allNull(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return false;
        }
        for (Map<String, Object> r : rows) {
            if (r == null) {
                continue;
            }
            for (Object v : r.values()) {
                if (v != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private String renderRows(List<Map<String, Object>> rows) {        if (rows == null || rows.isEmpty()) {
            return "（无数据）\n";
        }
        int limit = Math.min(rows.size(), 10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            Map<String, Object> row = rows.get(i);
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Object> e : row.entrySet()) {
                parts.add(label(e.getKey()) + " " + value(e.getKey(), e.getValue()));
            }
            sb.append("- ").append(String.join("，", parts)).append("\n");
        }
        if (rows.size() > limit) {
            sb.append("（共 ").append(rows.size()).append(" 行，仅列出前 ").append(limit).append(" 行）\n");
        }
        return sb.toString();
    }

    private String label(String key) {
        String k = key == null ? "" : key.toLowerCase();
        String label = LABELS.get(k);
        return label != null ? label : key;
    }

    private String value(String key, Object v) {
        if (v == null) {
            return "—";
        }
        if (v instanceof Number && RATE_KEYS.contains(key == null ? "" : key.toLowerCase())) {
            // 比率：统一按百分比展示（截断 1 位，与页面观感一致）
            java.math.BigDecimal rate = new java.math.BigDecimal(v.toString());
            return rate.multiply(java.math.BigDecimal.valueOf(100))
                    .setScale(1, java.math.RoundingMode.DOWN).stripTrailingZeros().toPlainString() + "%";
        }
        if (v instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) v).stripTrailingZeros().toPlainString();
        }
        return v.toString();
    }

    /** purpose 可能是字符串，也可能是数组（模型有时给多条）——统一成一句话 */
    private String normalizePurpose(String purpose) {
        if (purpose == null || purpose.trim().isEmpty()) {
            return "取数";
        }
        String p = purpose.trim();
        if (p.startsWith("[")) {
            try {
                com.alibaba.fastjson.JSONArray arr = JSON.parseArray(p);
                if (arr != null && !arr.isEmpty()) {
                    return arr.getString(0);
                }
            } catch (Exception ignore) {
                // 解析失败按普通字符串处理
            }
        }
        return p.length() > 120 ? p.substring(0, 120) + "…" : p;
    }

    // ------------------------------------------------------------------ 内部

    /** 结果样本（≤15 行，避免把明细全塞进提示词） */
    private String sample(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "（空）";
        }
        List<Map<String, Object>> part = rows.size() > 15 ? rows.subList(0, 15) : rows;
        return JSON.toJSONString(part);
    }

    private void audit(String question, String tenantCode, String sessionId, String intentCode,
                       LlmSqlGenerator.GeneratedSql gen, String sqlFinal, String execStatus, Object unused,
                       String status, String execError, int rowCount, long elapsedMs) {
        audit(question, tenantCode, sessionId, intentCode, gen, 0, sqlFinal, status, execError, rowCount, elapsedMs);
    }

    /**
     * 审计落库。
     *
     * @param guardIndex 该条 SQL 对应的闸结果下标（多语句时必须逐条对应，否则第 2 条的审计会串成第 1 条的表/列）
     */
    private void audit(String question, String tenantCode, String sessionId, String intentCode,
                       LlmSqlGenerator.GeneratedSql gen, int guardIndex, String sqlFinal,
                       String status, String execError, int rowCount, long elapsedMs) {
        try {
            // 用实体传参（不能用 Map：MybatisInterceptor 会反射注入基础字段，Map 会 NPE 刷日志）
            com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiSqlAudit audit =
                    new com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiSqlAudit();
            String rawSql = "REJECTED".equals(status) ? (gen.getRawSqls().isEmpty() ? null : String.join(";\n", gen.getRawSqls())) : sqlFinal;
            audit.setTenantCode(tenantCode);
            audit.setSessionId(sessionId);
            audit.setQuestion(cut(question, 500));
            audit.setQuestionHash(md5(question));
            audit.setIntent(intentCode);
            // purpose 现在是模型的"自报依据"（用了哪条登记关系/口径是否为自定），可能很长 →
            // 按列宽截断，避免 Data too long 导致整条审计丢失（审计记录比逐字完整更重要）
            audit.setPurpose(cut(gen.getPurpose(), 900));
            audit.setSqlText(cut(rawSql, 4000));
            audit.setSqlFinal(cut(sqlFinal, 4000));
            audit.setGuardOk(gen.isOk() ? "1" : "0");
            audit.setRejectReason(gen.isOk() ? null : abbreviate(gen.getReason()));
            // 逐条对应本语句的闸结果（多语句时不再串用第 1 条的表/列）
            com.cosmo.hhim.micro.application.service.ai.sql.SqlGuard.GuardResult guard =
                    gen.getGuards() != null && gen.getGuards().size() > guardIndex ? gen.getGuards().get(guardIndex)
                            : (gen.getGuards() == null || gen.getGuards().isEmpty() ? null : gen.getGuards().get(0));
            audit.setTablesJson(guard == null ? null : JSON.toJSONString(guard.getTables()));
            audit.setColumnsJson(guard == null ? null : abbreviate(JSON.toJSONString(guard.getColumns())));
            audit.setTenantInjected(guard != null && guard.isTenantInjected() ? "1" : "0");
            audit.setLimitRewritten(guard != null && guard.isLimitRewritten() ? "1" : "0");
            audit.setRetryCount(gen.getRetryCount());
            audit.setExecStatus(status);
            audit.setExecError(abbreviate(execError));
            audit.setRowCount(rowCount);
            audit.setElapsedMs(elapsedMs);
            audit.setModel(gen.getModel());
            audit.setCreatedBy("ai");
            dailyMapper.insertSqlAudit(audit);
        } catch (Exception e) {
            // 审计失败不影响主流程（但会告警：审计缺失本身就是问题）
            log.warn("[AI-SQL] 审计落库失败：{}", e.getMessage());
        }
    }

    private String abbreviate(String s) {
        if (s == null) {
            return null;
        }
        String t = s.replaceAll("\\s+", " ");
        return t.length() > 500 ? t.substring(0, 500) : t;
    }

    /** 按目标列宽安全截断（null 安全）：审计字段比"逐字完整"更重要的是**必须落库** */
    private String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    private String md5(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest((s == null ? "" : s).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

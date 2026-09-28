/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.application.service.ai.sql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.application.service.ai.AiLlmClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 生成 SQL（受控通道）。
 *
 * <p>流程：schema 摘要（敏感表/字段已剔除）+ 口径先验 + 用户问题 → LLM 输出 {@code {"sqls":["..."],"purpose":"..."}}
 * → 每条都过 {@link SqlGuard}（单条 SELECT、白名单、敏感列、强制租户、LIMIT 上限）
 * → 不过则把<b>拒绝原因回喂</b>给 LLM 重写（次数上限 {@code ai.sql.max-retry}，默认 1 次）
 * → 仍不过则放弃（返回失败原因，由上层如实告知用户，绝不"部分放行"）。
 *
 * <p>安全边界：本类<b>不执行</b> SQL，只负责生成与过闸；执行由只读执行器负责。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmSqlGenerator {

    private final AiLlmClient aiLlmClient;
    private final SchemaProvider schemaProvider;
    private final SchemaProvider.AiSqlProperties properties;

    /** 本体服务（提供"业务参考"：已登记对象/关系/口径）。可为空 → 退化为无参考 */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.cosmo.hhim.micro.application.service.ai.OntologyService ontologyService;

    /** 生成结果：sqls=已过闸（含改写）的 SQL；失败时 reason 说明原因 */
    @Data
    public static class GeneratedSql {
        private boolean ok;
        private List<String> sqls = new ArrayList<>();
        /** 原始 SQL（未改写，审计留痕用） */
        private List<String> rawSqls = new ArrayList<>();
        private String purpose;
        private String reason;
        private int retryCount;
        private List<SqlGuard.GuardResult> guards = new ArrayList<>();
        private String model = "chat";
        /**
         * 生成阶段失败（LLM 超时/未返回），区别于"闸拒绝"：
         * 属瞬时可恢复错误 → 上层可重试一次（与"结果不足再生成一轮"共用多步机制）。
         */
        private boolean llmFailed;
    }

    private static final String SYSTEM =
            "你是严谨的 MySQL 8 数据分析工程师。根据【可用表结构】把用户问题转成**只读查询**。\n"
            + "硬性规则：\n"
            + "1. 只允许 SELECT；禁止任何写操作（INSERT/UPDATE/DELETE/DROP/ALTER/TRUNCATE/CREATE/CALL）与多语句拼接；\n"
            + "2. 表名、列名只能来自【可用表结构】，不得臆造；不得查询 information_schema/mysql 等系统库；\n"
            + "3. 不要自己写 tenant_code 过滤条件（系统会自动注入租户隔离），但其它业务过滤条件必须写清楚；\n"
            + "3.1 **禁止使用 `?` 占位符**：本系统**不是预编译执行**，SQL 里出现 `?` 会直接语法错误、查询必然失败。"
            + "所有条件都必须写**具体值**（例如 submit_day >= '2026-08-01' AND submit_day < '2026-09-01'）；"
            + "唯一例外是租户条件——**根本不写**（系统会自动注入），也不要写成 tenant_code = ?。\n"
            + "3.2 **派生量必须在 SQL 里直接算出来**：问题要的是「差值/极差/占比/合计/倍数/排名倒数/剔掉某项后的值」时，"
            + "SQL 必须**一次性算出该结果列**并返回（例：极差 = MAX(rate) - MIN(rate)，写在同一 SELECT 里；"
            + "或先 GROUP BY 得到各项再在外层聚合），**不要只返回原值然后指望后续再算** ✗"
            + "（历史 bug：问「最高最低差多少」✗ 却只查了单个产品的良品率 → 拿不到差值 ✗）。\n"
            + "3.3 **限定条件必须落到 WHERE/GROUP BY**：问题里的限定对象（某产品/某工序/某员工）必须写成过滤条件，"
            + "要按某维度看就必须 GROUP BY 该维度；**不要把限定对象丢掉** ✗。\n"
            + "3.3.1 **没让你分组就不许分组**（实测踩到）：问题没问「哪个产品/哪道工序/哪天/每个员工」时，"
            + "**禁止**自行 `GROUP BY product_name/process_name/...` —— 那会把「全厂合计」变成一个分组的一行 ✗ "
            + "（历史 bug：问「本月返修率和不良数」✗ 却 GROUP BY 产品 → 答成「法兰盘DN20 不良数 1」✗）。"
            + "这种「整体一个数」的问题直接聚合：`SELECT SUM(x) ... WHERE ...`，不加 GROUP BY ✓\n"
            + "3.4.1 **比率类指标必须把「分子、分母、比率」一起 SELECT 出来**（实测踩到，会答错）："
            + "问「返修率/报废率」时只查 `SUM(repair_num), SUM(check_pass_num+check_ng_num)` 是不够的 ✗ "
            + "—— 结果里没有比率列，后续只能拿别的数字硬凑（实测把工单量 1190 当成了「报废率」✗）。"
            + "正确做法：同一 SELECT 里给出 `SUM(repair_num) AS repair_num, SUM(abandoned_num) AS abandoned_num, "
            + "SUM(check_pass_num + check_ng_num) AS report_total, "
            + "SUM(repair_num)/NULLIF(SUM(check_pass_num+check_ng_num),0) AS repair_rate, "
            + "SUM(abandoned_num)/NULLIF(SUM(check_pass_num+check_ng_num),0) AS abandoned_rate` ✓ "
            + "（问哪个就至少带上那个的分子/分母/比率，同类指标一并给出更省事）\n"
            + "3.4 **单位必须与问题一致**：问「差多少**个百分点**」时 —— 百分点 = 比率差 × 100 "
            + "（例：97.6% − 93.0% = 4.6 个百分点 → SQL 写 (MAX(rate) - MIN(rate)) * 100）✗ "
            + "不要把比率差 0.046 直接说成 0.046 个百分点 ✗；问「占比/倍数/比率差」时则**不要**乘 100 ✓。\n"
            + "3.5 **自检：分组维度不能与 WHERE 限定的维度撞车** ✗ —— 若 WHERE 已限定某产品"
            + "（product_name='X'），再 GROUP BY product_name 就**只剩一行** ✗ 于是"
            + "「极差/占比/排名/倍数/最高最低」这类度量**必然退化为 0** ✗（历史 bug：问"
            + "「法兰盘DN15 良品率最高最低差多少」✗ 却 GROUP BY product_name 且 WHERE 限定了该产品 → 答「相差0个百分点」✗）。"
            + "正确做法：按**问题真正要比的那个维度**分组（该例应 GROUP BY process_name ✓ → 得到各工序良品率后再取极差 ✓），"
            + "或去掉 WHERE 限定 ✗ 二者取其一 ✓ **绝不允许「限定某维度又按该维度分组」** ✗。\n"
            + "4. 口径约定：报工数据用 micro_work_submit，且只看已审（submit_status = 0，类型 submit_type = '1'）；"
            + "良品率 = sum(check_pass_num) / nullif(sum(check_pass_num + check_ng_num), 0)，用 truncate(...,3) 截断 3 位（与页面一致）；\n"
            + "4.1 **报工表上的数量列固定用 check_pass_num（良品数）与 check_ng_num（不良数）**："
            + "“报工多少/报工总量/产量”= sum(check_pass_num + check_ng_num)；"
            + "**不要**在报工表上用 pass_num/ng_num（那是库存表的在制列，会算出口径不一致的数字）；"
            + "“报工条数（记了几笔）”= count(*)，与上面的“件数”不是一回事，回答里要区分清楚；\n"
            + "4.1.1 **`COUNT(*)` 只能用于“条数/笔数/记录数”，绝不能用于“报工总数/报工量/产量”**（实测踩到）："
            + "问「本月报工总数」时若写 `COUNT(*) AS submit_count` → 会答成“报工总数 53”（=报工**条数**）✗，"
            + "而业务口径的报工件数是 **1190** ✗ —— 差 20 倍，属严重口径错 ✓。"
            + "这类词一律用 `SUM(check_pass_num + check_ng_num) AS report_total` ✓；"
            + "只有用户明确问“几笔 / 几条记录 / 报工次数”时才用 `COUNT(*)` ✓\n"
            + "4.2 **按员工筛选必须联结员工表**：micro_work_submit.submit_user 存的是**用户 ID（数字）**，"
            + "不是姓名。要按姓名查，必须 JOIN micro_user u ON u.id = ws.submit_user 且 u.tenant_code = ws.tenant_code，"
            + "然后用 u.nick_name = '张三' 过滤；**禁止**写 submit_user = '张三'（永远匹配不到，会得出"
            + "“该员工没有报工”的错误结论）；\n"
            + "5. 最多 3 条 SQL，每条解决一个明确的数据获取目的；必须写 LIMIT（不超过 500 行）；\n"
            + "5.1 **用户可能一次问了多个业务问题**（问号/顿号/以及分隔）：为**每个子问题**各出一条 SQL（最多 3 条），"
            + "以便逐条回答；不要只查第一个子问题；\n"
            + "6. 只输出 JSON：{\"sqls\":[\"select ...\"],\"purpose\":\"每条 SQL 取什么数据（一句话）\"}，不要任何解释文字。";

    /**
     * 生成并过闸。
     *
     * @param question   用户问题
     * @param tenantCode 当前租户
     */
    public GeneratedSql generate(String question, String tenantCode) {
        return generate(question, tenantCode, null);
    }

    /**
     * 生成并过闸（**多步**：previousRoundContext 非空时表示"上一轮结果不足"，带上结果再生成一轮）。
     *
     * <p>此时允许模型返回**空 sqls** 表示"上一轮结果已足够"（不再取数），避免多余查询。
     *
     * @param previousRoundContext 上一轮 SQL + 行数 + 结果摘要（null = 第一轮）
     */
    public GeneratedSql generate(String question, String tenantCode, String previousRoundContext) {
        GeneratedSql result = new GeneratedSql();
        if (!properties.isEnabled()) {
            result.reason = "生成 SQL 通道未开启（ai.sql.enabled=false）";
            return result;
        }
        String brief = schemaProvider.schemaBrief();
        if (!StringUtils.hasText(brief)) {
            result.reason = "没有可用的表结构（白名单为空或元数据读取失败）";
            return result;
        }
        SqlGuard guard = new SqlGuard(schemaProvider.policy());
        boolean followUp = StringUtils.hasText(previousRoundContext);
        // 本体"业务参考"（对象 / 已登记关系 / 已登记口径）：定位是参考而非规则 —— 优先参考，
        // 不适用时允许模型自行判断，但要求它在 purpose 里自报依据（便于审计与补本体）。
        // ⚠ 本体读取异常绝不能拖垮生成通道：这里捕获并降级为"无参考"。
        String ontology = "";
        try {
            ontology = ontologyService == null ? "" : ontologyService.referenceContext();
        } catch (Exception e) {
            log.warn("[AI-SQL] 本体参考生成失败（按无参考继续生成 SQL）: {}", e.toString());
        }
        // 【当前日期】必须告诉模型：否则它会把"8月/上个月/今天"换算成错误的年份
        // （曾出现 2026 年的问题被写成 2023-08-01 的严重错误）
        java.time.LocalDate today = java.time.LocalDate.now();
        String dateLine = "【当前日期】" + today + "（星期" + "一二三四五六日".charAt(today.getDayOfWeek().getValue() - 1)
                + "）。用户说的“今天/本月/上个月/8月/8月份”等相对时间，必须据此换算成**具体年月**，"
                + "并在 SQL 里用明确的日期字面量（如 '2026-08-01'），不要臆造年份。\n";
        String user = (StringUtils.hasText(ontology) ? ontology + "\n" : "")
                + dateLine
                + "【可用表结构】\n" + brief + "\n【用户问题】" + question
                + (followUp ? "\n\n【上一轮查询结果（供你判断是否已足够）】\n" + previousRoundContext
                + "\n这是补充轮：如果上一轮结果已能回答用户问题，请返回 sqls: []（空数组）并在 purpose 里说明原因；"
                + "如果仍然不足，再给最多 3 条 SELECT 去补充（例如换分组维度、换口径、放宽过窄的过滤条件），"
                + "并在 purpose 里说明本次补的是什么。" : "")
                + "\n\n请按规则输出 JSON（最多 3 条 SELECT）：";

        String feedback = null;
        for (int attempt = 0; attempt <= Math.max(0, properties.getMaxRetry()); attempt++) {
            String prompt = feedback == null ? user
                    : user + "\n\n【上次生成被安全闸拒绝，原因如下，请修正后重新输出 JSON】\n" + feedback;
            String reply = aiLlmClient.chat(SYSTEM, prompt);
            result.retryCount = attempt;
            if (!StringUtils.hasText(reply)) {
                result.reason = "LLM 未返回内容（未配置或调用失败）";
                // 标记为"生成阶段失败"（区别于"闸拒绝"）：这类是瞬时可恢复错误（超时/服务抖动），
                // 上层可以重试一次 —— 与"结果不足再生成一轮"共用同一套多步机制
                result.llmFailed = true;
                return result;
            }
            JSONObject json = parseJson(reply);
            if (json == null) {
                feedback = "输出不是合法 JSON：" + shorten(reply);
                result.reason = feedback;
                continue;
            }
            JSONArray sqls = json.getJSONArray("sqls");
            // 补充轮允许空数组：模型判定"上一轮结果已足够"→ 合法结束，不再取数
            if (followUp && (sqls == null || sqls.isEmpty())) {
                result.reason = null;
                result.purpose = json.getString("purpose");
                result.rawSqls = new ArrayList<>();
                result.guards = new ArrayList<>();
                result.ok = true;
                return result;
            }
            if (sqls == null || sqls.isEmpty()) {
                feedback = "JSON 里没有 sqls 数组";
                result.reason = feedback;
                continue;
            }
            List<String> candidate = new ArrayList<>();
            for (int i = 0; i < sqls.size(); i++) {
                candidate.add(sqls.getString(i));
            }
            if (candidate.size() > properties.getMaxStatements()) {
                feedback = "SQL 条数 " + candidate.size() + " 超过上限 " + properties.getMaxStatements();
                result.reason = feedback;
                continue;
            }
            // ★确定性修正：把 `?` 占位符与"自己写的租户条件"清掉。
            //   为什么不能只靠提示词：实测模型仍会写出 `WHERE tenant_code = ?`，而本通道是 ${sql} 直拼执行
            //   → MySQL 直接把 `?` 当语法错误 → 整条查询失败、白跑一轮（还浪费一次 LLM 调用）。
            //   这里先修，修不掉再让闸拒绝并回喂原因。
            candidate = sanitizePlaceholders(candidate);
            // 过闸（整批：任一条不过则整批不执行）
            List<SqlGuard.GuardResult> guards = guard.checkBatch(candidate, tenantCode);
            result.guards = guards;
            result.rawSqls = candidate;
            boolean allOk = true;
            StringBuilder reasons = new StringBuilder();
            for (SqlGuard.GuardResult g : guards) {
                if (!g.isOk()) {
                    allOk = false;
                    reasons.append("- ").append(g.getReason()).append("\n");
                }
            }
            if (!allOk) {
                feedback = reasons.toString();
                result.reason = "生成的 SQL 未通过安全闸：" + reasons.toString().trim();
                log.warn("[AI-SQL] 闸拒绝（第 {} 次）：{}", attempt + 1, feedback.replace("\n", " "));
                continue;
            }
            // 闸说"可以通过"但如果 SQL 里仍有 `?`：那是上面没修掉的形态 → 明确回喂、让模型重写
            boolean hasPlaceholder = false;
            for (String s : candidate) {
                if (s != null && s.contains("?")) {
                    hasPlaceholder = true;
                    break;
                }
            }
            if (hasPlaceholder) {
                feedback = "SQL 里仍然出现 `?` 占位符。本系统不是预编译执行，`?` 会直接语法错误。"
                        + "请把所有条件写成**具体值**（日期字面量如 '2026-08-01'），"
                        + "并且**完全不要写 tenant_code 条件**（系统会自动注入租户隔离）。";
                result.reason = feedback;
                log.warn("[AI-SQL] 第 {} 次生成仍含 `?` 占位符 → 回喂重写", attempt + 1);
                continue;
            }
            List<String> finalSqls = new ArrayList<>();
            guards.forEach(g -> finalSqls.add(g.getSql()));
            result.ok = true;
            result.sqls = finalSqls;
            result.purpose = json.getString("purpose");
            result.reason = null;
            log.info("[AI-SQL] 生成通过闸：{} 条 SQL，purpose={}", finalSqls.size(), result.purpose);
            return result;
        }
        return result;
    }

    // ------------------------------------------------------------------ 内部

    /** 租户条件（模型不该自己写）：`tenant_code = ?` / `= NULL` / `= 'NULL'`，允许带表别名 */
    private static final java.util.regex.Pattern TENANT_CONDITION = java.util.regex.Pattern.compile(
            "(?i)([a-z_][a-z0-9_]*\\.)?tenant_code\\s*=\\s*(\\?|null|'null'|''|'')");

    /**
     * `?` 占位符与"多余的租户条件"的**确定性修正**（不改业务语义）。
     *
     * <p>关键：**绝不能把租户条件改写成 `tenant_code = NULL`** —— 那会让查询恒不命中、静默返回 0 行
     * （实测踩到：「返修率/报废率」被答成"数据均未取到"，真因就是这里）。租户隔离由系统注入，
     * 模型写的租户条件必须**整条删除**。
     *
     * <p>做法：先把租户谓词整体替换成中性恒真 `1 = 1`，再收拾它带来的 `WHERE 1 = 1 AND` / `AND 1 = 1` 残渣；
     * 最后把其余位置残留的 `?` 置 NULL（保持可执行，语义上"该条件不生效"）。
     */
    private List<String> sanitizePlaceholders(List<String> sqls) {
        List<String> out = new ArrayList<>();
        for (String sql : sqls) {
            if (sql == null) {
                continue;
            }
            String s = sql;
            boolean touched = false;
            // ① 租户谓词 → 1 = 1（中性化，不做"整条删除"以免把 AND/OR 搞乱）
            String afterTenant = TENANT_CONDITION.matcher(s).replaceAll("1 = 1");
            if (!afterTenant.equals(s)) {
                s = afterTenant;
                touched = true;
            }
            // ② 收拾中性化残渣：既不改变真假值，也让 SQL 干净
            String cleaned = s.replaceAll("(?i)1\\s*=\\s*1\\s+and\\s+1\\s*=\\s*1", "1 = 1");
            cleaned = cleaned.replaceAll("(?i)\\s+and\\s+1\\s*=\\s*1(?=\\s*(and|group|order|limit|$))", " ");
            cleaned = cleaned.replaceAll("(?i)where\\s+1\\s*=\\s*1\\s+and\\s+", "where ");
            if (!cleaned.equals(s)) {
                s = cleaned;
                touched = true;
            }
            // ③ 残留的 `?` → NULL（此时已不含租户位）
            if (s.contains("?")) {
                s = s.replace("?", "NULL");
                touched = true;
            }
            if (touched) {
                log.warn("[AI-SQL] 检测到占位符/自写租户条件 → 已确定性修正：{}", s);
            }
            out.add(s);
        }
        return out;
    }

    private JSONObject parseJson(String reply) {
        String text = reply.trim();
        // 容忍 ```json 包裹与前后废话：截取第一个 { 到最后一个 }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            return JSON.parseObject(text.substring(start, end + 1));
        } catch (Exception e) {
            return null;
        }
    }

    private String shorten(String s) {
        String t = s == null ? "" : s.replaceAll("\\s+", " ");
        return t.length() > 200 ? t.substring(0, 200) + "…" : t;
    }
}

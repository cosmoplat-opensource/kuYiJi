/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.application.dto.ai.OntologyCapabilityDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 能力命中校验（唯一入口，三通道共用）。
 *
 * <p>架构定位：LLM 只负责"选能力槽位"（指标或算子），**本类负责"验"**。
 * 校验不通过不再"事后纠正"，而是直接把 mode 降级为 {@link Mode#GENERATED}（交给 LLM 生成 SQL 兜底），
 * 这样"未登记的指标/维度/实体"只有一条归宿，不需要为每种情况写补丁。
 *
 * <p>四类校验：
 * <ol>
 *   <li><b>槽位白名单</b>：capability ∈ 本体能力清单（指标 + 分析算子），否则 GENERATED；</li>
 *   <li><b>指标词一致</b>：问题里的指标词（xx率 / xx占比 / xx排名 / xx比例）必须在被选能力的
 *       名称/别名/公式/示例里出现，否则说明"选错了槽位"（如把「返修率排名」选成「记工排名」）→ GENERATED；</li>
 *   <li><b>维度白名单</b>：groupBy 必须在该能力的 dims 内，否则去掉；若维度词是问题核心（按班组/按班次…）→ GENERATED；</li>
 *   <li><b>实体字典</b>：实体是否命中字典由调用方已有的 EntityResolver 负责（未命中→披露），
 *       此处只负责"该能力是否支持该维度上的实体过滤"。</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CapabilityGate {

    private final OntologyService ontologyService;

    /** 执行模式（唯一决策输出） */
    public enum Mode {
        /** 命中登记指标 → 通道 A（登记 SQL，权威口径） */
        REGISTERED,
        /** 命中分析算子 → 通道 B（算子计算，权威可对账） */
        ANALYSIS,
        /** 未命中 → 通道 C（LLM 生成 SQL，探索性） */
        GENERATED
    }

    @Data
    public static class Decision {
        private Mode mode;
        /** 降级原因（写日志 + 供话术使用） */
        private String reason;
        /** 命中的能力编码（GENERATED 时为 null） */
        private String capability;
    }

    /** 问题里的"指标词"：xx率 / xx占比 / xx排名 / xx比例 */
    private static final Pattern METRIC_TERM =
            Pattern.compile("([\\u4e00-\\u9fa5A-Za-z0-9]{2,6}?(?:率|占比|排名|比例))");

    /**
     * 问题里的"分组短语" → 要求的维度键。
     *
     * <p>通用判定：**问题明确要求按某维度分组，而命中的能力没声明该维度 → 降级 GENERATED**。
     * 覆盖全部已知维度键（含尚未实现的 month/team/shift/device/order），所以不需要为每种新问法加词表补丁。
     * 历史 bug：「哪个月的产量最高」被答成"按员工记工排行"——月份维度没实现，却当成员工维度放行了。
     */
    private static final String[][] DIM_GROUP_PHRASES = {
            {"按产品", "product"}, {"每个产品", "product"}, {"哪个产品", "product"}, {"各产品", "product"},
            {"按工序", "process"}, {"每道工序", "process"}, {"哪道工序", "process"}, {"哪个工序", "process"},
            {"按员工", "employee"}, {"每个员工", "employee"}, {"哪个员工", "employee"}, {"谁", "employee"},
            {"按天", "day"}, {"哪天", "day"}, {"每天", "day"}, {"逐日", "day"},
            {"按月", "month"}, {"哪个月", "month"}, {"每个月", "month"}, {"各月", "month"}, {"哪一月", "month"},
            {"按订单", "order"}, {"哪个订单", "order"},
            {"按班组", "team"}, {"按班次", "shift"}, {"按设备", "device"}, {"按机台", "machine"},
            {"按不良类型", "ngType"}, {"哪类不良", "ngType"}, {"哪一类不良", "ngType"},
    };

    /**
     * 校验并给出执行模式。
     *
     * @param question 用户原问题
     * @param intent   LLM 解析结果（intent 字段承载能力编码）
     */
    public Decision decide(String question, AskIntentResult intent) {
        Decision d = new Decision();
        String capability = intent == null ? null : intent.getIntent();
        if (!StringUtils.hasText(capability) || "NOT_SUPPORTED".equals(capability)) {
            d.setMode(Mode.GENERATED);
            d.setReason("未命中任何已登记能力");
            return d;
        }
        JSONObject metric = ontologyService.metric(capability);
        if (metric == null) {
            d.setMode(Mode.GENERATED);
            d.setReason("能力 " + capability + " 不在本体清单内");
            return d;
        }
        // ① 指标词一致：**只告警、不否决**（2026-09 调整，职责重新划分）
        //
        // 理由：「这个能力能不能回答这个问题」属于**语义判断** → 由 LLM 在意图映射阶段决定；
        // 闸门的职责只是"**用法是否合法**"（能力是否已登记 / 参数与维度是否合法 / 实体是否存在）。
        // 曾经的硬否决会把 LLM 正确选出的能力误杀：
        //   例「上个月哪天良品率低？为什么？」→ LLM 正确选 ATTRIBUTION（归因算子）
        //   但词抽取得到噪声「月哪天良品率」→ 判"未被 ATTRIBUTION 覆盖" → 否决 → 转生成 SQL → 失败
        String uncovered = uncoveredMetricTerm(question, metric);
        if (uncovered != null) {
            log.warn("[AI闸门] 提示（**不否决**）：问题里的指标词「{}」未出现在能力 {}（{}）的名称/别名中；"
                            + "若最终答案不贴合，请检查意图映射的提示词与能力别名登记",
                    uncovered, capability, metric.getString("name"));
        }
        // ② 维度：问题要求的分组维度，必须是该能力声明过的维度；否则交给生成 SQL（它能按任意维度聚合）
        String unsupportedDim = unsupportedDimPhrase(question, metric);
        if (unsupportedDim != null) {
            d.setMode(Mode.GENERATED);
            d.setReason("问题要求" + unsupportedDim + "分组，但能力 " + capability + " 未声明该维度");
            return d;
        }
        // ②.5 **整体量 vs 明细清单**（实测踩到，2026-09-22）：
        //     问「这个月的返修率和报废率」「本月总不良数是多少」这类**只要一个整体数值**的问题，
        //     却被路由到"明细清单"型能力（如 NG_DETAIL 不良明细：一行一个产品/工序/不良类型）
        //     → 答案里出现"法兰盘DN20 不良数为 1"这种**某个分组的一行** ✗ 而用户要的是全厂合计 ✗。
        //     判据：问题没提任何分组/排名维度（哪/每/按/各/谁/最高/最低/排名），且该能力是明细型 → 交生成 SQL 做整体聚合。
        if (isDetailCapability(capability) && !asksForBreakdown(question)) {
            d.setMode(Mode.GENERATED);
            d.setReason("问题要的是整体数值，而能力 " + capability + " 只给明细清单（一行一个对象）→ 交生成 SQL 做整体聚合");
            return d;
        }
        // ③ 维度词冲突：问题明确提到某个业务维度（产品/工序/员工/不良类型），
        //    但被选能力既没声明该维度、也没在指标词里覆盖它 → 说明"选错了槽位"，
        //    交给生成 SQL（它能按本体登记的路径做跨实体查询）。
        //    典型：『报工最多的产品和最忙的工序是不是同一个』被错配成员工维度的记工排名。
        String conflicted = conflictedDimWord(question, metric);
        if (conflicted != null) {
            d.setMode(Mode.GENERATED);
            d.setReason("问题涉及「" + conflicted + "」维度，但能力 " + capability + " 与本维度不匹配（疑似选错槽位）");
            return d;
        }
        // ② 排序方向纠偏（**参数用法**层面，不是"能不能回答"）：
        //    问题里的方向词是硬事实（"最少/最低/最差/倒数" vs "最多/最高/第一"），
        //    而 LLM 可能被指标名带偏（例：「谁记工最少」→ 能力叫"记工排名"，它给了 desc ✗ →
        //    执行器按 desc 取第一名 = 最多 → 答案把"最多"说成"最少" ✗ 事实性错误）。
        //    这里以**问题为准**强制校准方向；LLM 没说方向时也照样补上。
        String wantedDir = directionFromQuestion(question);
        if (wantedDir != null && !wantedDir.equalsIgnoreCase(String.valueOf(intent.getOrderDir()))) {
            log.warn("[AI闸门] 排序方向纠偏：问题要「{}」→ 采用 dir={}（LLM 原值={}，能力={}）",
                    wantedDir.equals("asc") ? "最小/最低" : "最大/最高", wantedDir,
                    intent.getOrderDir(), capability);
            intent.setOrderDir(wantedDir);
            if (intent.getLimit() == null) {
                intent.setLimit(1);   // 问"最少/最高"通常只要那一个
            }
        }
        d.setCapability(capability);
        boolean analysis = "analysis".equalsIgnoreCase(metric.getString("type"))
                || IntentExecutor.isAnalysisOperator(capability);
        d.setMode(analysis ? Mode.ANALYSIS : Mode.REGISTERED);
        d.setReason(analysis ? "命中分析算子" : "命中登记指标");
        return d;
    }

    // ------------------------------------------------------------------ 内部

    /** 明细清单型能力：返回的是"一行一个对象"的清单，而不是整体数值 */
    private boolean isDetailCapability(String capability) {
        return "NG_DETAIL".equals(capability) || "ENTITY_LIST".equals(capability);   // 不良明细 / 实体清单
    }

    /**
     * 问题是否要求"按某个维度拆开看"。
     *
     * <p>**判据必须"认维度词"，不能用「分别/各/多少种」这类口语词**：
     * 「本月的返修率和不良数**分别**是多少」里的"分别"指的是"两个指标各自的值"，不是"按维度拆开"
     * —— 用口语词判定会把这个整体量问题误判成"要拆开"（实测把闸门兜底整条挡掉了 ✗）。
     */
    private boolean asksForBreakdown(String question) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        // 具体维度词：出现即"要拆开"（这些词在 CapabilityGate 的 DIM_WORDS / DIM_GROUP_PHRASES 里已有登记）
        for (String[] pair : DIM_WORDS) {
            if (question.contains(pair[0])) {
                return true;
            }
        }
        for (String[] pair : DIM_GROUP_PHRASES) {
            if (question.contains(pair[0])) {
                return true;
            }
        }
        // 排名/极值/趋势：问答的是"哪一个"，同样属于"要拆开"
        String[] rank = {"排名", "排行", "第一", "倒数", "最高", "最低", "最多", "最少", "最差", "最好", "哪天", "哪一天"};
        for (String w : rank) {
            if (question.contains(w)) {
                return true;
            }
        }
        return false;
    }

    /** 返回"问题里有、但该能力没覆盖"的指标词（null = 全部覆盖） */
    private String uncoveredMetricTerm(String question, JSONObject metric) {
        if (!StringUtils.hasText(question)) {
            return null;
        }
        StringBuilder coverage = new StringBuilder();
        coverage.append(metric.getString("name")).append(' ')
                .append(metric.getString("code")).append(' ')
                .append(metric.getString("formula"));
        JSONArray aliases = metric.getJSONArray("aliases");
        if (aliases != null) {
            coverage.append(' ').append(aliases.toJSONString());
        }
        JSONArray templates = metric.getJSONArray("questionTemplates");
        if (templates != null) {
            coverage.append(' ').append(templates.toJSONString());
        }
        String cover = coverage.toString();
        Matcher m = METRIC_TERM.matcher(question);
        while (m.find()) {
            String term = cleanTerm(m.group(1));
            if (term == null) {
                continue;
            }
            if (!cover.contains(term)) {
                return term;
            }
        }
        return null;
    }

    /**
     * 清洗抽取到的指标词。
     *
     * <p>正则会贪婪地带上前缀（实测把「各个工序的返修率」抽成「个工序的返修率」）：
     * 先截取最后一个「的」之后的部分，再去掉开头的量词/指示词，最后校验长度。
     */
    private String cleanTerm(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw;
        int idx = t.lastIndexOf('的');
        if (idx >= 0 && idx < t.length() - 1) {
            t = t.substring(idx + 1);
        }
        t = t.replaceAll("^[个每各这那哪些]+", "");
        return t.length() >= 2 ? t : null;
    }

    /**
     * 问题里出现的"该能力不支持的分组短语"（null = 全部支持）。
     *
     * @param metric 命中的能力（读它的 dims 声明）
     */
    private String unsupportedDimPhrase(String question, JSONObject metric) {
        if (!StringUtils.hasText(question)) {
            return null;
        }
        Set<String> declared = new LinkedHashSet<>();
        JSONArray dims = metric.getJSONArray("dims");
        if (dims != null) {
            dims.forEach(x -> declared.add(String.valueOf(x)));
        }
        for (String[] pair : DIM_GROUP_PHRASES) {
            if (question.contains(pair[0]) && !declared.contains(pair[1])) {
                return "按「" + pair[0].replace("按", "") + "」";
            }
        }
        return null;
    }

    /** 从问题里读"排序方向"：最少/最低/最差/倒数/最后 → asc；最多/最高/最好/第一 → desc；没提返回 null */
    private String directionFromQuestion(String question) {
        if (question == null || question.isEmpty()) {
            return null;
        }
        String[] ascWords = {"最少", "最低", "最差", "最小", "倒数", "最后", "垫底", "排最后"};
        String[] descWords = {"最多", "最高", "最好", "最大", "第一", "排第一", "最多的是谁"};
        for (String w : ascWords) {
            if (question.contains(w)) {
                return "asc";
            }
        }
        for (String w : descWords) {
            if (question.contains(w)) {
                return "desc";
            }
        }
        return null;
    }

    /** 业务维度词 → 维度枚举（用于"选错槽位"的确定性识别） */
    private static final String[][] DIM_WORDS = {
        {"产品", "product"}, {"零件", "product"}, {"物料", "product"},
        {"工序", "process"}, {"工位", "process"},
        {"员工", "employee"}, {"工人", "employee"}, {"人员", "employee"}, {"谁", "employee"},
        {"不良", "ngType"}, {"缺陷", "ngType"}, {"瑕疵", "ngType"}
    };

    /**
     * 维度词冲突：问题里出现的业务维度词，被选能力既没声明该维度、能力名称/别名里也没提到它
     * → 判定为"选错槽位"，转生成 SQL（可跨实体）。
     * <p>例：『报工最多的产品和最忙的工序是不是同一个』→ 问题含 产品+工序，
     * 而选中的"记工排名"只声明 employee → 冲突 → 交给生成 SQL（本体参考已注入，能按登记路径联结）。
     */
    private String conflictedDimWord(String question, JSONObject metric) {
        if (!StringUtils.hasText(question)) {
            return null;
        }
        Set<String> declared = new LinkedHashSet<>();
        JSONArray dims = metric.getJSONArray("dims");
        if (dims != null) {
            dims.forEach(x -> declared.add(String.valueOf(x)));
        }
        // 能力自身的名称/别名里提到的维度词，视为该能力"本来就面向这个维度"
        StringBuilder own = new StringBuilder();
        own.append(metric.getString("name") == null ? "" : metric.getString("name"));
        JSONArray aliases = metric.getJSONArray("aliases");
        if (aliases != null) {
            aliases.forEach(x -> own.append('|').append(String.valueOf(x)));
        }
        String ownText = own.toString();
        for (String[] pair : DIM_WORDS) {
            String word = pair[0];
            String dim = pair[1];
            if (question.contains(word) && !declared.contains(dim) && !ownText.contains(word)) {
                return word;
            }
        }
        return null;
    }
}

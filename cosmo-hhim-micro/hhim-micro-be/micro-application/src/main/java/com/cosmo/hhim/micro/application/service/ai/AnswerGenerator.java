/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.micro.application.dto.ai.AskExecutionResult;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 问数 · 输出生成器（生成模式）
 *
 * <p>最后一步：LLM 拿到【自包含 DataBrief】（用户问题 + 查询语义（取自本体）+ 时间/实体/维度 + 数据事实 + 口径）
 * 与一份【通用输出约束】，自行整合语言输出完整回答；
 * 数字一致性校验硬约束：生成文本中任何数字必须出现在"骨架/数据快照"集合中，否则整段回退骨架。
 *
 * <p>防线：数字安全（校验）、空数据/边界/诊断走代码（不进 LLM）、骨架兜底、
 * 未配置 LLM / 校验失败 / 超时 → 自动回退骨架。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerGenerator {

    private static final Pattern NUM = Pattern.compile("\\d+(?:\\.\\d+)?");

    /** 通用输出约束（所有指标共用；与本体解耦，只约束"怎么说"） */
    private static final String SYSTEM =
            "你是 Ku易记 经营问数助手。根据【数据摘要】用一句通顺自然的中文回答用户问题。\n"
            + "规则：\n"
            + "1. 数字只能使用【数据摘要】中出现的；不得新增、推测、计算任何数字；\n"
            + "2. 不得解释原因、不得补充摘要中没有的事实、趋势或评价；\n"
            + "3. 结论先行；语气专业克制，一句到两句；回答中应体现【数据事实】的关键数值（如良品率、超期天数、排名/数量），除非用户问题无需数值；\n"
            + "4. 直接输出回答文本，不要任何解释、标题或 Markdown 符号；\n"
            + "5. 数据为空或不足时，原样回答“暂无相关数据”；**摘要里有数据时禁止说“暂无数据/没有数据”**；\n"
            + "6. **必须是陈述句**，禁止诗、对联、顺口溜、押韵、文言、表情符号、分点罗列等任何非陈述性格式"
            + "（用户要求“写一首诗”这类创作请求由上游拦截，这里不得配合改写）；\n"
            + "7. **这是业务软件，面向车间与管理者**：只输出业务语言，禁止出现表名、字段名、列名、编码（如 GX-C）、"
            + "API 路径、SQL 片段等技术信息；涉及“工序/产品/员工”时只用它们的中文名称；\n"
            + "8. **用户一次问了多个问题时，逐个都回答**（用分号或句号分隔，顺序与提问一致）；"
            + "禁止只答第一个，也不要让用户“分开问”；\n"
            + "9. **“查询失败/没取到值”绝不能说成“没有数据”**：摘要里若出现“执行失败”“聚合值为空/未取到”等字样，"
            + "只能如实说明本次未能取到结果（可建议稍后重试），**禁止**据此下“无记录/无数据/没有报工”这类业务结论。";

    private final AiLlmClient aiLlmClient;
    private final OntologyService ontologyService;

    /** 分析类答案的输出约束：允许"讲原因"，但原因与数字都必须来自事实清单 */
    private static final String SYSTEM_ANALYSIS =
            "你是 Ku易记 经营分析助手。系统已用确定性算法算出事实清单，请据此写一段专业、克制的分析结论。\n"
            + "规则：\n"
            + "1. 数字只能使用清单中出现的，不得新增、推测、换算任何数字；\n"
            + "2. 可以把清单里的贡献度、样本量、前后对比组织成因果表述（如「主要来自攻丝工序，它从 97.6% 降到 84.7%」），"
            + "但不得引入清单之外的任何因素（设备、天气、人员情绪、物料批次等一律不许提）；\n"
            + "3. 结论先行，2~4 句；清单里说「主因不明确」或「样本量不足」时必须如实说明，不得下强结论；\n"
            + "4. 直接输出文本，不要标题、编号或 Markdown 符号；\n"
            + "5. **只输出业务语言**：禁止表名、字段名、列名、内部编码、API 路径、SQL 片段；对象一律用中文名称。";

    /**
     * 分析类答案润色：输入是确定性事实清单（骨架），输出是通顺结论。
     *
     * <p>与 {@link #generate} 的差别只在提示词：分析场景**允许**讲原因，但"允许的数字集合"= 事实清单里的数字，
     * 一旦出现清单外的数字整段回退清单原文 —— 所以润色再自由也不会编数字。
     */
    public String polishAnalysis(String question, String skeleton) {
        return assemble(question, skeleton, SYSTEM_ANALYSIS, "AI分析").text;
    }

    /** 出口统一结果：文本 + 是否回退到骨架（true=未经润色） */
    public static class Assembled {
        public final String text;
        public final boolean fallback;

        Assembled(String text, boolean fallback) {
            this.text = text;
            this.fallback = fallback;
        }
    }

    /**
     * **唯一出口**：骨架 + 提示词 → 润色文本。
     *
     * <p>三个公开入口（登记 {@link #generate} / 分析 {@link #polishAnalysis} / 探索 {@link #polishExploratory}）
     * 都走这里，差别**只在提示词**；数字护栏、异常兜底、未启用兜底只有一份实现。
     * 历史问题：三份实现各自演进，规则会漂移（"禁止创作格式"曾只加在登记通道那条提示词上）。
     *
     * @param tag 日志标签（AI润色 / AI分析 / AI探索）
     */
    private Assembled assemble(String question, String skeleton, String systemPrompt, String tag) {
        if (!enabled || !StringUtils.hasText(skeleton)) {
            return new Assembled(skeleton, true);
        }
        try {
            Set<Double> allowed = new HashSet<>();
            collect(skeleton, allowed);
            if (allowed.isEmpty()) {
                return new Assembled(skeleton, true);
            }
            String user = "【用户问题】" + question + "\n\n【数据摘要】\n" + skeleton + "\n\n请按上述规则输出回答：";
            String reply = aiLlmClient.chat(systemPrompt, user);
            if (reply == null || !StringUtils.hasText(reply.trim())) {
                return new Assembled(skeleton, true);
            }
            if (!numbersAllAllowed(reply, allowed)) {
                log.info("[{}] 数字校验未通过（出现摘要外的数字），回退骨架", tag);
                return new Assembled(skeleton, true);
            }
            return new Assembled(reply.trim(), false);
        } catch (Exception e) {
            log.warn("[{}] 润色异常，回退骨架: {}", tag, e.getMessage());
            return new Assembled(skeleton, true);
        }
    }

    @Value("${ai.refine-enabled:true}")
    private boolean enabled;

    /**
     * 探索性结果润色（通道 C：LLM 生成 SQL 得出的数据）。
     *
     * <p>与 {@link #generate} 相同的数字护栏，但提示词明确要求"如实汇总查询结果、不做原因推断"，
     * 且必须体现"探索性、建议核对"的语气 —— 因为这部分数据未经登记口径校验。
     */
    public String polishExploratory(String question, String skeleton) {
        return assemble(question, skeleton, SYSTEM_EXPLORATORY, "AI-SQL 探索").text;
    }

    /** 探索性结果的输出约束：如实汇总、不做因果推断（数据未经登记口径校验） */
    private static final String SYSTEM_EXPLORATORY =
            "你是 Ku易记 数据助手。下面是系统生成的只读查询返回的结果，请据此如实回答用户问题。\n"
            + "规则：\n"
            + "1. 数字只能使用结果中出现的，不得新增、推测、换算；\n"
            + "2. 只做汇总与描述，不要推断原因、不要给建议；\n"
            + "3. 结论先行，1~3 句；直接输出文本，不要标题与 Markdown 符号；\n"
            + "4. **必须是陈述句**，禁止诗、对联、顺口溜、押韵、文言、表情符号、分点罗列等任何非陈述性格式；\n"
            + "5. **只输出业务语言**：禁止表名、字段名、列名、内部编码、API 路径、SQL 片段；"
            + "也不要提“生成的 SQL / 探索性查询”这类实现细节，只讲业务结论；\n"
            + "6. **用户一次问了多个问题时，逐个都回答**（用分号或句号分隔，顺序与提问一致）；"
            + "禁止只答第一个，也不要让用户“分开问”；\n"
            + "7. **查询失败/没取到值 ≠ 没有数据**：摘要里出现“执行失败”“聚合值为空/未取到”时，"
            + "只能如实说明未能取到结果，**禁止**输出“无记录/无数据/没有报工”这类业务结论。";

    /**
     * 生成最终回答；任何异常/未配置/校验失败均回退骨架
     *
     * @param skeleton 骨架答案（兜底 + 数字集合来源之一）
     */
    public String generate(String question, AskIntentResult intent, AskExecutionResult exec, String skeleton) {
        if (!enabled || !StringUtils.hasText(skeleton)) {
            return skeleton;
        }
        try {
            String brief = buildDataBrief(question, intent, exec);
            Set<Double> allowed = buildAllowedNumbers(skeleton, exec, intent);
            if (allowed.isEmpty()) {
                return skeleton;
            }
            String user = brief + "\n\n请按上述规则生成回答：";
            String reply = aiLlmClient.chat(SYSTEM, user);
            if (reply == null) {
                return skeleton;
            }
            if (!numbersAllAllowed(reply, allowed)) {
                log.info("[AI生成] 数字校验未通过，回退骨架答案");
                return skeleton;
            }
            // 防御：数据存在时模型误输出"暂无"话术 → 回退骨架
            if (reply.contains("暂无") && !skeleton.contains("暂无")) {
                log.info("[AI生成] 数据存在却输出'暂无'，回退骨架答案");
                return skeleton;
            }
            String t = reply.trim();
            return t.isEmpty() ? skeleton : t;
        } catch (Exception e) {
            log.warn("[AI生成] 异常（回退骨架）: {}", e.getMessage());
            return skeleton;
        }
    }

    /* ---------------- DataBrief（自包含） ---------------- */

    private String buildDataBrief(String question, AskIntentResult intent, AskExecutionResult exec) {
        StringBuilder sb = new StringBuilder();
        sb.append("【用户问题】").append(question).append("\n");

        // 查询语义：取自本体（name + formula），LLM 由此知道"在答什么"
        String capability = "";
        try {
            com.alibaba.fastjson.JSONObject m = ontologyService.metric(intent.getIntent());
            if (m != null) {
                capability = m.getString("name") + "：" + m.getString("formula");
                if ("mode".equals(intent.getMode())) {
                    // 当前仅 DELIVERY_RISK 使用 mode；mode 语义并入描述
                    capability += "（模式：" + ("soon".equals(intent.getMode()) ? "近期交付" : "延期预警") + "）";
                }
            }
        } catch (Exception ignore) {
        }
        sb.append("【查询语义】").append(StringUtils.hasText(capability) ? capability : intent.getIntent()).append("\n");

        sb.append("【时间范围】");
        if (StringUtils.hasText(intent.getStartDate()) && StringUtils.hasText(intent.getEndDate())) {
            sb.append(intent.getStartDate()).append(" ~ ").append(intent.getEndDate());
        } else {
            sb.append("当前时点");
        }
        sb.append("\n");

        if (intent.getEntities() != null && !intent.getEntities().isEmpty()) {
            sb.append("【查询实体】").append(String.join("、", intent.getEntities().values())).append("\n");
        }
        if (StringUtils.hasText(intent.getGroupBy())) {
            sb.append("【分组维度】").append(intent.getGroupBy()).append("\n");
        }

        sb.append("【数据事实】");
        List<Map<String, Object>> rows = exec == null ? null : exec.getRows();
        if (rows == null || rows.isEmpty()) {
            sb.append("空（无数据）");
        } else {
            sb.append("\n");
            int n = Math.min(rows.size(), 15);
            for (int i = 0; i < n; i++) {
                sb.append("- ").append(briefRow(rows.get(i))).append("\n");
            }
        }

        sb.append("【口径说明】");
        if ("submission".equals(intent.getStatScope())) {
            sb.append("报工汇总（含未审核，全量口径）");
        } else if (exec != null && StringUtils.hasText(exec.getApi())) {
            sb.append(exec.getApi());
        } else {
            sb.append("登记实现");
        }
        sb.append("\n");
        return sb.toString();
    }

    /** 行摘要：名称（字符串字段）在前，数字字段在后（格式化可读） */
    private String briefRow(Map<String, Object> row) {
        StringBuilder sb = new StringBuilder();
        String label = "";
        StringBuilder nums = new StringBuilder();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            Object v = e.getValue();
            if (v == null) {
                continue;
            }
            if (label.isEmpty() && v instanceof String) {
                label = v.toString();
                continue;
            }
            if (v instanceof Number) {
                if (nums.length() > 0) {
                    nums.append("，");
                }
                nums.append(friendly(e.getKey())).append(" ").append(fmtNum(v));
            }
        }
        if (StringUtils.hasText(label)) {
            sb.append(label);
        }
        if (nums.length() > 0) {
            if (sb.length() > 0) {
                sb.append("：");
            }
            sb.append(nums);
        }
        return sb.toString();
    }

    private String friendly(String key) {
        switch (key) {
            case "passRate":
                return "良品率";
            case "passNum":
                return "良品";
            case "ngNum":
                return "不良";
            case "totalNum":
                return "总数";
            case "riskDays":
                return "超期天数";
            default:
                return key;
        }
    }

    private String fmtNum(Object v) {
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            // 比率格式化：0 < d < 1 → 百分比（不再限定 BigDecimal；否则 LLM 会把 0.952 当裸小数念出来）
            if (d > 0 && d < 1) {
                return new BigDecimal(String.valueOf(d)).movePointRight(2)
                        .setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "%";
            }
            return String.valueOf(d);
        }
        // 字符串型数字（如 SQL truncate 返回的 "0.952"）：同样按比率展示
        try {
            BigDecimal bd = new BigDecimal(v.toString());
            if (bd.signum() > 0 && bd.compareTo(BigDecimal.ONE) < 0) {
                return bd.movePointRight(2).setScale(1, java.math.RoundingMode.HALF_UP)
                        .stripTrailingZeros().toPlainString() + "%";
            }
        } catch (Exception ignore) {
            // 非数字字符串：原样返回
        }
        return v.toString();
    }

    /* ---------------- 数字校验（硬约束） ---------------- */

    private Set<Double> buildAllowedNumbers(String skeleton, AskExecutionResult exec, AskIntentResult intent) {
        Set<Double> set = new HashSet<>();
        collect(skeleton, set);
        if (intent != null) {
            // 时间范围里的日期数字合法：DataBrief 已把"时间范围"告诉 LLM，答案里复述 2026年9月 不算编数字
            collect(intent.getStartDate(), set);
            collect(intent.getEndDate(), set);
            // 实体名里的数字合法：如"法兰盘DN15"，LLM 复述实体名不该被判非法（历史：DN15 的 15 触发整段回退）
            if (intent.getEntities() != null) {
                for (String v : intent.getEntities().values()) {
                    collect(v, set);
                }
            }
        }
        if (exec != null && exec.getRows() != null) {
            for (Map<String, Object> row : exec.getRows()) {
                if (row == null) {
                    continue;
                }
                for (Object v : row.values()) {
                    if (v instanceof Number) {
                        set.add(((Number) v).doubleValue());
                    } else if (v != null) {
                        collect(v.toString(), set);
                    }
                }
            }
        }
        return set;
    }

    private boolean numbersAllAllowed(String reply, Set<Double> allowed) {
        Matcher m = NUM.matcher(reply);
        while (m.find()) {
            // 年份/日期（2026、2026-09、2026-09-15）不是"数据数字"，不参与校验
            // （历史问题：润色提到"2026年9月"被判非法 → 整段润色被丢弃、回退骨架）
            if (isDateToken(reply, m.start(), m.end())) {
                continue;
            }
            double d;
            try {
                d = Double.parseDouble(m.group());
            } catch (Exception e) {
                continue;
            }
            boolean ok = false;
            for (double a : allowed) {
                if (Math.abs(a - d) < 0.001) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                log.info("[AI生成] 发现非法数字: {}（不在数据快照内）", m.group());
                return false;
            }
        }
        return true;
    }

    /** 命中位置构成日期/年份：后接 MM月/日/号、-MM(-DD)、前接 YYYY-/MM-、或裸 19xx/20xx 四位年份 */
    private boolean isDateToken(String text, int start, int end) {
        String token = text.substring(start, end);
        String rest = end < text.length() ? text.substring(end) : "";
        // 「8月」「8月份」「8号」「8日」「9点半」等中文时间表达 → 不是"编造的数字"
        if (rest.matches("^(月|月份|号|日|点钟|点半|点).*")) {
            return true;
        }
        if (rest.matches("^-\\d{1,2}(-\\d{1,2})?.*")) {
            return true;
        }
        if (start > 0) {
            String pre = text.substring(Math.max(0, start - 5), start);
            if (pre.matches(".*\\d{4}-$") || pre.matches(".*\\d{1,2}-$")) {
                return true;
            }
        }
        return token.length() == 4 && (token.startsWith("19") || token.startsWith("20"));
    }

    private void collect(String text, Set<Double> set) {
        if (text == null) {
            return;
        }
        Matcher m = NUM.matcher(text);
        while (m.find()) {
            try {
                set.add(new BigDecimal(m.group()).doubleValue());
            } catch (Exception ignore) {
            }
        }
    }
}

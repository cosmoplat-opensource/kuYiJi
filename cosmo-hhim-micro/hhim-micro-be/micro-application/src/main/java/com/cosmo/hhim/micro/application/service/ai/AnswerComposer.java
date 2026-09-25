/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.application.dto.ai.AskExecutionResult;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.application.dto.ai.AskReplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 答案合成器
 *
 * <p>执行结果 → 模板答案 + route（跳转）+ evidence（三级血缘）。
 * 数字只来自执行结果（rows），不经过任何模型；P1 可加 LLM 润色（数字后校验）。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnswerComposer {

    private final OntologyService ontologyService;

    private static final SimpleDateFormat TS = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /** 组装 /ai/ask 回复（含答案/跳转/证据） */
    public AskReplyVO compose(AskIntentResult intent, AskExecutionResult exec) {
        AskReplyVO vo = new AskReplyVO();
        vo.setIntent(intent.getIntent());
        vo.setFallback(!"LLM".equals(intent.getSource()));
        vo.setSource(intent.getSource());
        // 空数据：route 不跳（无对应图表页/无数据可看）；evidence 保留口径与来源
        vo.setAnswer(render(intent.getIntent(), intent, exec));
        vo.setRoute(exec == null || exec.getRows() == null || exec.getRows().isEmpty()
                ? null : buildRoute(intent.getIntent(), intent));
        vo.setEvidence(buildEvidence(intent.getIntent(), exec));
        return vo;
    }

    /* ---------------- 模板答案 ---------------- */

    private String render(String code, AskIntentResult intent, AskExecutionResult exec) {
        List<Map<String, Object>> rows = exec.getRows();
        // 组合式：SUMMARY × day（"哪天产量最高"类）
        if ("SUMMARY".equals(code) && "day".equals(intent.getGroupBy())) {
            return renderDaily(intent, rows);
        }
        // 组合式：SUMMARY × {product|employee|process}（"哪个产品/员工/工序产量最高"类）
        if ("SUMMARY".equals(code) && StringUtils.hasText(intent.getGroupBy())) {
            return renderSummaryRank(intent, rows);
        }
        // 组合式：良品率 × day（"哪天良品率最低"类）
        if (("PRODUCT_PASS_RATE".equals(code) || "PROCESS_PASS_RATE".equals(code) || "EMPLOYEE_PASS_RATE".equals(code))
                && "day".equals(intent.getGroupBy())) {
            return renderPassRateByDay(intent, rows);
        }
        switch (code) {
            case "ENTITY_LIST":
                return renderEntityList(intent, rows);
            case "DELIVERY_RISK":
                if ("soon".equals(intent.getMode())) {
                    return "1".equals(exec.getParams().get("capZero")) ? "近 30 天暂无产能数据，无法预测交付情况。"
                            : renderDeliverySoon(rows);
                }
                return "1".equals(exec.getParams().get("capZero")) ? "近 30 天暂无产能数据，无法预测是否延期。"
                        : renderDeliveryRisk(rows);
            case "PRODUCT_PASS_RATE":
            case "PROCESS_PASS_RATE":
            case "EMPLOYEE_PASS_RATE":
                return renderPassRate(code, intent, rows);
            case "SUBMIT_RANK":
                return renderRank(intent, rows, "submitNum");
            case "NG_DETAIL":
                return renderNg(rows);
            case "SUMMARY":
                return renderSummary(rows);
            case "STOCK":
                return "product".equals(intent.getGroupBy()) ? renderStockByProduct(rows) : renderStock(rows);
            default:
                return "该指标暂未支持回答合成。";
        }
    }

    private String renderPassRate(String code, AskIntentResult intent, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        // 单实体（"张三这个月良品率多少"/"法兰盘良品率多少"）：直接给该实体的口径数字，
        // 不套"最低/最高"话术（实体已下推成过滤条件，只有一行）
        if (rows.size() == 1) {
            Map<String, Object> row = rows.get(0);
            String name = strOf(row, "productName", "processName", "nickName");
            return "「" + name + "」良品率 " + rate(row.get("passRate"))
                    + "（良品 " + num(row.get("checkPassNum")) + " 件，不良 " + num(row.get("checkNgNum")) + " 件）。";
        }
        Map<String, Object> top = rows.get(0);
        String name = strOf(top, "productName", "processName", "nickName");
        String desc = "asc".equals(intent.getOrderDir()) ? "最低" : "最高";
        StringBuilder sb = new StringBuilder();
        sb.append("良品率").append(desc).append("的是「").append(name).append("」（").append(rate(top.get("passRate"))).append("）");
        if (rows.size() > 1) {
            List<String> more = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                more.add(strOf(rows.get(i), "productName", "processName", "nickName")
                        + " " + rate(rows.get(i).get("passRate")));
            }
            sb.append("；其次：").append(String.join("，", more));
        }
        sb.append("。");
        return sb.toString();
    }

    private String renderRank(AskIntentResult intent, List<Map<String, Object>> rows, String numKey) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            items.add(strOf(row, "nickName") + " " + num(row.get(numKey)) + " 件");
        }
        return "记工排名（" + ("desc".equals(intent.getOrderDir()) ? "从高到低" : "从低到高") + "）：" + String.join("，", items) + "。";
    }

    private String renderNg(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            items.add(strOf(row, "productName") + " " + strOf(row, "processName") + " 不良 " + strOf(row, "ngNum"));
        }
        return "不良明细前 " + rows.size() + " 条：" + String.join("；", items) + "。";
    }

    /** 近期交付渲染（DELIVERY_RISK × soon）：未延期且 7 天内交付的订单 */
    private String renderDeliverySoon(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "近期 7 天内没有待交付的订单。";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            items.add(strOf(row, "orderNo") + "（" + strOf(row, "productName") + "）"
                    + " 交期 " + strOf(row, "deliveryDate")
                    + "，预计完成 " + strOf(row, "predictDate") + "（按期）");
        }
        return "近期交付订单（" + rows.size() + " 个）：" + String.join("；", items) + "。";
    }

    /** 订单延期预警渲染（DELIVERY_RISK）：延期清单 | 无延期提示 */
    private String renderDeliveryRisk(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "近期没有预计延期的订单。";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            items.add(strOf(row, "orderNo") + "（" + strOf(row, "productName") + "）"
                    + " 交期 " + strOf(row, "deliveryDate")
                    + "，预计完成 " + strOf(row, "predictDate")
                    + "，超期 " + num(row.get("riskDays")) + " 天");
        }
        return "延期风险订单（" + rows.size() + " 个）：" + String.join("；", items) + "。";
    }

    /** 实体清单渲染（ENTITY_LIST）："目前有 N 个产品：A、B、C。" */
    private String renderEntityList(AskIntentResult intent, List<Map<String, Object>> rows) {        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        String typeName = "产品";
        if ("process".equals(intent.getEntities().get("entityType"))) {
            typeName = "工序";
        } else if ("employee".equals(intent.getEntities().get("entityType"))) {
            typeName = "员工";
        }
        List<String> names = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            names.add(strOf(row, "name", "productName", "processName", "nickName"));
        }
        return "目前有 " + rows.size() + " 个" + typeName + "：" + String.join("、", names) + "。";
    }

    /** 按天聚合渲染（SUMMARY × day；rows 已按 totalNum 排序） */
    private String renderDaily(AskIntentResult intent, List<Map<String, Object>> rows) {        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String day = strOf(row, "submitDay");
            items.add((day.length() >= 5 ? day.substring(5) : day) + "（良品 " + num(row.get("passNum"))
                    + "，不良 " + num(row.get("ngNum")) + "）");
        }
        if ("desc".equals(intent.getOrderDir())) {
            return "按天产量从高到低：" + String.join("、", items) + "。";
        }
        return "按天产量从低到高：" + String.join("、", items) + "。";
    }

    private String renderSummary(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        Map<String, Object> d = rows.get(0);
        return "记工总数 " + num(d.get("totalCounts"))
                + "，良品数 " + num(d.get("totalPassNum"))
                + "，不良数 " + num(d.get("totalNgNum"))
                + "，良品率 " + rate(d.get("passRate")) + "。";
    }

    /**
     * 产量排行渲染（SUMMARY × {product|employee|process}）
     *
     * <p>口径写明"已审记工总数"（=良品+不良），同时给出良品数，避免与"产量=良品数"混淆。
     */
    private String renderSummaryRank(AskIntentResult intent, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        String dim = "product".equals(intent.getGroupBy()) ? "产品"
                : "process".equals(intent.getGroupBy()) ? "工序" : "员工";
        String dir = "asc".equals(intent.getOrderDir()) ? "从低到高" : "从高到低";
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String name = strOf(row, "productName", "processName", "nickName");
            items.add(name + " " + num(row.get("totalNum")) + " 件（良品 " + num(row.get("checkPassNum")) + "）");
        }
        return "按" + dim + "产量（已审记工总数）" + dir + "：" + String.join("，", items) + "。";
    }

    /** 按天良品率渲染（良品率 × day）："良品率最低的是 08-12（95.6%）；其次…" */
    private String renderPassRateByDay(AskIntentResult intent, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        String desc = "asc".equals(intent.getOrderDir()) ? "最低" : "最高";
        // 只有一天有已审数据时不要断言"最低/最高"（那是过度解读：样本只有 1 天）
        if (rows.size() == 1) {
            String day = strOf(rows.get(0), "submitDay");
            return "统计范围内只有 " + (day.length() >= 5 ? day.substring(5) : day) + " 一天有已审数据，良品率 "
                    + rate(rows.get(0).get("passRate")) + "。";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String day = strOf(row, "submitDay");
            items.add((day.length() >= 5 ? day.substring(5) : day) + "（" + rate(row.get("passRate")) + "）");
        }
        // 明确"这是排名前 N 天"而不是"这些就是全部"：
        // 历史问题：骨架写"按天良品率最低：A、B、C"，润色后变成"本月记录的良品率分别为 A/B/C"，
        // 读者会以为该月只有 3 天有数据（实际有 20+ 天）。
        return "按天良品率" + desc + "的**前 " + items.size() + " 天**（仅列前 " + items.size() + " 天，不代表全部日期）："
                + String.join("、", items) + "。";
    }

    /**
     * 产品库存排行渲染（STOCK × product）。
     *
     * <p>行结构由执行器按问题措辞决定：可能只含成品库存（finished）、只含在制品（wip），或两者都有。
     * 这里按实际存在的键分别渲染并**标注口径**，避免"问在制、答成品"这种错配。
     * 库存是**时点快照**，与用户所选时间区间无关，必须注明。
     */
    private String renderStockByProduct(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无可统计的库存数据。";
        }
        Map<String, Object> d = rows.get(0);
        List<String> parts = new ArrayList<>();
        if (d.get("wip") instanceof List) {
            parts.add(renderStockItems((List<Map<String, Object>>) d.get("wip"), "wipNum", "工序在制品"));
        }
        if (d.get("finished") instanceof List) {
            parts.add(renderStockItems((List<Map<String, Object>>) d.get("finished"), "finishedNum", "成品库存"));
        }
        if (parts.isEmpty()) {
            // 兼容旧结构（直接是产品行）
            return renderStockItems(rows, "finishedNum", "成品库存");
        }
        return String.join("；", parts) + "。";
    }

    private String renderStockItems(List<Map<String, Object>> list, String numKey, String label) {
        if (list == null || list.isEmpty()) {
            return label + "暂无可统计的数量";
        }
        List<String> items = new ArrayList<>();
        for (Map<String, Object> row : list) {
            items.add(strOf(row, "productName") + " " + num(row.get(numKey)) + " 件");
        }
        return label + "（从高到低）" + String.join("、", items);
    }

    private String renderStock(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "暂无相关数据。";
        }
        Map<String, Object> d = rows.get(0);
        // 库存为"时点量"（不分期间），与完工数/报工数口径不同，文案注明；
        // 泛问"库存还有多少"时执行器会一并给出在制品合计（wipTotal），这里一并说明，避免只答成品
        String base = "成品库存共 " + num(d.get("finishedNum")) + " 件，涉及 " + num(d.get("productCnt")) + " 个产品";
        if (d.get("wipTotal") != null) {
            base += "；工序在制品合计 " + num(d.get("wipTotal")) + " 件，涉及 " + num(d.get("wipProductCnt")) + " 个产品";
        }
        return base + "。";
    }

    /* ---------------- route / evidence ---------------- */

    private Map<String, Object> buildRoute(String code, AskIntentResult intent) {
        Map<String, Object> route = new LinkedHashMap<>();
        Map<String, String> params = new LinkedHashMap<>();
        String path = null;
        if ("SUMMARY".equals(code)) {
            // 汇总 = 质量趋势页（顶部统计同源）；按天组合（SUMMARY×day）暂无对应图表页 → 不跳
            if ("day".equals(intent.getGroupBy())) {
                return null;
            }
            path = "/pages-analysis/quality/index";
            params.put("startDate", intent.getStartDate() != null ? intent.getStartDate() : "");
            params.put("endDate", intent.getEndDate() != null ? intent.getEndDate() : "");
        } else if ("PRODUCT_PASS_RATE".equals(code)) {
            // 产品维度 → 质量趋势页（按产品过滤）
            path = "/pages-analysis/quality/index";
            String entity = intent.getEntities().get("productNameOrCode");
            if (StringUtils.hasText(entity)) {
                params.put("productNameOrCode", entity);
            }
            params.put("startDate", intent.getStartDate() != null ? intent.getStartDate() : "");
            params.put("endDate", intent.getEndDate() != null ? intent.getEndDate() : "");
        } else if ("PROCESS_PASS_RATE".equals(code) || "EMPLOYEE_PASS_RATE".equals(code)) {
            // 工序/员工维度 → 良品率分析页（该页有 产品/工序/员工 三个 tab），按维度打开
            // 历史 bug：原来统一跳质量趋势页并把工序名/员工名塞进 productNameOrCode，图表页按"产品"过滤（空/错位）
            path = "/pages-analysis/rate-analysis/index";
            params.put("dimension", "PROCESS_PASS_RATE".equals(code) ? "process" : "employee");
            params.put("startDate", intent.getStartDate() != null ? intent.getStartDate() : "");
            params.put("endDate", intent.getEndDate() != null ? intent.getEndDate() : "");
        } else if ("SUBMIT_RANK".equals(code)) {
            path = "/pages-analysis/work-rank";
            params.put("startDate", intent.getStartDate() != null ? intent.getStartDate() : "");
            params.put("endDate", intent.getEndDate() != null ? intent.getEndDate() : "");
        } else if ("NG_DETAIL".equals(code)) {
            path = "/pages-analysis/quality/ng-pass-list";
            params.put("startDate", intent.getStartDate() != null ? intent.getStartDate() : "");
            params.put("endDate", intent.getEndDate() != null ? intent.getEndDate() : "");
        } else {
            return null;
        }
        route.put("path", path);
        if (!params.isEmpty()) {
            route.put("params", params);
        }
        return route;
    }

    private Map<String, Object> buildEvidence(String code, AskExecutionResult exec) {
        JSONObject metric = ontologyService.metric(code);
        Map<String, Object> evidence = new LinkedHashMap<>();
        if (metric != null) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", metric.getString("name"));
            m.put("formula", metric.getString("formula"));
            evidence.put("metric", m);
        }
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("api", exec.getApi());
        source.put("params", exec.getParams());
        source.put("note", "与本页统计同源（登记于 micro_ai_metric_api）");
        evidence.put("source", source);

        List<Map<String, Object>> snapshot = new ArrayList<>();
        for (Map<String, Object> row : exec.getRows()) {
            // 前端约定 snapshot 项为 {label, value}：label=首个字符串字段（日期/名称），value=数值摘要
            Map<String, Object> item = new HashMap<>();
            String label = "";
            StringBuilder val = new StringBuilder();
            for (Map.Entry<String, Object> e : row.entrySet()) {
                Object v = e.getValue();
                if (v == null) {
                    continue;
                }
                if (label.isEmpty() && v instanceof String) {
                    label = v.toString();
                } else if (v instanceof Number) {
                    if (val.length() > 0) {
                        val.append(" · ");
                    }
                    val.append(e.getKey()).append(" ").append(num(v));
                }
            }
            item.put("label", label);
            item.put("value", val.toString());
            snapshot.add(item);
        }
        evidence.put("snapshot", snapshot);
        evidence.put("asOf", TS.format(new Date()));
        return evidence;
    }

    private String strOf(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) {
                return v.toString();
            }
        }
        return "-";
    }

    /** 数字显示：去尾零（31.0000 → 31；0.913 → 0.913） */
    private String num(Object v) {
        if (v == null) {
            return "-";
        }
        try {
            double d = Double.parseDouble(v.toString());
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return v.toString();
        }
    }

    /**
     * 比率显示：0.956 → 95.6%
     *
     * <p><b>取舍必须与页面完全一致</b>：既有接口（MicroAnalysisServiceImpl）统一用
     * {@code BigDecimal.divide(..., 3, RoundingMode.DOWN)}（**截断** 3 位），页面用 bignumber.js
     * {@code times(100)} 原样打印。因此这里也"先截断 3 位、再 ×100"，不做二次四舍五入——
     * 否则会出现"问数 95.7% / 页面 95.6%"（88/92 的场景）这种 0.1 的口径差。
     */
    private String rate(Object v) {
        if (v == null) {
            return "-";
        }
        try {
            java.math.BigDecimal d = new java.math.BigDecimal(v.toString())
                    .setScale(3, java.math.RoundingMode.DOWN);
            return d.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
        } catch (Exception e) {
            return v + "%";
        }
    }
}

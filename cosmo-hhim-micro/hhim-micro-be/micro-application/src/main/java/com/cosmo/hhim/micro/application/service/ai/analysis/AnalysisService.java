/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai.analysis;

import com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 分析服务：把"取数（登记聚合 SQL）→ 纯算法归因（MiniAnalyst）→ 事实清单"串起来。
 *
 * <p>职责边界：
 * <ul>
 *   <li><b>只做确定性的事</b>：取数走已审口径的登记 SQL，计算走 MiniAnalyst，本类不做任何"猜测"；</li>
 *   <li><b>不生成话术</b>：输出的是事实清单（facts），措辞交给上层（AnswerGenerator）+ 数字一致性校验；</li>
 *   <li><b>下钻</b>就是"带上父维度过滤再跑一次对比"，因此天然可递归，且每层结论都能对账（Σ贡献=Δ）。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final MicroAiDailyMapper dailyMapper;

    /** 分析结果：结构化结果 + 事实清单 + 溯源信息 */
    public static class AnalysisOutcome {
        /** 事实清单（喂 LLM 的唯一数字来源） */
        public String brief;
        /** 两期对比结构化结果（图表/表格用） */
        public MiniAnalyst.CompareResult compare;
        /** 归一化后的明细行（前端画柱状/表格） */
        public List<Map<String, Object>> rows = new ArrayList<>();
        /** 溯源：登记 SQL 标识 + 参数 */
        public String api;
        public Map<String, Object> params = new HashMap<>();
        /** 数据不足或维度无数据时的说明（非空时上层应如实转述，不要编） */
        public String notice;
    }

    /**
     * 两期对比 + 贡献度归因。
     *
     * @param dim            分组维度：process / product / employee / day
     * @param periodALabel   基期标签（如 "2026-08"）
     * @param periodAStart   基期起（yyyy-MM-dd）
     * @param periodAEnd     基期止
     * @param periodBLabel   本期标签
     * @param periodBStart   本期起
     * @param periodBEnd     本期止
     * @param filters        可选过滤：productNameOrCode / processNameOrCode / employeeName（下钻时带父维度）
     */
    public AnalysisOutcome compareByDim(String dim, String metricLabel,
                                        String periodALabel, String periodAStart, String periodAEnd,
                                        String periodBLabel, String periodBStart, String periodBEnd,
                                        Map<String, String> filters) {
        AnalysisOutcome out = new AnalysisOutcome();
        String tenant = tenantCode();
        Map<String, String> f = filters == null ? new HashMap<>() : filters;
        String product = f.get("productNameOrCode");
        String process = f.get("processNameOrCode");
        String employee = f.get("employeeName");

        out.api = "/ai/registered/metricByDimPeriod（登记 SQL：已审口径，指标×维度×期间）";
        out.params.put("dim", dim);
        out.params.put("productNameOrCode", product);
        out.params.put("processNameOrCode", process);
        out.params.put("employeeName", employee);
        out.params.put("periodA", periodAStart + "~" + periodAEnd);
        out.params.put("periodB", periodBStart + "~" + periodBEnd);
        out.params.put("tenantCode", tenant);

        List<Map<String, Object>> rawA = dailyMapper.selectMetricByDimPeriod(tenant, periodAStart, periodAEnd, dim, product, process, employee);
        List<Map<String, Object>> rawB = dailyMapper.selectMetricByDimPeriod(tenant, periodBStart, periodBEnd, dim, product, process, employee);
        List<Map<String, Object>> normA = normalize(rawA);
        List<Map<String, Object>> normB = normalize(rawB);

        if (normA.isEmpty() && normB.isEmpty()) {
            out.notice = "统计范围内没有已审报工数据，无法做对比分析。";
            return out;
        }
        if (normA.isEmpty() || normB.isEmpty()) {
            out.notice = (normA.isEmpty() ? periodALabel : periodBLabel) + "没有已审报工数据，只做单期说明，无法给出变化原因。";
        }
        MiniAnalyst.CompareResult r = MiniAnalyst.compare(normA, normB);
        out.compare = r;
        out.rows = mergeForChart(r);
        out.brief = MiniAnalyst.brief(r, metricLabel, periodALabel, periodBLabel);
        log.info("[AI分析] dim={} {}→{} 主因={} Δ={} 样本={}/{}",
                dim, periodALabel, periodBLabel, r.mainGroup,
                r.deltaPp == null ? "-" : r.deltaPp.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString(),
                r.rowsA, r.rowsB);
        return out;
    }

    /**
     * 下钻一层：对父维度的某个取值，换下一层维度再对比一次。
     *
     * <p>例：工序维度定位到"攻丝"后，下钻到 不良类型/产品/员工 看具体是谁。
     */
    public AnalysisOutcome drilldown(String parentDim, String parentValue, String childDim, String metricLabel,
                                     String periodALabel, String periodAStart, String periodAEnd,
                                     String periodBLabel, String periodBStart, String periodBEnd) {
        Map<String, String> filters = new HashMap<>();
        if ("process".equals(parentDim)) {
            filters.put("processNameOrCode", parentValue);
        } else if ("product".equals(parentDim)) {
            filters.put("productNameOrCode", parentValue);
        } else if ("employee".equals(parentDim)) {
            filters.put("employeeName", parentValue);
        }
        AnalysisOutcome out = compareByDim(childDim, metricLabel, periodALabel, periodAStart, periodAEnd,
                periodBLabel, periodBStart, periodBEnd, filters);
        out.params.put("parentDim", parentDim);
        out.params.put("parentValue", parentValue);
        return out;
    }

    /**
     * 不良类型集中度：回答"不良集中在哪一类"。
     *
     * <p>质检开关未开启（质检记录表为空）时，返回 notice，让上层如实说明而不是编 0。
     */
    public AnalysisOutcome ngTypeConcentration(String metricLabel, String startDate, String endDate,
                                               Map<String, String> filters) {
        AnalysisOutcome out = new AnalysisOutcome();
        String tenant = tenantCode();
        Map<String, String> f = filters == null ? new HashMap<>() : filters;
        String product = f.get("productNameOrCode");
        String process = f.get("processNameOrCode");

        out.api = "/ai/registered/ngTypeByPeriod（登记 SQL：质检记录，不良类型×期间）";
        out.params.put("productNameOrCode", product);
        out.params.put("processNameOrCode", process);
        out.params.put("startDate", startDate);
        out.params.put("endDate", endDate);
        out.params.put("tenantCode", tenant);

        List<Map<String, Object>> raw = dailyMapper.selectNgTypeByPeriod(tenant, startDate, endDate, product, process);
        if (raw == null || raw.isEmpty()) {
            out.notice = "没有质检记录数据（质检开关未开启或该范围未质检），无法按不良类型拆解；"
                    + "可先看工序/产品/员工三个维度的对比。";
            return out;
        }
        // 集中度按"不良数"算：把 ngNum 映射到 passNum 槽位（MiniAnalyst.concentrate 的分子槽）
        List<Map<String, Object>> norm = new ArrayList<>();
        for (Map<String, Object> row : raw) {
            Map<String, Object> m = new HashMap<>(row);
            m.put("passNum", row.get("ngNum"));
            m.put("group", row.get("groupName"));
            norm.add(m);
        }
        MiniAnalyst.Concentration c = MiniAnalyst.concentrate(norm, 3);
        out.rows = norm;
        out.brief = MiniAnalyst.brief(c, metricLabel);
        return out;
    }

    // ------------------------------------------------------------------ 内部工具

    /** 把 SQL 别名归一成 MiniAnalyst 约定（group/groupKey/passNum/totalNum/rows） */
    private List<Map<String, Object>> normalize(List<Map<String, Object>> raw) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (raw == null) {
            return list;
        }
        for (Map<String, Object> row : raw) {
            Map<String, Object> m = new HashMap<>(row);
            m.put("group", row.get("groupName"));
            m.put("groupKey", row.get("groupKey"));
            // SQL 别名为 rowCnt（rows 是 MySQL 8 保留字，不能做别名）；这里归一成 MiniAnalyst 约定的 rows
            Object cnt = row.get("rowCnt");
            m.put("rows", cnt == null ? row.get("rows") : cnt);
            // 无质检数的分组不参与比率（MiniAnalyst 内部对 totalNum=0 已做贡献为 0 的处理）
            list.add(m);
        }
        return list;
    }

    /** 供前端画图：每个分组的 两期良品率 + 贡献（百分点），按 |贡献| 降序 */
    private List<Map<String, Object>> mergeForChart(MiniAnalyst.CompareResult r) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (r == null) {
            return rows;
        }
        for (MiniAnalyst.Contribution c : r.items) {
            Map<String, Object> m = new HashMap<>();
            m.put("group", c.group);
            m.put("rateA", c.rateA);
            m.put("rateB", c.rateB);
            m.put("contributionPp", c.contributionPp);
            m.put("rowsA", c.rowsA);
            m.put("rowsB", c.rowsB);
            m.put("newGroup", c.newGroup);
            m.put("goneGroup", c.goneGroup);
            rows.add(m);
        }
        return rows;
    }

    private String tenantCode() {
        Object v = com.cosmo.hhim.common.core.threadlocal.ThreadContext.get(
                com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
        return v == null ? "" : v.toString();
    }
}

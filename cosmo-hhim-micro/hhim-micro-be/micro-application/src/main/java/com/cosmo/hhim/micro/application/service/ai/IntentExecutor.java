/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.application.dto.ai.AskExecutionResult;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.analysis.ProductionQualityAnalysisParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 问数 · 指标执行器
 *
 * <p>意图 → 本体登记接口 → 调用现有 IMicroAnalysisService（SQL 在既有 Mapper 中，不新增查询）→
 * 结果投影（Top N 裁剪 + 排序）。首期支持：SUMMARY / PRODUCT_PASS_RATE / PROCESS_PASS_RATE /
 * EMPLOYEE_PASS_RATE / SUBMIT_RANK / NG_DETAIL / STOCK。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentExecutor {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private final IMicroAnalysisService microAnalysisService;
    private final com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDailyMapper dailyMapper;
    private final com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDictMapper dictMapper;

    /* ---------------- 组合登记台账（"覆盖不到"的唯一真相 = 本声明 ∩ 本体 dims） ----------------
     * 背景：本体 dims 是"语义上可以这么问"的白名单，执行器覆盖是"确实登记了实现"的白名单，
     * 两者不一致时若不收口，会出现两类静默失败：
     * ① 静默空数据（执行器返回 0 行且无错误 → 被 Reflector 当成"合法空结果"→ 用户以为真的没数据）；
     * ② 静默答非所问（执行器忽略 groupBy → 返回别的聚合形状）。
     * 因此：未消费且不可容忍的粒度 → 确定性"未登记"错误（走引导话术，并打 [AI缺口] 日志）。
     * 新增组合时同步更新本声明；启动自检 ExecutionCoverageChecker 会打本体与实现的差集。
     */

    /** 已登记执行器的指标（能力矩阵自检/诊断用） */
    private static final Set<String> IMPLEMENTED_METRICS = new LinkedHashSet<>(Arrays.asList(
            "SUMMARY", "PRODUCT_PASS_RATE", "PROCESS_PASS_RATE", "EMPLOYEE_PASS_RATE", "SUBMIT_RANK",
            "NG_DETAIL", "STOCK", "DELIVERY_RISK", "ENTITY_LIST",
            // 分析算子：不在本执行器里执行，由 AnalysisService + MiniAnalyst 实现。
            // 登记在此处是为了让 [AI覆盖] 自检与"命中即执行"的判定把它们与指标同等看待（都是一等能力槽位）。
            "ATTRIBUTION", "CONCENTRATE", "COMPARE"));

    /** 分析算子（由 AnalysisService 执行，不是登记 SQL） */
    public static boolean isAnalysisOperator(String code) {
        return "ATTRIBUTION".equals(code) || "CONCENTRATE".equals(code) || "COMPARE".equals(code);
    }

    /** 执行器真正消费 groupBy 的指标 → 已实现的分组粒度 */
    private static final Map<String, Set<String>> CONSUMED_GROUP_BY;
    /** 执行器不消费 groupBy，但返回行天然带该维度（近似可接受：记录缺口，不拒绝） */
    private static final Map<String, Set<String>> TOLERATED_GROUP_BY;
    /** 全局容忍的伪粒度：time 是"时间范围"维度，不是分组粒度（本体 dims 里两者混放，待拆 timeRange/groupBy） */
    private static final Set<String> GLOBAL_TOLERATED = new LinkedHashSet<>(Collections.singletonList("time"));
    /**
     * 默认执行路径会把"该维度的实体"作为过滤条件下推的 (指标 → 粒度) 组合：
     * 例如"法兰盘产量"= SUMMARY + entities.productNameOrCode，此时 groupBy=product 是冗余的，
     * 忽略分组不影响答案正确性（否则会误伤这类已验证问法）。
     */
    private static final Map<String, Set<String>> ENTITY_NARROWED_OK;
    /** 粒度 → 实体键（判断是否已被单个实体约束） */
    private static final Map<String, String> GROUP_BY_ENTITY_KEY;

    static {
        Map<String, Set<String>> consumed = new LinkedHashMap<>();
        consumed.put("SUMMARY", dims("day", "product", "employee", "process"));
        // 三条良品率查询的参数签名统一（product/process/employee 三个槽位都可作为过滤），
        // 因此"按产品+限定工序""按工序+限定产品""按产品+限定员工"这些跨维度组合都已登记
        consumed.put("PRODUCT_PASS_RATE", dims("product", "day", "process"));
        consumed.put("PROCESS_PASS_RATE", dims("process", "product"));
        consumed.put("EMPLOYEE_PASS_RATE", dims("employee", "product"));
        consumed.put("SUBMIT_RANK", dims("employee"));
        consumed.put("STOCK", dims("product"));
        CONSUMED_GROUP_BY = Collections.unmodifiableMap(consumed);

        Map<String, Set<String>> tolerated = new LinkedHashMap<>();
        tolerated.put("NG_DETAIL", dims("product", "process", "ngType"));
        tolerated.put("DELIVERY_RISK", dims("order", "product"));
        tolerated.put("ENTITY_LIST", dims("product", "process", "employee"));
        TOLERATED_GROUP_BY = Collections.unmodifiableMap(tolerated);

        Map<String, Set<String>> narrowed = new LinkedHashMap<>();
        narrowed.put("SUMMARY", dims("product"));
        narrowed.put("PRODUCT_PASS_RATE", dims("product"));
        narrowed.put("NG_DETAIL", dims("product"));
        ENTITY_NARROWED_OK = Collections.unmodifiableMap(narrowed);

        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("product", "productNameOrCode");
        keys.put("process", "processNameOrCode");
        keys.put("employee", "employeeName");
        keys.put("order", "orderNo");
        keys.put("ngType", "ngType");
        GROUP_BY_ENTITY_KEY = Collections.unmodifiableMap(keys);
    }

    private static Set<String> dims(String... ds) {
        return new LinkedHashSet<>(Arrays.asList(ds));
    }

    /** 该指标是否已登记执行器 */
    public static boolean isRegistered(String metricCode) {
        return metricCode != null && IMPLEMENTED_METRICS.contains(metricCode);
    }

    public static Set<String> registeredMetrics() {
        return IMPLEMENTED_METRICS;
    }

    /** 已实现的分组粒度（用户话术"已支持："用） */
    public static Set<String> consumedGroupBys(String code) {
        Set<String> s = CONSUMED_GROUP_BY.get(code);
        return s == null ? Collections.<String>emptySet() : s;
    }

    public static Set<String> toleratedGroupBys(String code) {
        Set<String> s = TOLERATED_GROUP_BY.get(code);
        return s == null ? Collections.<String>emptySet() : s;
    }

    /**
     * 分组粒度（人话标签），用于"目前不支持按 X 看"话术
     */
    public static String groupByLabel(String groupBy) {
        if (groupBy == null) {
            return "";
        }
        switch (groupBy) {
            case "day":
                return "天";
            case "product":
                return "产品";
            case "process":
                return "工序";
            case "employee":
                return "员工";
            case "order":
                return "订单";
            case "ngType":
                return "不良类型";
            case "stock":
                return "库存";
            case "time":
                return "时间";
            default:
                return groupBy;
        }
    }

    /**
     * 组合登记收口校验（在 Act 之前判定，返回非空=确定性"未登记"）
     */
    private String coverageViolation(AskIntentResult intent, String metricCode) {
        String gb = intent == null ? null : intent.getGroupBy();
        if (!StringUtils.hasText(gb) || !isRegistered(metricCode)) {
            return null;
        }
        if (CONSUMED_GROUP_BY.getOrDefault(metricCode, Collections.<String>emptySet()).contains(gb)) {
            return null;
        }
        if (GLOBAL_TOLERATED.contains(gb) || TOLERATED_GROUP_BY.getOrDefault(metricCode, Collections.<String>emptySet()).contains(gb)) {
            log.warn("[AI缺口] type=IGNORED_GROUP_BY intent={} groupBy={}（执行器忽略该粒度，按默认形状返回）", metricCode, gb);
            return null;
        }
        // 已被单个实体约束（如"法兰盘产量"+groupBy=product）→ 分组冗余，不影响正确性
        String entityKey = GROUP_BY_ENTITY_KEY.get(gb);
        boolean narrowed = entityKey != null
                && ENTITY_NARROWED_OK.getOrDefault(metricCode, Collections.<String>emptySet()).contains(gb)
                && StringUtils.hasText(intent.getEntities() == null ? null : intent.getEntities().get(entityKey));
        if (narrowed) {
            log.info("[AI执行] groupBy={} 已被实体 {} 约束（冗余分组，忽略）: intent={}", gb, entityKey, metricCode);
            return null;
        }
        log.warn("[AI缺口] type=UNREGISTERED_COMBO intent={} groupBy={} scope={}", metricCode, gb, intent.getStatScope());
        return "该统计组合尚未登记: " + metricCode + " × groupBy=" + gb;
    }

    public AskExecutionResult execute(String metricCode, AskIntentResult intent) {
        AskExecutionResult r = new AskExecutionResult();
        r.setMetricCode(metricCode);
        // 组合登记收口：本体 dims 内的粒度但执行器未登记 → 确定性错误（禁止静默空数据/静默换维度）
        String violation = coverageViolation(intent, metricCode);
        if (violation != null) {
            r.setError(violation);
            return r;
        }
        try {
            // 组合式：SUMMARY × groupBy=day（"哪天产量最高"类），走登记按日聚合 SQL
            if ("SUMMARY".equals(metricCode) && "day".equals(intent.getGroupBy())) {
                summaryByDay(intent, r);
                return r;
            }
            // 组合式：SUMMARY × {product|employee|process}——"哪个产品/员工/工序产量最高"
            // 复用良品率登记 SQL（同一批已审聚合），只换排序键=已审记工总数，不新增 SQL
            if ("SUMMARY".equals(metricCode) && StringUtils.hasText(intent.getGroupBy())) {
                summaryRank(intent, r, intent.getGroupBy());
                return r;
            }
            // 组合式：良品率类指标 × 维度分组（process/product/employee/day）——"哪道工序呢/按天呢"
            if (("PRODUCT_PASS_RATE".equals(metricCode) || "PROCESS_PASS_RATE".equals(metricCode)
                    || "EMPLOYEE_PASS_RATE".equals(metricCode)) && StringUtils.hasText(intent.getGroupBy())) {
                if ("day".equals(intent.getGroupBy())) {
                    passRateByDay(intent, r);
                } else {
                    passRate(intent, r, intent.getGroupBy());
                }
                return r;
            }
            switch (metricCode) {
                case "SUMMARY":
                    summary(intent, r);
                    break;
                case "PRODUCT_PASS_RATE":
                    passRate(intent, r, "product");
                    break;
                case "PROCESS_PASS_RATE":
                    passRate(intent, r, "process");
                    break;
                case "EMPLOYEE_PASS_RATE":
                    passRate(intent, r, "employee");
                    break;
                case "SUBMIT_RANK":
                    submitRank(intent, r);
                    break;
                case "NG_DETAIL":
                    ngDetail(intent, r);
                    break;
                case "STOCK":
                    if ("product".equals(intent.getGroupBy())) {
                        stockByProduct(intent, r);
                    } else {
                        stock(intent, r);
                    }
                    break;
                case "ENTITY_LIST":
                    entityList(intent, r);
                    break;
                case "DELIVERY_RISK":
                    deliveryRisk(intent, r);
                    break;
                default:
                    r.setError("该指标暂未支持执行器: " + metricCode);
            }
        } catch (Exception e) {
            log.warn("[AI执行] {} 执行异常: {}", metricCode, e.getMessage());
            r.setError("执行异常: " + e.getMessage());
        }
        return r;
    }

    /* ---------------- 各指标执行 ---------------- */

    private void summary(AskIntentResult intent, AskExecutionResult r) {
        // 口径第一性：submission=含未审核报工行为（登记 SQL）；production=已审产出（与质量页同源）
        if ("submission".equals(intent.getStatScope())) {
            r.setApi("/ai/registered/submitSummaryRaw（登记 SQL：报工汇总·含未审核）");
            r.setParams(strMap("startDate", intent.getStartDate(), "endDate", intent.getEndDate(), "tenantCode", tenantCode()));
            Map<String, Object> data = dailyMapper.selectSubmitSummaryRaw(tenantCode(), intent.getStartDate(), intent.getEndDate());
            List<Map<String, Object>> rows = new java.util.ArrayList<>();
            if (data != null) {
                rows.add(data);
            }
            r.setRows(rows);
            return;
        }
        // 与质量趋势页同源口径：/analysis/showTotalCount（已审口径，与页面"记工总数/良品/不良/良品率"一致）
        ProductionQualityAnalysisParam p = new ProductionQualityAnalysisParam();
        String entity = intent.getEntities().get("productNameOrCode");
        if (StringUtils.hasText(entity)) {
            p.setProductNameOrCode(entity);
        }
        LocalDate start = parseDate(intent.getStartDate());
        LocalDate end = parseDate(intent.getEndDate());
        r.setApi("/analysis/showTotalCount");
        r.setParams(strMap("productNameOrCode", entity, "startDate", intent.getStartDate(), "endDate", intent.getEndDate()));
        Object data = microAnalysisService.showTotalCount(entity, start, end);
        r.setRows(toRows(data, 1));
    }

    /** SUMMARY × day：按日聚合；submission=全量（含未审），production=已审 */
    private void summaryByDay(AskIntentResult intent, AskExecutionResult r) {
        String start = intent.getStartDate();
        String end = intent.getEndDate();
        boolean submission = "submission".equals(intent.getStatScope());
        r.setApi(submission ? "/ai/registered/dailySummaryRaw（登记 SQL：按日报工·含未审核）"
                : "/ai/daily/summary（登记按日聚合 SQL）");
        r.setParams(strMap("startDate", start, "endDate", end, "tenantCode", tenantCode(), "scope", submission ? "submission" : "production"));
        // 组合排行类问题（"最高的是哪天"）：LLM 可能给 limit=1，产品语义仍展示 Top3 排名上下文
        if (intent.getLimit() == null || intent.getLimit() < 3) {
            intent.setLimit(3);
        }
        List<Map<String, Object>> data = submission
                ? dailyMapper.selectDailySummaryRaw(tenantCode(), start, end)
                : dailyMapper.selectDailySummary(tenantCode(), start, end);
        log.info("[AI执行] SUMMARY×day({}): start={}, end={}, rawRows={}, limit={}, orderDir={}",
                submission ? "submission" : "production", start, end, data == null ? 0 : data.size(),
                intent.getLimit(), intent.getOrderDir());
        // 产量口径 = 良品数（passNum）：报工合格数才是"产出"，不良不计入产量排序
        r.setRows(project(data, intent, new String[]{"submitDay", "passNum", "ngNum"}, "passNum"));
        log.info("[AI执行] SUMMARY×day: afterProject={}, api={}", r.getRows().size(), r.getApi());
    }

    private String tenantCode() {
        Object v = com.cosmo.hhim.common.core.threadlocal.ThreadContext.get(
                com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
        return v == null ? "" : v.toString();
    }

    /**
     * SUMMARY × {product|employee|process}：产量排行（"哪个产品/员工/工序产量最高"）
     *
     * <p>口径与"某个产品产量"单值答案完全一致：**已审记工总数 = checkPassNum + checkNgNum**
     * （页面上"上个月法兰盘产量"回的就是记工总数 92）。复用同一批良品率登记 SQL，
     * 只换排序键与投影字段——零新增 SQL。
     */
    private void summaryRank(AskIntentResult intent, AskExecutionResult r, String dim) {
        String start = intent.getStartDate();
        String end = intent.getEndDate();
        List<Map<String, Object>> data;
        String[] nameKeys;
        // 三个实体槽位统一取出：目标维度决定"分组"，另外两个维度作为可选过滤（跨维度组合问法）
        String productEntity = intent.getEntities().get("productNameOrCode");
        String processEntity = intent.getEntities().get("processNameOrCode");
        String employeeEntity = intent.getEntities().get("employeeName");
        if ("product".equals(dim)) {
            r.setApi("/ai/registered/passRateByProduct（登记 SQL·复用：按已审记工总数排行）");
            r.setParams(strMap("productNameOrCode", productEntity, "processNameOrCode", processEntity,
                    "employeeName", employeeEntity, "startDate", start, "endDate", end, "tenantCode", tenantCode()));
            nameKeys = new String[]{"productName", "productCode", "checkPassNum", "checkNgNum"};
            data = dailyMapper.selectProductPassRate(tenantCode(), start, end, productEntity, processEntity, employeeEntity);
        } else if ("process".equals(dim)) {
            r.setApi("/ai/registered/passRateByProcess（登记 SQL·复用：按已审记工总数排行）");
            r.setParams(strMap("processNameOrCode", processEntity, "productNameOrCode", productEntity,
                    "employeeName", employeeEntity, "startDate", start, "endDate", end, "tenantCode", tenantCode()));
            nameKeys = new String[]{"processName", "processCode", "checkPassNum", "checkNgNum"};
            data = dailyMapper.selectProcessPassRate(tenantCode(), start, end, processEntity, productEntity, employeeEntity);
        } else {
            r.setApi("/ai/registered/passRateByEmployee（登记 SQL·复用：按已审记工总数排行）");
            r.setParams(strMap("employeeName", employeeEntity, "productNameOrCode", productEntity,
                    "processNameOrCode", processEntity, "startDate", start, "endDate", end, "tenantCode", tenantCode()));
            nameKeys = new String[]{"nickName", "userName", "checkPassNum", "checkNgNum"};
            data = dailyMapper.selectEmployeePassRate(tenantCode(), start, end, employeeEntity, productEntity, processEntity);
        }
        List<Map<String, Object>> withTotal = new java.util.ArrayList<>();
        if (data != null) {
            for (Map<String, Object> row : data) {
                Map<String, Object> m = new HashMap<>(row);
                m.put("totalNum", toDouble(m.get("checkPassNum")) + toDouble(m.get("checkNgNum")));
                withTotal.add(m);
            }
        }
        r.setRows(project(withTotal, intent, nameKeys, "totalNum"));
    }

    /**
     * 良品率 × day：按天良品率（"哪天良品率最低"）
     *
     * <p>复用按日聚合登记 SQL（已审口径），比率在 Java 侧按页面口径**截断 3 位**后排序——零新增 SQL。
     */
    private void passRateByDay(AskIntentResult intent, AskExecutionResult r) {
        String start = intent.getStartDate();
        String end = intent.getEndDate();
        // 排行类问题（"哪天良品率最低"）LLM 可能给 limit=1：至少取 3 条，
        // 一是给排名上下文，二是避免渲染层把"只返回了 1 条"误判成"只有一天有数据"
        if (intent.getLimit() == null || intent.getLimit() < 3) {
            intent.setLimit(3);
        }
        r.setApi("/ai/registered/dailySummary（登记 SQL·复用：按天算良品率）");
        r.setParams(strMap("startDate", start, "endDate", end, "tenantCode", tenantCode()));
        List<Map<String, Object>> data = dailyMapper.selectDailySummary(tenantCode(), start, end);
        List<Map<String, Object>> withRate = new java.util.ArrayList<>();
        if (data != null) {
            for (Map<String, Object> row : data) {
                double pass = toDouble(row.get("passNum"));
                double ng = toDouble(row.get("ngNum"));
                double total = pass + ng;
                // 兜底：无质检数的日子不参与良品率排名（不得当成 0%，那是假数；SQL 侧已有 having 双保险）
                if (total <= 0) {
                    log.warn("[AI缺口] type=ZERO_CHECK_DAY day={}（无质检数，已从良品率排名剔除）", row.get("submitDay"));
                    continue;
                }
                Map<String, Object> m = new HashMap<>(row);
                // 与页面同取舍：截断 3 位（页面 = BigDecimal.divide(...,3,RoundingMode.DOWN)）
                // 必须放 BigDecimal 而不是 double：DataBrief 的比率格式化按 BigDecimal 识别，
                // 放 double 会让 LLM 把 0.952 当裸小数念出来（"良品率为 0.952"）
                m.put("passRate", java.math.BigDecimal.valueOf(pass / total)
                        .setScale(3, java.math.RoundingMode.DOWN));
                withRate.add(m);
            }
        }
        r.setRows(project(withRate, intent, new String[]{"submitDay"}, "passRate"));
    }

    /**
     * 产品维度库存排行。
     *
     * <p>**数据源按问题措辞确定性选择**（原来只查成品库存，导致"上周各产品在制余额"被答成成品库存）：
     * <ul>
     *   <li>问"在制/在制品/车间/工序还有多少" → 工序在制品（micro_process_storage）</li>
     *   <li>问"成品/仓库/库存有多少" → 成品库存（micro_finished_product_storage）</li>
     *   <li>两者都没明说（如"库存还有多少"之外的泛问）→ 两种都给，分别标注</li>
     * </ul>
     * 库存是**时点快照**，与时间区间无关；区间信息由渲染层如实说明（不参与过滤）。
     */
    private void stockByProduct(AskIntentResult intent, AskExecutionResult r) {
        String q = intent.getQuestion() == null ? "" : intent.getQuestion();
        boolean askWip = q.contains("在制") || q.contains("车间") || q.contains("工序");
        boolean askFinished = q.contains("成品") || q.contains("仓库") || q.contains("库存") || q.isEmpty();
        if (!askWip && !askFinished) {
            askFinished = true;
        }
        Map<String, Object> merged = new LinkedHashMap<>();
        if (askWip) {
            List<Map<String, Object>> wip = dailyMapper.selectProcessStockByProduct(tenantCode());
            merged.put("wip", wip == null ? new ArrayList<>() : wip);
        }
        if (askFinished) {
            List<Map<String, Object>> fin = dailyMapper.selectFinishedStockByProduct(tenantCode());
            merged.put("finished", fin == null ? new ArrayList<>() : fin);
        }
        r.setApi(askWip && !askFinished
                ? "/ai/registered/processStockByProduct（登记 SQL：工序在制品按产品）"
                : "/ai/registered/finishedStockByProduct（登记 SQL：成品库存按产品）");
        r.setParams(strMap("tenantCode", tenantCode()));
        r.setRows(java.util.Collections.singletonList(merged));
    }

    /**
     * 良品率 × {product|process|employee}：登记 SQL（已审口径 + nullif 防除零 + 实体下推）
     *
     * <p>产品维度替代既有接口（未过滤已审 + 会除零）；工序/员工维度带实体过滤参数，
     * 使"车削工序的良品率""张三这个月良品率多少"真正按该实体下推，而不是返回无关的 Top3 排行。
     */
    private void passRate(AskIntentResult intent, AskExecutionResult r, String dim) {
        String start = intent.getStartDate();
        String end = intent.getEndDate();
        // 未指定实体时这是排行类问题（"良品率最低的产品/工序/员工"）：至少取 3 条给排名上下文
        boolean ranking = !StringUtils.hasText(intent.getEntities().get("productNameOrCode"))
                && !StringUtils.hasText(intent.getEntities().get("processNameOrCode"))
                && !StringUtils.hasText(intent.getEntities().get("employeeName"));
        if (ranking && (intent.getLimit() == null || intent.getLimit() < 3)) {
            intent.setLimit(3);
        }
        // 三个实体槽位统一取出：dim 决定"分组"，另外两个槽位作为可选过滤 → 跨维度组合
        String productEntity = intent.getEntities().get("productNameOrCode");
        String processEntity = intent.getEntities().get("processNameOrCode");
        String employeeEntity = intent.getEntities().get("employeeName");
        // 产品维度：登记 SQL（已审口径，与良品率列表/趋势同源——既有接口未过滤已审，导致口径差/除零）
        if ("product".equals(dim)) {
            r.setApi("/ai/registered/passRateByProduct（登记 SQL：已审口径）");
            r.setParams(strMap("productNameOrCode", productEntity, "processNameOrCode", processEntity,
                    "employeeName", employeeEntity, "startDate", start, "endDate", end, "tenantCode", tenantCode()));
            List<Map<String, Object>> data = dailyMapper.selectProductPassRate(tenantCode(), start, end,
                    productEntity, processEntity, employeeEntity);
            r.setRows(project(data, intent,
                    new String[]{"productName", "productCode", "checkPassNum", "checkNgNum"}, "passRate"));
            return;
        }
        // 工序维度：登记 SQL（已审口径 + nullif 防除零 + 工序/产品/员工实体下推）
        if ("process".equals(dim)) {
            r.setApi("/ai/registered/passRateByProcess（登记 SQL：已审口径）");
            r.setParams(strMap("processNameOrCode", processEntity, "productNameOrCode", productEntity,
                    "employeeName", employeeEntity, "startDate", start, "endDate", end, "tenantCode", tenantCode()));
            List<Map<String, Object>> data = dailyMapper.selectProcessPassRate(tenantCode(), start, end,
                    processEntity, productEntity, employeeEntity);
            r.setRows(project(data, intent,
                    new String[]{"processName", "processCode", "checkPassNum", "checkNgNum"}, "passRate"));
            return;
        }
        // 员工维度：登记 SQL（已审口径 + nullif 防除零 + 员工/产品/工序实体下推）
        if ("employee".equals(dim)) {
            r.setApi("/ai/registered/passRateByEmployee（登记 SQL：已审口径）");
            r.setParams(strMap("employeeName", employeeEntity, "productNameOrCode", productEntity,
                    "processNameOrCode", processEntity, "startDate", start, "endDate", end, "tenantCode", tenantCode()));
            List<Map<String, Object>> data = dailyMapper.selectEmployeePassRate(tenantCode(), start, end,
                    employeeEntity, productEntity, processEntity);
            r.setRows(project(data, intent,
                    new String[]{"nickName", "userName", "checkPassNum", "checkNgNum"}, "passRate"));
            return;
        }
        // 兜底（不应到达：入口收口已拦截未登记粒度）：明确报"未登记"，绝不返回空数据
        // （返回空数据会被 Reflector 判为"合法空结果"，用户看到"暂无已审核报工数据"而误以为真没数据）
        r.setApi("/ai/registered/(未登记粒度)");
        r.setParams(strMap("startDate", start, "endDate", end, "groupBy", dim));
        r.setRows(new java.util.ArrayList<>());
        log.warn("[AI缺口] type=UNREGISTERED_PASS_RATE_DIM dim={}", dim);
        r.setError("该统计组合尚未登记: passRate × groupBy=" + dim);
    }

    private void submitRank(AskIntentResult intent, AskExecutionResult r) {
        // 口径第一性：submission=含未审（谁报工）；production=已审（谁产量，与记工排行页同源）
        if ("submission".equals(intent.getStatScope())) {
            r.setApi("/ai/registered/workRankRaw（登记 SQL：记工排名·含未审核）");
            r.setParams(strMap("startDate", intent.getStartDate(), "endDate", intent.getEndDate(), "tenantCode", tenantCode()));
            List<Map<String, Object>> data = dailyMapper.selectWorkRankRaw(tenantCode(), intent.getStartDate(), intent.getEndDate());
            r.setRows(data != null && data.size() > 10 ? data.subList(0, 10) : data);
            return;
        }
        LocalDate start = parseDate(intent.getStartDate());
        LocalDate end = parseDate(intent.getEndDate());
        r.setApi("/analysis/submitRecordRank");
        r.setParams(strMap("startDate", intent.getStartDate(), "endDate", intent.getEndDate()));
        List<?> data = microAnalysisService.getRecordRankForSubmitter(start, end);
        r.setRows(project(data, intent, new String[]{"nickName", "userName"}, "submitNum"));
    }

    private void ngDetail(AskIntentResult intent, AskExecutionResult r) {
        ProductionQualityAnalysisParam p = new ProductionQualityAnalysisParam();
        applyTime(p, intent);
        String entity = intent.getEntities().get("productNameOrCode");
        if (StringUtils.hasText(entity)) {
            p.setProductNameOrCode(entity);
        }
        r.setApi("/analysis/showNgProductList");
        r.setParams(toParams(p));
        List<?> data = microAnalysisService.showNgProductList(p);
        r.setRows(project(data, intent, new String[]{"productName", "processName", "ngTypeName", "nickName"}, "ngNum"));
    }

    private void stock(AskIntentResult intent, AskExecutionResult r) {
        // 登记 SQL：成品库存 + 租户隔离（既有 obtainedTotalStock 查的是在制品且无租户过滤，口径不可信）
        r.setApi("/ai/registered/finishedStock（登记 SQL：成品库存+租户隔离）");
        r.setParams(strMap("tenantCode", tenantCode()));
        Map<String, Object> data = dailyMapper.selectFinishedStock(tenantCode());
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        if (data != null) {
            rows.add(data);
        }
        // 泛问"库存还有多少"（没明说成品/在制）时，把在制品合计一并给出，避免只答成品而丢掉在制
        String q = intent.getQuestion() == null ? "" : intent.getQuestion();
        boolean askWip = q.contains("在制") || q.contains("车间") || q.contains("工序");
        boolean explicitFinished = q.contains("成品") || q.contains("仓库");
        if (askWip || !explicitFinished) {
            List<Map<String, Object>> wip = dailyMapper.selectProcessStockByProduct(tenantCode());
            long wipTotal = 0L;
            int wipProducts = 0;
            if (wip != null) {
                for (Map<String, Object> row : wip) {
                    Object v = row.get("wipNum");
                    if (v instanceof Number) {
                        wipTotal += ((Number) v).longValue();
                    }
                    wipProducts++;
                }
            }
            if (!rows.isEmpty()) {
                rows.get(0).put("wipTotal", wipTotal);
                rows.get(0).put("wipProductCnt", wipProducts);
            }
        }
        r.setRows(rows);
    }

    /** ENTITY_LIST：实体清单（字典列举，Top 10） */
    private void entityList(AskIntentResult intent, AskExecutionResult r) {
        String type = intent.getEntities().getOrDefault("entityType", "product");
        r.setApi("/ai/dict/list");
        r.setParams(strMap("entityType", type, "tenantCode", tenantCode()));
        List<Map<String, Object>> data = dictMapper.selectEntityList(tenantCode(), type);
        r.setRows(data != null && data.size() > 10 ? data.subList(0, 10) : data);
    }

    /** DELIVERY_RISK：订单延期预警 / 近期交付（"快交付"模式） */
    private void deliveryRisk(AskIntentResult intent, AskExecutionResult r) {
        java.math.BigDecimal cap = dailyMapper.selectAvgDailyCapacity(tenantCode());
        java.math.BigDecimal safeCap = cap == null ? java.math.BigDecimal.ZERO : cap;
        boolean soon = "soon".equals(intent.getMode());
        // 产能缺失兜底：cap<=0 时无法预测（"0 天完成"假阴性），明确标记由渲染层告知
        if (safeCap.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            r.setApi(soon ? "/ai/registered/deliverySoon（登记 SQL：近期交付）"
                    : "/ai/registered/deliveryRisk（登记 SQL：订单延期预测）");
            r.setParams(strMap("mode", soon ? "soon" : "overdue", "capZero", "1", "dailyCap", "0", "tenantCode", tenantCode()));
            r.setRows(new java.util.ArrayList<>());
            return;
        }
        r.setApi(soon ? "/ai/registered/deliverySoon（登记 SQL：近期交付）"
                : "/ai/registered/deliveryRisk（登记 SQL：订单延期预测）");
        r.setParams(strMap("mode", soon ? "soon" : "overdue", "dailyCap", safeCap.toPlainString(), "tenantCode", tenantCode()));
        List<Map<String, Object>> data = soon ? dailyMapper.selectDeliverySoon(tenantCode(), safeCap)
                : dailyMapper.selectDeliveryRisk(tenantCode(), safeCap);
        r.setRows(data != null && data.size() > 10 ? data.subList(0, 10) : data);
    }

    /* ---------------- 时间/参数 ---------------- */

    private void applyTime(ProductionQualityAnalysisParam p, AskIntentResult intent) {
        if (StringUtils.hasText(intent.getStartDate()) && StringUtils.hasText(intent.getEndDate())) {
            p.setStartDate(LocalDate.parse(intent.getStartDate()));
            p.setEndDate(LocalDate.parse(intent.getEndDate()));
            p.setTimeType("3");
        } else {
            // 默认本月（与页面一致）：timeType + 起止日期齐全，避免"时间类型、开始时间和结束时间均不能为空"
            LocalDate today = LocalDate.now();
            LocalDate first = today.withDayOfMonth(1);
            p.setTimeType("1");
            p.setStartDate(first);
            p.setEndDate(first.withDayOfMonth(first.lengthOfMonth()));
        }
    }

    private Date toDate(String s) {
        if (s == null) {
            return new Date();
        }
        try {
            return SDF.parse(s);
        } catch (Exception e) {
            return new Date();
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    /* ---------------- 投影 ---------------- */

    /** 数据 → 行 Map（保留读得懂的 key） */
    private List<Map<String, Object>> toRows(Object data, int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (data == null) {
            return rows;
        }
        if (data instanceof List) {
            for (Object o : (List<?>) data) {
                rows.add(toMap(o));
                if (rows.size() >= limit) {
                    break;
                }
            }
        } else {
            rows.add(toMap(data));
        }
        return rows;
    }

    /**
     * 排序 + TopN 投影：按 numericKey（如 passRate/submitNum/ngNum）排序，取前 limit；
     * 保留 nameKeys 可读字段。
     */
    private List<Map<String, Object>> project(List<?> data, AskIntentResult intent, String[] nameKeys, String numericKey) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (data == null) {
            return rows;
        }
        for (Object o : data) {
            Map<String, Object> full = toMap(o);
            Map<String, Object> item = new HashMap<>();
            for (String k : nameKeys) {
                Object v = full.get(k);
                if (v != null) {
                    item.put(k, v);
                }
            }
            Object num = firstOf(full, numericKey, "passRate", "submitNum", "totalNagNum", "totalPassNum", "ngNum", "num");
            if (num != null) {
                item.put(numericKey, num);
            } else {
                item.put(numericKey, full.values().stream().filter(v -> v instanceof Number).findFirst().orElse(null));
            }
            rows.add(item);
        }
        // 排序（数字取不到则保持原序）
        Comparator<Map<String, Object>> cmp = Comparator.comparingDouble(
                (Map<String, Object> m) -> toDouble(m.get(numericKey)));
        if ("desc".equals(intent.getOrderDir())) {
            cmp = cmp.reversed();
        }
        rows.sort(cmp);
        int limit = intent.getLimit() == null ? 3 : intent.getLimit();
        return rows.size() > limit ? new ArrayList<>(rows.subList(0, limit)) : rows;
    }

    private double toDouble(Object v) {
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v != null) {
            try {
                return Double.parseDouble(v.toString());
            } catch (Exception ignore) {
            }
        }
        return 0d;
    }

    private Object firstOf(Map<String, Object> full, String... keys) {
        for (String k : keys) {
            Object v = full.get(k);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    private Map<String, Object> toMap(Object o) {
        // fastjson 的 JSONObject 也实现 Map：直接返回，避免 toJavaObject(HashMap) 的
        // "can not get javaBeanDeserializer. java.util.HashMap" 异常
        if (o instanceof Map) {
            return (Map<String, Object>) o;
        }
        Object json = JSON.toJSON(o);
        if (json instanceof JSONObject) {
            return ((JSONObject) json).getInnerMap();
        }
        // 标量/其他类型：不抛异常，返回空 map（防御性）
        return new HashMap<>();
    }

    private Map<String, String> toParams(Object dto) {
        Map<String, String> m = new HashMap<>();
        try {
            JSONObject o = (JSONObject) JSON.toJSON(dto);
            for (Map.Entry<String, Object> e : o.entrySet()) {
                if (e.getValue() == null) {
                    continue;
                }
                Object v = e.getValue();
                if (v instanceof java.util.Date) {
                    // Date 值统一格式化，避免 toString() 英文串/毫秒污染
                    m.put(e.getKey(), new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) v));
                } else if (v instanceof java.util.Calendar) {
                    m.put(e.getKey(), new java.text.SimpleDateFormat("yyyy-MM-dd").format(((java.util.Calendar) v).getTime()));
                } else {
                    m.put(e.getKey(), v.toString());
                }
            }
        } catch (Exception ignore) {
        }
        return m;
    }

    private Map<String, String> strMap(String... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }
}

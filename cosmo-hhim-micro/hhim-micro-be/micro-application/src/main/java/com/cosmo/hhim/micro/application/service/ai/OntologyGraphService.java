/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 业务本体关系图（只读）。
 *
 * <p>给「本体图」页面提供：实体节点 + 关系连线 + 指标落点 + 预置故事线。
 * 数据全部来自本体的三个 JSON（由 ai-ontology/sync.js 同步到 classpath:ontology/），**不新建表**。
 *
 * <p>范围约定：只输出 {@code scope=workshop}（车间管理域＝当前前端可触达的业务）；
 * 计划域（订单/工单/任务）放在 {@code planning} 块单独返回，前端折叠展示——不投入、不误导。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class OntologyGraphService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDailyMapper dailyMapper;

    /**
     * **业务数据图（实例级）**：节点是真实业务记录（产品/工序/员工），关系是真实发生过的报工关系。
     * 全部从库里实时统计，数据更新图就更新；不含任何写死的本体文本。
     *
     * @param days 统计窗口（天），默认 30
     */
    public Map<String, Object> dataGraph(String tenantCode, int days) {
        int d = days <= 0 ? 30 : Math.min(days, 365);
        String start = java.time.LocalDate.now().minusDays(d - 1).toString();
        List<Map<String, Object>> products = safe(dailyMapper.selectDataGraphProducts(tenantCode, start));
        List<Map<String, Object>> processes = safe(dailyMapper.selectDataGraphProcesses(tenantCode, start));
        List<Map<String, Object>> employees = safe(dailyMapper.selectDataGraphEmployees(tenantCode, start));
        List<Map<String, Object>> p2w = safe(dailyMapper.selectDataGraphProductProcess(tenantCode, start));
        List<Map<String, Object>> e2w = safe(dailyMapper.selectDataGraphEmployeeProcess(tenantCode, start));
        Map<String, Object> cnt = dailyMapper.selectDataGraphCounts(tenantCode);
        cnt = cnt == null ? new java.util.LinkedHashMap<String, Object>() : cnt;

        // 员工节点只保留"本窗口有报工"的人（字典里其他人不画到图上，避免噪音）
        List<Map<String, Object>> employeesUsed = new ArrayList<>();
        for (Map<String, Object> u : employees) {
            if (num(u.get("submitCnt")) > 0) {
                employeesUsed.add(u);
            }
        }
        employees = employeesUsed;

        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, String> idMap = new LinkedHashMap<>();
        for (Map<String, Object> p : products) {
            String id = "p:" + p.get("nodeId");
            idMap.put("p" + p.get("nodeId"), id);
            nodes.add(node(id, "product", str(p.get("name")), str(p.get("code")), p, null));
        }
        for (Map<String, Object> w : processes) {
            String id = "w:" + w.get("nodeId");
            idMap.put("w" + w.get("nodeId"), id);
            nodes.add(node(id, "process", str(w.get("name")), str(w.get("code")), w, null));
        }
        for (Map<String, Object> u : employees) {
            String id = "u:" + u.get("nodeId");
            idMap.put("u" + u.get("nodeId"), id);
            nodes.add(node(id, "employee", str(u.get("name")), str(u.get("account")), u, null));
        }

        List<Map<String, Object>> links = new ArrayList<>();
        for (Map<String, Object> r : p2w) {
            String from = idMap.get("p" + r.get("productSeq"));
            String to = idMap.get("w" + r.get("processSeq"));
            if (from != null && to != null) {
                links.add(link(from, to, "经工序", r));
            }
        }
        for (Map<String, Object> r : e2w) {
            String from = idMap.get("u" + r.get("employeeId"));
            String to = idMap.get("w" + r.get("processSeq"));
            if (from != null && to != null) {
                links.add(link(from, to, "报工", r));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mode", "data");
        data.put("days", d);
        data.put("startDate", start);
        data.put("asOf", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("products", products.size());
        summary.put("processes", processes.size());
        summary.put("employees", employees.size());
        summary.put("links", links.size());
        // 字典规模（库里一共多少个 vs 本窗口有多少条数据）与各环节数据量
        Map<String, Object> dict = new LinkedHashMap<>();
        dict.put("productDict", num(cnt.get("productDict")));
        dict.put("processDict", num(cnt.get("processDict")));
        dict.put("employeeDict", num(cnt.get("employeeDict")));
        dict.put("employeeUsed", num(cnt.get("employeeUsed")));
        summary.put("dict", dict);
        Map<String, Object> env = new LinkedHashMap<>();
        env.put("qcCnt", num(cnt.get("qcCnt")));
        env.put("completeCnt", num(cnt.get("completeCnt")));
        env.put("wipNum", num(cnt.get("wipNum")));
        env.put("finishedNum", num(cnt.get("finishedNum")));
        summary.put("env", env);
        data.put("summary", summary);
        data.put("nodes", nodes);
        data.put("links", links);
        data.put("note", "节点与数字均来自数据库实时统计（窗口 " + d + " 天）；关系=真实发生过的报工组合");
        log.info("[本体数据图] 产品 {} / 工序 {} / 员工 {}，关系 {} 条，窗口 {} 起", products.size(), processes.size(),
                employees.size(), links.size(), start);
        return data;
    }

    private List<Map<String, Object>> safe(List<Map<String, Object>> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private long num(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return 0L;
        }
    }

    /** 实例节点：名字/编码 + 关键数字（报工数、良品率、库存/在制） */
    private Map<String, Object> node(String id, String type, String name, String code,
                                     Map<String, Object> row, String extra) {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", id);
        n.put("kind", "instance");
        n.put("level", type);
        n.put("name", name);
        n.put("code", code);
        long pass = num(row.get("passNum"));
        long ng = num(row.get("ngNum"));
        long total = pass + ng;
        n.put("submitCnt", num(row.get("submitCnt")));
        n.put("passNum", pass);
        n.put("ngNum", ng);
        n.put("passRate", total > 0 ? Math.round(pass * 1000.0 / total) / 1000.0 : null);
        if (row.get("finishedNum") != null) {
            n.put("finishedNum", num(row.get("finishedNum")));
        }
        if (row.get("wipNum") != null) {
            n.put("wipNum", num(row.get("wipNum")));
        }
        if (row.get("productCnt") != null) {
            n.put("productCnt", num(row.get("productCnt")));
        }
        if (row.get("processCnt") != null) {
            n.put("processCnt", num(row.get("processCnt")));
        }
        return n;
    }

    private Map<String, Object> link(String from, String to, String label, Map<String, Object> row) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("from", from);
        l.put("to", to);
        l.put("label", label);
        l.put("submitCnt", num(row.get("submitCnt")));
        l.put("passNum", num(row.get("passNum")));
        l.put("ngNum", num(row.get("ngNum")));
        // 该组合"首次报工日期"：前端据此排出产品的真实工序先后顺序
        l.put("firstDay", row.get("firstDay"));
        l.put("kind", "data");
        l.put("flow", true);
        return l;
    }

    /**
     * 指标 → 依赖实体（v1 手工映射：让图上能画出"指标血缘"）。
     * 后续可从 metrics.dataSource 自动推导，届时删除本表。
     */
    /** 表名 → 实体列表（血缘推导用：实体的物理表命中指标登记的表即视为血缘） */
    private final Map<String, java.util.List<JSONObject>> entityTableIndex = new LinkedHashMap<>();

    private static final Map<String, String[]> METRIC_SOURCES = new LinkedHashMap<>();

    static {
        METRIC_SOURCES.put("SUMMARY", new String[]{"submit"});
        METRIC_SOURCES.put("PRODUCT_PASS_RATE", new String[]{"submit", "product"});
        METRIC_SOURCES.put("PROCESS_PASS_RATE", new String[]{"submit", "process"});
        METRIC_SOURCES.put("EMPLOYEE_PASS_RATE", new String[]{"submit", "employee"});
        METRIC_SOURCES.put("SUBMIT_RANK", new String[]{"submit", "employee"});
        METRIC_SOURCES.put("NG_DETAIL", new String[]{"qualityControl", "ngType"});
        METRIC_SOURCES.put("STOCK", new String[]{"stock_finished", "stock_wip"});
        METRIC_SOURCES.put("ENTITY_LIST", new String[]{"product", "process", "employee"});
        METRIC_SOURCES.put("ATTRIBUTION", new String[]{"submit", "process", "product", "employee"});
        METRIC_SOURCES.put("CONCENTRATE", new String[]{"qualityControl", "ngType", "process", "product"});
        METRIC_SOURCES.put("COMPARE", new String[]{"submit", "product", "process", "employee"});
    }

    /** 分层：主数据 / 现场动作 / 结果 / 指标 */
    private static final Map<String, String> ENTITY_LEVEL = new LinkedHashMap<>();

    static {
        ENTITY_LEVEL.put("product", "master");
        ENTITY_LEVEL.put("process", "master");
        ENTITY_LEVEL.put("employee", "master");
        ENTITY_LEVEL.put("ngType", "master");
        ENTITY_LEVEL.put("submit", "action");
        ENTITY_LEVEL.put("qualityControl", "action");
        ENTITY_LEVEL.put("completeReport", "action");
        ENTITY_LEVEL.put("stock_wip", "result");
        ENTITY_LEVEL.put("stock_finished", "result");
    }

    public Map<String, Object> graph() {
        JSONObject entityDoc = read("ontology/entities.json");
        JSONObject relationDoc = read("ontology/relations.json");
        JSONObject metricDoc = read("ontology/metrics.json");

        JSONArray entities = entityDoc == null ? new JSONArray() : entityDoc.getJSONArray("entities");
        JSONArray relations = relationDoc == null ? new JSONArray() : relationDoc.getJSONArray("relations");
        JSONArray metrics = metricDoc == null ? new JSONArray() : metricDoc.getJSONArray("metrics");

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();
        // ⚠ 成员变量索引必须每次构建前清空：否则多次调用会把实体反复追加进去，
        // 导致血缘线成倍重复（曾出现"血缘 363 条"而真实只有 20 多条）
        entityTableIndex.clear();
        List<Map<String, Object>> planningNodes = new ArrayList<>();
        List<Map<String, Object>> planningLinks = new ArrayList<>();
        LinkedHashSet<String> workshopIds = new LinkedHashSet<>();

        // 1) 实体节点
        if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
                JSONObject e = entities.getJSONObject(i);
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", e.getString("code"));
                node.put("name", e.getString("name"));
                node.put("kind", "entity");
                node.put("level", ENTITY_LEVEL.getOrDefault(e.getString("code"), "master"));
                JSONObject src = e.getJSONObject("source");
                if (src != null) {
                    node.put("table", src.getString("table"));
                    node.put("keyField", src.getString("keyField"));
                    // 建"表→实体"索引，供血缘自动推导使用
                    String table = src.getString("table");
                    if (table != null && !table.isEmpty()) {
                        entityTableIndex.computeIfAbsent(table, k -> new java.util.ArrayList<>()).add(e);
                    }
                }
                node.put("fields", e.getJSONArray("fields"));
                node.put("examples", e.getJSONArray("examples"));
                node.put("aliases", e.getJSONArray("aliases"));
                node.put("note", e.getString("note"));
                boolean workshop = !"planning".equals(e.getString("scope"));
                node.put("scope", workshop ? "workshop" : "planning");
                if (workshop) {
                    workshopIds.add(e.getString("code"));
                    nodes.add(node);
                } else {
                    planningNodes.add(node);
                }
            }
        }

        // 2) 关系连线
        if (relations != null) {
            for (int i = 0; i < relations.size(); i++) {
                JSONObject r = relations.getJSONObject(i);
                Map<String, Object> link = new LinkedHashMap<>();
                link.put("from", r.getString("from"));
                link.put("to", r.getString("to"));
                link.put("label", StringUtils.hasText(r.getString("label")) ? r.getString("label") : r.getString("via"));
                link.put("via", r.getString("via"));
                link.put("arity", r.getString("arity"));
                link.put("traversable", Boolean.TRUE.equals(r.getBoolean("traversable")));
                link.put("note", r.getString("note"));
                if ("planning".equals(r.getString("scope"))) {
                    planningLinks.add(link);
                } else {
                    links.add(link);
                }
            }
        }

        // 3) 指标 / 算子节点（含口径血缘连线）
        if (metrics != null) {
            for (int i = 0; i < metrics.size(); i++) {
                JSONObject m = metrics.getJSONObject(i);
                String code = m.getString("code");
                boolean analysis = "analysis".equalsIgnoreCase(m.getString("type"));
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", "metric:" + code);
                node.put("name", m.getString("name"));
                node.put("kind", analysis ? "operator" : "metric");
                node.put("level", "metric");
                node.put("code", code);
                node.put("formula", m.getString("formula"));
                node.put("formulaNote", m.getString("formulaNote"));
                node.put("dims", m.getJSONArray("dims"));
                node.put("examples", m.getJSONArray("questionTemplates"));
                JSONObject ds = m.getJSONObject("dataSource");
                node.put("api", ds == null ? null : ds.getString("api"));
                nodes.add(node);
                // 口径血缘：**由本体自动推导**（不再用手工映射 ✗）
                // 规则：实体 e 的物理表 ∈ 指标 m 登记的 dataSource.tables  →  血缘边 e ──▶ m
                // 好处：改本体（换数据源/新增指标）后血缘自动跟着变，不需要改 Java 代码。
                JSONArray metricTables = ds == null ? null : ds.getJSONArray("tables");
                if (metricTables != null && !metricTables.isEmpty()) {
                    for (int t = 0; t < metricTables.size(); t++) {
                        String table = metricTables.getString(t);
                        for (JSONObject ent : entityTableIndex.getOrDefault(table, java.util.Collections.emptyList())) {
                            String eid = ent.getString("code");
                            if (eid == null || !workshopIds.contains(eid)) {
                                continue;   // 计划域实体不画血缘（按既定范围）
                            }
                            Map<String, Object> lineage = new LinkedHashMap<>();
                            lineage.put("from", eid);
                            lineage.put("to", "metric:" + code);
                            lineage.put("label", "口径血缘");
                            lineage.put("kind", "lineage");
                            lineage.put("flow", false);
                            links.add(lineage);
                        }
                    }
                }
            }
        }

        // 4) 故事线（车间域两条，前端做逐节点点亮动画）
        List<Map<String, Object>> stories = new ArrayList<>();
        stories.add(story("submit_journey", "一次报工的旅程",
                new String[]{"employee", "submit", "stock_wip", "completeReport", "stock_finished"},
                "员工报工 → 审核通过 → 推进工序在制 → 尾序完工 → 形成成品库存"));
        stories.add(story("quality_trace", "一次质量归因",
                new String[]{"metric:PRODUCT_PASS_RATE", "metric:ATTRIBUTION", "process", "product", "submit"},
                "良品率下降 → 归因算子 → 定位到工序 → 下钻到产品 → 落到具体报工明细"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ontologyVersion", entityDoc == null ? null : entityDoc.getString("version"));
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("entities", nodes.size() - countMetricNodes(nodes));
        summary.put("links", links.size());
        summary.put("capabilities", metrics == null ? 0 : metrics.size());
        summary.put("planningEntities", planningNodes.size());
        data.put("summary", summary);
        data.put("nodes", nodes);
        data.put("links", links);
        data.put("stories", stories);
        data.put("planning", planning(planningNodes, planningLinks));
        data.put("note", "当前范围＝车间管理域（前端可触达）；计划域折叠展示，不投入");
        log.info("[本体图] 输出节点 {} 个（实体 {} / 能力 {}），连线 {} 条，计划域节点 {} 个",
                nodes.size(), summary.get("entities"), summary.get("capabilities"), links.size(), planningNodes.size());
        return data;
    }

    private int countMetricNodes(List<Map<String, Object>> nodes) {
        int n = 0;
        for (Map<String, Object> node : nodes) {
            if ("metric".equals(node.get("level"))) {
                n++;
            }
        }
        return n;
    }

    private Map<String, Object> planning(List<Map<String, Object>> nodes, List<Map<String, Object>> links) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("nodes", nodes);
        p.put("links", links);
        p.put("note", "计划域（订单/工单/任务）：手机端无页面、当前部署多无数据，保留设计记录但不参与可视化与 AI 提示词");
        return p;
    }

    private Map<String, Object> story(String code, String name, String[] steps, String desc) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("code", code);
        s.put("name", name);
        List<String> list = new ArrayList<>();
        for (String step : steps) {
            list.add(step);
        }
        s.put("steps", list);
        s.put("desc", desc);
        return s;
    }

    private JSONObject read(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            byte[] buf = new byte[is.available()];
            int read = is.read(buf);
            if (read <= 0) {
                return null;
            }
            return JSON.parseObject(new String(buf, 0, read, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("[本体图] 读取 {} 失败: {}", path, e.getMessage());
            return null;
        }
    }
}

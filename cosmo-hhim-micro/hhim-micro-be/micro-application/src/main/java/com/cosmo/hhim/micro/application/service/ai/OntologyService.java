/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.application.dto.ai.OntologyCapabilityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 问数 · 本体加载与投影服务
 *
 * <p>启动时加载本体（resources/ontology/metrics.json，与仓库根 ai-ontology/metrics.json 同源），
 * 执行基础校验（code 唯一 / ACTIVE 必须已实现 / 别名冲突），并向 /ai/capability 与问数编排层
 * 提供"能力清单投影"（LLM 可见的最小信息，不含口径与接口定义）。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class OntologyService {

    private static final String ONTOLOGY_FILE = "ontology/metrics.json";

    private static final String ENTITIES_FILE = "ontology/entities.json";

    private static final String RELATIONS_FILE = "ontology/relations.json";

    /** 指标缓存：code -> metric 节点（仅 ACTIVE 且已实现） */
    private final Map<String, JSONObject> metrics = new LinkedHashMap<>();

    /** 实体层缓存（车间域；计划域不注入 AI） */
    private final List<JSONObject> entities = new ArrayList<>();

    /** 关系层缓存（车间域；joinOn 作为"已登记的连接方式"供参考） */
    private final List<JSONObject> relations = new ArrayList<>();

    /**
     * 生成"业务参考"上下文（供生成 SQL 的提示词使用）。
     * <p>定位是**参考资料**，不是必须遵守的规则：优先参考，不适用时允许模型自行判断，
     * 但要求它在 purpose 里自报依据（便于回审计、发现本体缺口）。安全与正确性的硬约束由 SqlGuard 负责。
     */
    public String referenceContext() {
        StringBuilder sb = new StringBuilder();
        if (!entities.isEmpty()) {
            sb.append("【业务对象 · 本体登记（供参考）】\n");
            for (JSONObject e : entities) {
                JSONObject src = e.getJSONObject("source");
                String table = src == null ? "" : src.getString("table");
                sb.append("- ").append(e.getString("name")).append("（").append(table).append("）");
                JSONArray fields = e.getJSONArray("fields");
                if (fields != null && !fields.isEmpty()) {
                    sb.append("：");
                    for (int i = 0; i < fields.size(); i++) {
                        if (i > 0) {
                            sb.append("、");
                        }
                        // fields 是字符串数组（形如 "product_seq=产品序号"）：只取业务名（等号后）
                        Object raw = fields.get(i);
                        String text = raw == null ? "" : String.valueOf(raw);
                        if (raw instanceof JSONObject) {
                            JSONObject fo = (JSONObject) raw;
                            text = fo.getString("label") != null ? fo.getString("label") : fo.getString("key");
                        }
                        int eq = text == null ? -1 : text.indexOf('=');
                        sb.append(eq >= 0 ? text.substring(eq + 1) : text);
                    }
                }
                sb.append('\n');
            }
        }
        if (!relations.isEmpty()) {
            sb.append("\n【已登记关系 · 供参考（箭头为业务流向；括号内为已登记的连接方式）】\n");
            for (JSONObject r : relations) {
                sb.append("- ").append(nameOf(r.getString("from"))).append(' ').append(r.getString("label"))
                        .append("→ ").append(nameOf(r.getString("to")));
                String joinOn = r.getString("joinOn");
                if (StringUtils.hasText(joinOn)) {
                    sb.append("（").append(joinOn).append("）");
                }
                sb.append('\n');
            }
        }
        if (!metrics.isEmpty()) {
            sb.append("\n【已登记口径 · 供参考（优先采用，保持一致）】\n");
            for (JSONObject m : metrics.values()) {
                sb.append("- ").append(m.getString("name")).append("：").append(m.getString("formula"))
                        .append("（来源：").append(apiOf(m)).append("）\n");
            }
        }
        sb.append("\n【如何使用上面的参考资料】\n"
                + "1) 优先参考上述对象与关系来组织查询；口径优先按上面的公式算（这样与页面口径一致）。\n"
                + "2) 如果问题需要的关系或口径**不在上面**，或者上面的不适用于当前问题，"
                + "你可以依据下面的表结构自行判断如何处理——这是允许的。\n"
                + "3) 无论走哪条路，都请在 purpose 里写明：① 联结了哪些对象、依据是什么；"
                + "② 口径是自己定义的（写清分子÷分母）还是登记的；③ 若属未登记的口径/关系，标注「未登记」。\n"
                + "4) 硬性正确性（不可违反）：数字必须来自查询结果；分母为 0 时回答「无数据」，不要给 0%。\n");
        return sb.toString();
    }

    private String nameOf(String code) {
        for (JSONObject e : entities) {
            if (code != null && code.equals(e.getString("code"))) {
                return e.getString("name");
            }
        }
        return code;
    }

    private String apiOf(JSONObject m) {
        JSONObject ds = m.getJSONObject("dataSource");
        return ds == null ? "" : ds.getString("api");
    }

    private void loadEntitiesAndRelations() {
        entities.clear();
        relations.clear();
        try {
            InputStream is = new ClassPathResource(ENTITIES_FILE).getInputStream();
            JSONObject doc = JSON.parseObject(new String(readAll(is), StandardCharsets.UTF_8));
            JSONArray arr = doc.getJSONArray("entities");
            for (int i = 0; arr != null && i < arr.size(); i++) {
                JSONObject e = arr.getJSONObject(i);
                // 只注入车间域（计划域不在本期范围，避免误导模型）
                if (!"planning".equals(e.getString("scope"))) {
                    entities.add(e);
                }
            }
        } catch (Exception e) {
            log.warn("[本体] entities.json 读取失败（按无实体参考继续）: {}", e.getMessage());
        }
        try {
            InputStream is = new ClassPathResource(RELATIONS_FILE).getInputStream();
            JSONObject doc = JSON.parseObject(new String(readAll(is), StandardCharsets.UTF_8));
            JSONArray arr = doc.getJSONArray("relations");
            int traversable = 0;
            for (int i = 0; arr != null && i < arr.size(); i++) {
                JSONObject r = arr.getJSONObject(i);
                if (!"planning".equals(r.getString("scope"))) {
                    relations.add(r);
                    if (Boolean.TRUE.equals(r.getBoolean("traversable"))) {
                        traversable++;
                    }
                }
            }
            log.info("[本体] 关系层加载完成: 车间域关系 {} 条（可串联 {} 条），实体参考 {} 个",
                    relations.size(), traversable, entities.size());
        } catch (Exception e) {
            log.warn("[本体] relations.json 读取失败（按无关系参考继续）: {}", e.getMessage());
        }
    }

    @PostConstruct
    public void load() {
        metrics.clear();
        loadEntitiesAndRelations();
        try {
            InputStream is = new ClassPathResource(ONTOLOGY_FILE).getInputStream();
            String json = new String(readAll(is), StandardCharsets.UTF_8);
            JSONObject doc = JSON.parseObject(json);
            JSONArray arr = doc.getJSONArray("metrics");
            if (arr == null || arr.isEmpty()) {
                log.warn("[本体] metrics.json 无指标节点");
                return;
            }
            for (int i = 0; i < arr.size(); i++) {
                JSONObject m = arr.getJSONObject(i);
                String code = m.getString("code");
                boolean active = "ACTIVE".equals(m.getString("status"));
                boolean implemented = Boolean.TRUE.equals(m.getBoolean("implemented"));
                if (!StringUtils.hasText(code)) {
                    log.warn("[本体] 指标缺少 code，跳过: {}", m);
                    continue;
                }
                if (metrics.containsKey(code)) {
                    log.error("[本体] 指标 code 重复: {}", code);
                    throw new IllegalStateException("本体校验失败: 指标 code 重复 " + code);
                }
                if (!active || !implemented) {
                    log.info("[本体] 指标跳过（未发布/未实现）: {} status={} implemented={}", code, m.getString("status"), implemented);
                    continue;
                }
                metrics.put(code, m);
            }
            log.info("[本体] 加载完成: 版本 {}，生效指标 {} 个: {}", doc.getString("version"), metrics.size(), metrics.keySet());
        } catch (Exception e) {
            log.error("[本体] 加载失败: {}", e.getMessage(), e);
            throw new IllegalStateException("本体加载失败: " + e.getMessage(), e);
        }
    }

    /** 能力清单投影（供 /ai/capability 与注入 LLM 的能力清单使用） */
    public List<OntologyCapabilityDTO> capabilities() {
        List<OntologyCapabilityDTO> list = new ArrayList<>();
        for (JSONObject m : metrics.values()) {
            OntologyCapabilityDTO dto = new OntologyCapabilityDTO();
            dto.setCode(m.getString("code"));
            dto.setName(m.getString("name"));
            dto.setAliases(toList(m.getJSONArray("aliases")));
            dto.setDims(toList(m.getJSONArray("dims")));
            dto.setParams(toList(m.getJSONArray("llmParams")));
            List<String> examples = toList(m.getJSONArray("questionTemplates"));
            dto.setExamples(examples.size() > 3 ? examples.subList(0, 3) : examples);
            list.add(dto);
        }
        return list;
    }

    /** 快捷提问（每个指标首个问题模板，对齐前端 utils/ai-capability.ts 的 QUICK_QUESTIONS） */
    public List<String> quickQuestions() {
        return capabilities().stream()
                .map(c -> c.getExamples().isEmpty() ? null : c.getExamples().get(0))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /** 按 code 取指标节点（编排层用：意图 → 接口登记/paramMap） */
    public JSONObject metric(String code) {
        return metrics.get(code);
    }

    public int size() {
        return metrics.size();
    }

    private List<String> toList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.getString(i));
            }
        }
        return list;
    }

    /** Java 8 兼容的流读取 */
    private byte[] readAll(InputStream is) throws java.io.IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        return bos.toByteArray();
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 实体解析器（租户字典查证，防幻觉核心）
 *
 * <p>把意图中的"实体候选名"（LLM/规则从问题中提取）与本租户字典比对：
 * 命中 1 个 → 绑定规范名（返回原样名，执行层以编码/名称查询）；
 * 命中多个 → 触发 clarify（前端选项由用户选择后重新提问）；
 * 命中 0 个 → 记录未命中（交回编排层给出"未找到"话术）。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntityResolver {

    private final MicroAiDictMapper dictMapper;

    public enum Status { OK, AMBIGUOUS, NOT_FOUND, NONE }

    public static class Resolved {
        public Status status = Status.NONE;
        public String field;      // productNameOrCode / processNameOrCode / employeeName
        public String value;      // 绑定后的规范值（取命中项名称）
        public List<String> candidates = new java.util.ArrayList<>(); // AMBIGUOUS 时的候选展示名
        public String ambiguousQuestion;
    }

    /**
     * 实体键归一：LLM 偶尔用同义键（productName/product/产品…）而不给契约里的键。
     * 不归一就会出现"键不认识 → 既不校验也不生效 → 静默按全量回答"（历史上"法兰盘产量"答成全公司合计）。
     */
    private static final Map<String, String> KEY_ALIASES = new java.util.LinkedHashMap<>();

    static {
        KEY_ALIASES.put("productname", "productNameOrCode");
        KEY_ALIASES.put("productcode", "productNameOrCode");
        KEY_ALIASES.put("product", "productNameOrCode");
        KEY_ALIASES.put("productseq", "productNameOrCode");
        KEY_ALIASES.put("产品", "productNameOrCode");
        KEY_ALIASES.put("processname", "processNameOrCode");
        KEY_ALIASES.put("processcode", "processNameOrCode");
        KEY_ALIASES.put("process", "processNameOrCode");
        KEY_ALIASES.put("processseq", "processNameOrCode");
        KEY_ALIASES.put("工序", "processNameOrCode");
        KEY_ALIASES.put("employeename", "employeeName");
        KEY_ALIASES.put("username", "employeeName");
        KEY_ALIASES.put("nickname", "employeeName");
        KEY_ALIASES.put("employee", "employeeName");
        KEY_ALIASES.put("员工", "employeeName");
    }

    /**
     * 已认识的实体键（含 ngType；其余键由执行器分别消费）。
     *
     * <p>{@code metricCode} 是"控制参数"而非字典实体：分析算子（ATTRIBUTION）用它说明"要归因哪个指标"，
     * 必须原样保留；否则会被当未知键丢掉，导致归因退化到默认指标（实测踩到过）。
     */
    private static final java.util.Set<String> KNOWN_KEYS = new java.util.HashSet<>(java.util.Arrays.asList(
            "productNameOrCode", "processNameOrCode", "employeeName", "ngType", "entityType", "metricCode"));

    /** 归一实体键（原地改 intent.entities）；返回是否有键被改写/丢弃 */
    private void normalizeKeys(AskIntentResult intent) {
        Map<String, String> entities = intent.getEntities();
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> e : new java.util.ArrayList<>(entities.entrySet())) {
            String key = e.getKey();
            if (KNOWN_KEYS.contains(key)) {
                continue;
            }
            String canonical = KEY_ALIASES.get(key.toLowerCase());
            if (canonical == null) {
                log.warn("[AI缺口] type=UNKNOWN_ENTITY_KEY key={} value={}（既非契约键也非已知同义键，已忽略）",
                        key, e.getValue());
                continue;
            }
            log.info("[Agent/实体] 键归一: {} -> {} ({})", key, canonical, e.getValue());
            if (!entities.containsKey(canonical) || !StringUtils.hasText(entities.get(canonical))) {
                entities.put(canonical, e.getValue());
            }
            entities.remove(key);
        }
    }

    /** 解析指标的所有实体槽位；任一歧义即返回（首个歧义） */
    public Resolved resolveEntity(AskIntentResult intent) {
        normalizeKeys(intent);
        Resolved r = new Resolved();
        r.field = pickField(intent);
        if (r.field == null) {
            return r;
        }
        String raw = intent.getEntities().get(r.field);
        if (!StringUtils.hasText(raw)) {
            return r;
        }
        List<Map<String, Object>> rows;
        switch (r.field) {
            case "productNameOrCode":
                rows = dictMapper.selectProductByName(raw);
                break;
            case "processNameOrCode":
                rows = dictMapper.selectProcessByName(raw);
                break;
            case "employeeName":
                rows = dictMapper.selectEmployeeByName(raw);
                break;
            default:
                return r;
        }
        if (rows == null || rows.isEmpty()) {
            r.status = Status.NOT_FOUND;
            return r;
        }
        if (rows.size() > 1) {
            r.status = Status.AMBIGUOUS;
            r.ambiguousQuestion = "「" + raw + "」有多个匹配，请确认：";
            for (Map<String, Object> row : rows) {
                String name = String.valueOf(row.get(fieldNameKey(r.field)));
                if (!"null".equals(name)) {
                    r.candidates.add(name);
                }
            }
            return r;
        }
        r.status = Status.OK;
        r.value = String.valueOf(rows.get(0).get(fieldNameKey(r.field)));
        return r;
    }

    /** 取第一个已填写的实体槽位（按指标常见顺序） */
    private String pickField(AskIntentResult intent) {
        Map<String, String> entities = intent.getEntities();
        if (entities == null || entities.isEmpty()) {
            return null;
        }
        if (StringUtils.hasText(entities.get("productNameOrCode"))) {
            return "productNameOrCode";
        }
        if (StringUtils.hasText(entities.get("processNameOrCode"))) {
            return "processNameOrCode";
        }
        if (StringUtils.hasText(entities.get("employeeName"))) {
            return "employeeName";
        }
        return null;
    }

    private String fieldNameKey(String field) {
        switch (field) {
            case "productNameOrCode":
                return "productName";
            case "processNameOrCode":
                return "processName";
            case "employeeName":
                return "nickName";
            default:
                return "name";
        }
    }
}

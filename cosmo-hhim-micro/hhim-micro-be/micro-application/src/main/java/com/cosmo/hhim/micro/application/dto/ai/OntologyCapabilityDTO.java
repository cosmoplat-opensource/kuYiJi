/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 问数 · 本体能力清单 DTO（/ai/capability 返回项）
 *
 * <p>由本体（ontology/metrics.json）中 ACTIVE 且已实现的指标投影而来；
 * 对齐前端 utils/ai-capability.ts 的结构。
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class OntologyCapabilityDTO {

    /** 指标编码（如 PRODUCT_PASS_RATE） */
    private String code;

    /** 指标名称 */
    private String name;

    /** 允许的维度/粒度（组合式能力白名单：day / product / process / employee / ngType…） */
    private List<String> dims;

    /** 口语别名 */
    private List<String> aliases;

    /** LLM 可填的意图参数（如 time / productNameOrCode） */
    private List<String> params;

    /** 问题模板示例（最多取 3 个） */
    private List<String> examples;
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ai;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 问数 · 回答 VO（/ai/ask 返回结构，对齐前端 h-ask-answer 消费）
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class AskReplyVO {

    /** 答案文本（模板渲染；P1 可加 LLM 润色后校验） */
    private String answer;

    /** 命中意图（NOT_SUPPORTED=超范围） */
    private String intent;

    /** 图表跳转（对齐前端 route；无则 null） */
    private Map<String, Object> route;

    /** 三级血缘依据（metric/source/snapshot/asOf） */
    private Map<String, Object> evidence;

    /** 歧义澄清（question/options；无则 null） */
    private Map<String, Object> clarify;

    /** 是否降级（true=规则解析/边界话术） */
    private Boolean fallback;

    /** 解析/执行耗时（ms） */
    private Long elapsedMs;

    /** 解析来源：LLM | RULE | NONE */
    private String source;

    /**
     * 结果信任级别：authority=权威（登记指标 / 分析算子，与页面同源、可对账）；
     * exploratory=探索性（LLM 生成 SQL 直接得出，未经登记口径校验）。前端据此标注，双跑冲突时以 authority 为准。
     */
    private String trust;

    /** 查询参数回显（调试/审计，可选） */
    private Map<String, Object> params = new LinkedHashMap<>();
}

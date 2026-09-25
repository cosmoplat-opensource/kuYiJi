/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;

/**
 * AI 问数 · 意图解析器（可插拔）
 *
 * <p>两种实现：LlmIntentResolver（LLM 映射，默认优先）、RuleIntentResolver
 * （本体别名/模板关键词匹配，降级兜底）。编排层优先 LLM、失败回退规则。
 *
 * @author cosmo-hhim-open Team
 */
public interface IntentResolver {

    /**
     * 将用户问题解析为意图结果
     *
     * @param question 用户问题（已清洗，≤200 字符）
     * @return 意图结果（解析失败时 intent=NOT_SUPPORTED）
     */
    AskIntentResult resolve(String question);
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.micro.application.dto.ai.AskReplyVO;
import com.cosmo.hhim.micro.application.service.agent.AgentLoopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 问数 · 问数门面（/ai/ask 入口）
 *
 * <p>委托 Agent 循环（Router → Plan-Act-Reflect → 输出）执行问数；
 * 会话落库在循环内统一完成。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAskService {

    private final AgentLoopService agentLoopService;

    /**
     * 问数主入口
     *
     * @param question  用户问题（≤200 字符）
     * @param sessionId 会话（可空；非空则落库 user+ai 两条消息）
     */
    public AskReplyVO ask(String question, String sessionId) {
        return agentLoopService.run(question, sessionId);
    }
}

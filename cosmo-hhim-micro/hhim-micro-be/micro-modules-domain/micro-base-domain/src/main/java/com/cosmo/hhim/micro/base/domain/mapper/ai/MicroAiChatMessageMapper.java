/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.ai;

import com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 问数消息表 Mapper
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroAiChatMessageMapper {

    /** 新增消息 */
    int insert(MicroAiChatMessage message);

    /** 会话消息列表（按消息时间/ID 升序） */
    List<MicroAiChatMessage> selectListBySession(@Param("sessionId") String sessionId);

    /** 最近一条 AI 消息的意图（整 JSON 或旧版 code），追问槽位继承用 */
    String selectLastAiIntent(@Param("sessionId") String sessionId);

    /** 最近 N 条消息（按时间倒序返回，调用方反转），LLM 对话上下文用 */
    List<MicroAiChatMessage> selectRecentBySession(@Param("sessionId") String sessionId,
                                                   @Param("limit") int limit);
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.ai;

import com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiChatSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 问数会话表 Mapper
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroAiChatSessionMapper {

    /** 新增会话 */
    int insert(MicroAiChatSession session);

    /** 按 sessionId 查询（含软删校验由调用方处理） */
    MicroAiChatSession selectBySessionId(@Param("sessionId") String sessionId);

    /** 用户会话列表（最后消息时间倒序，仅活跃） */
    List<MicroAiChatSession> selectListByUser(@Param("tenantCode") String tenantCode, @Param("userId") Long userId);

    /** 重命名 */
    int updateTitle(@Param("sessionId") String sessionId, @Param("title") String title,
                    @Param("lastUpdBy") String lastUpdBy);

    /** 更新消息计数与最后消息时间 */
    int updateMsgInfo(@Param("sessionId") String sessionId, @Param("msgCount") Integer msgCount,
                      @Param("lastMsgTime") java.util.Date lastMsgTime, @Param("lastUpdBy") String lastUpdBy);

    /** 软删除（active_flag = 0） */
    int deleteBySessionId(@Param("sessionId") String sessionId, @Param("lastUpdBy") String lastUpdBy);
}

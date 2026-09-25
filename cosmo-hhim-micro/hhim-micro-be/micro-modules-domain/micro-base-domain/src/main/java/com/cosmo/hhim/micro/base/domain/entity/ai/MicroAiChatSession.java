/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ai;

import lombok.Data;

import java.util.Date;

/**
 * AI 问数会话表 micro_ai_chat_session
 *
 * <p>会话元信息：租户/用户/名称/消息数（上限校验用）/状态/最后消息时间。
 * 建表脚本见仓库根 hhim-ai-chat-schema.sql。
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroAiChatSession {

    private Long id;

    /** 会话唯一ID（服务端生成） */
    private String sessionId;

    /** 租户编码 */
    private String tenantCode;

    /** 所属用户ID */
    private Long userId;

    /** 会话名称（首问截断或用户重命名） */
    private String title;

    /** 消息条数（上限=20，写入时校验） */
    private Integer msgCount;

    /** 状态：10进行中 20已满 30已结束 */
    private String status;

    /** 最后消息时间（列表排序/展示） */
    private Date lastMsgTime;

    private String createdBy;

    private Date createdDate;

    private String lastUpdBy;

    private Date lastUpdDate;

    /** 激活标记1是0否（删除=0） */
    private String activeFlag;
}

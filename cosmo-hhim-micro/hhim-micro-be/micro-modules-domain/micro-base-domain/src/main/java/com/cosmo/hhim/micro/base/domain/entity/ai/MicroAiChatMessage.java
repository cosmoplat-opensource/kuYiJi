/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ai;

import lombok.Data;

import java.util.Date;

/**
 * AI 问数消息表 micro_ai_chat_message
 *
 * <p>消息明细：role/content + 答案卡片结构化字段（route/evidence/clarify）+ 埋点字段。
 * 建表脚本见仓库根 hhim-ai-chat-schema.sql。
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroAiChatMessage {

    private Long id;

    /** 会话唯一ID（关联 micro_ai_chat_session.session_id） */
    private String sessionId;

    private String tenantCode;

    /** 消息角色：user / ai / clarify */
    private String role;

    /** 消息文本 */
    private String content;

    /** 答案卡片-图表跳转（JSON 字符串） */
    private String routeJson;

    /** 答案卡片-三级血缘依据（JSON 字符串） */
    private String evidenceJson;

    /** 答案卡片-澄清选项（JSON 字符串） */
    private String clarifyJson;

    /** 命中意图编码（ai 消息，埋点用） */
    private String intent;

    /** 本次问答耗时毫秒（ai 消息） */
    private Integer elapsedMs;

    /** 消息时间（服务端记录，前端时间戳展示来源） */
    private Date msgTime;

    private String createdBy;

    private Date createdDate;
}

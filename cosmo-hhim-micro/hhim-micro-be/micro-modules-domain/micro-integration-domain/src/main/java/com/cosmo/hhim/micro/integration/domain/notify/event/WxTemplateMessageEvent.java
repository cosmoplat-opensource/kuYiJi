/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.notify.event;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.event.event.BaseEvent;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/22
 */
public abstract class WxTemplateMessageEvent extends BaseEvent { 

    // 微信模版消息列表 
    protected List<SendMessageParam> sendMessageParams;

    // 服务标识 
    protected CommonConstant.ApplicationSignEnum serviceSign;

    // 模版类别 
    protected NotifyEnums.NoticeChannelEnum templateType;

    // 消息类型 
    protected NotifyEnums.NoticeBusinessSignEnum messageType;


    public List<SendMessageParam> getSendMessageParams() {
        return sendMessageParams;
    }

    public CommonConstant.ApplicationSignEnum getServiceSign() {
        return serviceSign;
    }

    public NotifyEnums.NoticeChannelEnum getTemplateType() {
        return templateType;
    }

    public NotifyEnums.NoticeBusinessSignEnum getMessageType() {
        return messageType;
    }
}
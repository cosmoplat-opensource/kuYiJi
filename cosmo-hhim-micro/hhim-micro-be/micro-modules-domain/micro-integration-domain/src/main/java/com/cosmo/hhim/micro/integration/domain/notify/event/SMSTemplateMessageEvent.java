/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.notify.event;

import com.cosmo.hhim.common.event.event.BaseEvent;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/7
 */
public abstract class SMSTemplateMessageEvent extends BaseEvent { 

    // SMS模版消息 
    private SendSmsParam sendSmsParam;

    public SendSmsParam getSendSmsParam() {
        return sendSmsParam;
    }

    public void setSendSmsParam(SendSmsParam sendSmsParam) {
        this.sendSmsParam = sendSmsParam;
    }
}

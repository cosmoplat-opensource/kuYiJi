/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.event.EventHeader;
import com.cosmo.hhim.common.event.listener.CommonMultiListener;
import com.cosmo.hhim.micro.integration.domain.notify.event.SMSTemplateMessageEvent;
import com.cosmo.hhim.micro.integration.domain.notify.event.WeekReportMessageEvent;
import com.cosmo.hhim.thirdplat.api.cosmosupport.RemoteSmsService;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.plumelog.core.TraceId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/7
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SMSMessageListener extends CommonMultiListener { 

    private final RemoteSmsService remoteSmsService;

    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        return Arrays.asList(WeekReportMessageEvent.class);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof SMSTemplateMessageEvent) {
            SMSTemplateMessageEvent smsTemplateMessageEvent = (SMSTemplateMessageEvent) event;
            setDbAndSchema(smsTemplateMessageEvent.getEventHeader());

            // 调用三方平台发送SMS消息
            SendSmsParam sendSmsParam = smsTemplateMessageEvent.getSendSmsParam();
            APIResponse<Boolean> apiResponse = remoteSmsService.sendSms(sendSmsParam);
            if (!apiResponse.isSuccess()) {
                log.warn("SMS消息发送失败！sendMessageParam:{}", JSON.toJSONString(sendSmsParam));
            }
        }
    }

    /**
     * 设置数据源信息
     *
     * @param eventHeader
     */
    private void setDbAndSchema(EventHeader eventHeader) {
        log.info("eventHeader----->{}", JSON.toJSONString(eventHeader));
        DBControlUtil.setDbAndSchema(eventHeader.getTargetDs(), eventHeader.getTargetSchema(), eventHeader.getTargetCustomer());
        TraceId.logTraceID.set(eventHeader.getTraceId());
        ThreadContext.put(Constants.TRACEID, eventHeader.getTraceId());
    }
}

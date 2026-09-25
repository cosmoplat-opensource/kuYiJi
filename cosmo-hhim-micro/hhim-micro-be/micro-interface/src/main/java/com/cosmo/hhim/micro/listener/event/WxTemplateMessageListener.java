/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.event.EventHeader;
import com.cosmo.hhim.common.event.listener.CommonMultiListener;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatPushRecord;
import com.cosmo.hhim.micro.integration.domain.notify.event.TodayReportMessageEvent;
import com.cosmo.hhim.micro.integration.domain.notify.event.WxTemplateMessageEvent;
import com.cosmo.hhim.micro.integration.domain.service.IMicroWechatPushRecordService;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppMessageService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.constants.WechatMiniAppConstants;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
import com.plumelog.core.TraceId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WxTemplateMessageListener extends CommonMultiListener {

    private final RemoteWxMiniAppMessageService remoteWxMiniAppMessageService;
    private final IMicroWechatPushRecordService microWechatPushRecordService;

    private static final String SYSTEM_SCHEDULE_USER = "system-schedule";

    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        return Arrays.asList(TodayReportMessageEvent.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onEvent(Event event) {
        if (event instanceof WxTemplateMessageEvent) {
            WxTemplateMessageEvent wxTemplateMessageEvent = (WxTemplateMessageEvent) event;
            setDbAndSchema(wxTemplateMessageEvent.getEventHeader(), wxTemplateMessageEvent.getServiceSign().getKey());

            for (SendMessageParam sendMessageParam : wxTemplateMessageEvent.getSendMessageParams()) {
                // 存储消息发送记录
                MicroWechatPushRecord pushRecord = new MicroWechatPushRecord();
                pushRecord.setCreateBy(SYSTEM_SCHEDULE_USER);
                pushRecord.setWechatModelId(sendMessageParam.getTemplate_id());
                pushRecord.setToUsers(sendMessageParam.getTouser());
                pushRecord.setSkipUrl(sendMessageParam.getPage());
                pushRecord.setContent(JSON.toJSONString(sendMessageParam.getData()));
                pushRecord.setPushTime(DateUtils.getNowDate());
                pushRecord.setTemplateType(wxTemplateMessageEvent.getTemplateType().getCode());
                pushRecord.setServiceSign(wxTemplateMessageEvent.getServiceSign().getKey());
                pushRecord.setPushStatus(NotifyEnums.PushStatus.WAITING_PUSH.getCode());
                microWechatPushRecordService.insertMicroWechatPushRecord(pushRecord);

                // 发送微信模版消息
                boolean sendResult = this.sendMessage(sendMessageParam);

                // 更新消息发送状态
                if (null != pushRecord.getId()) {
                    MicroWechatPushRecord updateParam = new MicroWechatPushRecord();
                    updateParam.setId(pushRecord.getId());
                    updateParam.setPushStatus(NotifyEnums.PushStatus.FINISHED_PUSH.getCode());
                    updateParam.setPushResult(sendResult ? NotifyEnums.PushResult.SUCCESSED_PUSH.getCode() : NotifyEnums.PushResult.FAILED_PUSH.getCode());
                    updateParam.setPushFinishTime(DateUtils.getNowDate());
                    updateParam.setLastUpdBy(SYSTEM_SCHEDULE_USER);
                    updateParam.setLastUpdDate(DateUtils.getNowDate());
                    microWechatPushRecordService.updateMicroWechatPushRecord(updateParam);
                }
            }
        }
    }

    /**
     * 发送微信模版消息
     *
     * @return
     */
    private boolean sendMessage(SendMessageParam sendMessageParam) {
        APIResponse<WechatMiniAppConstants.SendMessageResultEnum> apiResponse = remoteWxMiniAppMessageService.sendSubscribeMessage(sendMessageParam);
        if (!apiResponse.isSuccess()) {
            log.warn("微信模版消息发送失败！sendMessageParam:{}", JSON.toJSONString(sendMessageParam));
            return false;
        }
        if (apiResponse.getData() != WechatMiniAppConstants.SendMessageResultEnum.SUCCESS) {
            log.warn("错误信息：{}, sendMessageParam:{}", apiResponse.getData().getErrorMsg(), JSON.toJSONString(sendMessageParam));
            return false;
        }
        return true;
    }


    /**
     * 设置数据源信息
     *
     * @param eventHeader
     */
    private void setDbAndSchema(EventHeader eventHeader, String appSign) {
        log.info("eventHeader----->{}", JSON.toJSONString(eventHeader));
        DBControlUtil.setDbAndSchema(eventHeader.getTargetDs(), eventHeader.getTargetSchema(), eventHeader.getTargetCustomer());
        TraceId.logTraceID.set(eventHeader.getTraceId());
        ThreadContext.put(Constants.TRACEID, eventHeader.getTraceId());

        ThreadContext.put(Constants.APPLICATION_SIGN, appSign);
        ThreadContext.put(CacheConstants.DETAILS_TYPE, CommonConstants.DETAILS_TYPE);
    }
}

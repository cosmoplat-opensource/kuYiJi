/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.notify.event;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/22
 */
public class TodayReportMessageEvent extends WxTemplateMessageEvent {

    public TodayReportMessageEvent(List<SendMessageParam> sendMessageParams, CommonConstant.ApplicationSignEnum serviceSign, NotifyEnums.NoticeChannelEnum templateType) {
        super.messageType = NotifyEnums.NoticeBusinessSignEnum.PRODUCE_TODAY_REPORT;
        super.serviceSign = serviceSign;
        super.templateType = templateType;
        super.sendMessageParams = sendMessageParams;
    }
}

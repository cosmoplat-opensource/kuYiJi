/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration;

import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.NotifyMessageContent;

import java.util.Date;

/**
 * 日报推送
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroReportFacadeService {
    /**
     * 生成生产日报消息
     *
     * @param reportDate
     * @return
     */
    NotifyMessageContent genTodayReportMessageContent(Date reportDate);

    /**
     * 生成生产周报消息
     *
     * @param reportDate
     * @return
     */
    NotifyMessageContent genWeekReportMessageContent(Date reportDate);


    /**
     * 按照推送配置推送通知消息
     *
     * @param reportDate
     * @param noticeBusinessSignEnum
     */
    void pushMessageByNoticeConfig(Date reportDate, NotifyEnums.NoticeBusinessSignEnum noticeBusinessSignEnum);
}

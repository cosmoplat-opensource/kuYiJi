/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro;

import com.cosmo.hhim.micro.application.service.integration.IMicroReportFacadeService;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/9
 */
public class NoticeTest extends BaseTest {

    @Autowired
    private IMicroReportFacadeService microReportfacadeService;

    /**
     * 生产周报-SMS推送
     */
    @Test
    public void sendSms() {
        microReportfacadeService.pushMessageByNoticeConfig(new Date(), NotifyEnums.NoticeBusinessSignEnum.PRODUCE_WEEK_REPORT);
    }

    /**
     * 生产日报-微信小程序推送
     */
    @Test
    public void sendWxMiniProgram() {
        microReportfacadeService.pushMessageByNoticeConfig(new Date(), NotifyEnums.NoticeBusinessSignEnum.PRODUCE_TODAY_REPORT);
    }

}

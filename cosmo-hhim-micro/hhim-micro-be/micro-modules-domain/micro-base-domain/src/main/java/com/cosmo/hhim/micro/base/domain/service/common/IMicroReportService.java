/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.DailyReportContentEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.WeekReportContentEntity;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/23
 */
public interface IMicroReportService {

    /**
     * 统计生产日报
     *
     * @param reportDate
     */
    DailyReportContentEntity statisticsTodayReport(Date reportDate); 

    /**
     * 统计生产周报
     * @param reportDate
     * @return
     */
    WeekReportContentEntity statisticsWeekReport(Date reportDate);


}

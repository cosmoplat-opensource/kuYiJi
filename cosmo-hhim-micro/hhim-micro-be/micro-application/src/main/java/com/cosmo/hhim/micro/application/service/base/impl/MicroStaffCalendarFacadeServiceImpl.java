/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.service.base.IMicroStaffCalendarFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.StaffWorkStatisticParam;
import com.cosmo.hhim.micro.base.domain.entity.common.StaffWorkStatisticResult;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroStaffCalendarService;
import com.cosmo.hhim.micro.complete.domain.mapper.MicroSettlementReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroStaffCalendarFacadeServiceImpl implements IMicroStaffCalendarFacadeService {

    @Autowired
    private IMicroStaffCalendarService calendarService;
    @Autowired
    private MicroSettlementReportMapper settlementReportMapper;


    @Override
    public StaffWorkStatisticResult summaryStaffStatistics(StaffWorkStatisticParam param) {
        Long employeeId = SecurityUtils.getUserId();
        StaffWorkStatisticResult result = calendarService.queryStaffWorkStatistic(param);
        BigDecimal openNum = settlementReportMapper.selectEmployeeOpenNum(employeeId);
        BigDecimal settledNum = settlementReportMapper.selectEmployeeSettledNum(param.getQueryStartDate(), employeeId);
        result.setSettledNum(settledNum);
        result.setOpenNum(openNum);
        return result;
    }
}

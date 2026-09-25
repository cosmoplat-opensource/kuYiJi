/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.base.domain.entity.common.StaffWorkStatisticParam;
import com.cosmo.hhim.micro.base.domain.entity.common.StaffWorkStatisticResult;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroStaffCalendarFacadeService {
    StaffWorkStatisticResult summaryStaffStatistics(StaffWorkStatisticParam param); 
}

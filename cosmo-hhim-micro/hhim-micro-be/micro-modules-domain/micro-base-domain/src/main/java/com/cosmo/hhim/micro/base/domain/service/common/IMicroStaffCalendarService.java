/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.common.core.web.page.PageDomain;
import com.cosmo.hhim.micro.base.domain.entity.common.*;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-03
 */
public interface IMicroStaffCalendarService {

    /**
     * 查询员工记工统计信息
     *
     * @param param
     * @return
     */
    StaffWorkStatisticResult queryStaffWorkStatistic(StaffWorkStatisticParam param);


    /**
     * 查询员工记工日历统计明细
     *
     * @param param
     * @return
     */
    List<StaffWorkCalendarDetailResult> queryStaffCalendarDetail(StaffWorkCalendarDetailParam param);


    /**
     * 按照产品种类分组查询员工工序明细信息
     *
     * @param param
     * @return
     */
    List<StaffProcessSeqInfoForProduct> queryProcessSeqDetailForProduct(PageDomain pageDomain, StaffWorkStatisticParam param); 

}

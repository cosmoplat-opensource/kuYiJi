/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-03
 */
@Data
public class StaffWorkCalendarDetailResult {

    // 日期（yyyy-MM-dd） 
    private String day;

    // 业务统计总数 
    private BigDecimal statisticNumber = BigDecimal.ZERO;

}

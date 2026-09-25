/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Data
public class MicroCompleteReportStatisticsDayResult {

    // 日期 
    private String statDate;

    // 完工总数 
    private BigDecimal completeTotalNum;
}

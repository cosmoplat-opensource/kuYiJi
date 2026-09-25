/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class DailyReportContentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 生产产品数 
    private Long productTotalNum;

    // 在产数量 
    private BigDecimal inProductNum;

    // 记工人数 
    private Long workerNum;

    // 审核进度 
    private BigDecimal checkProgress;

    // 不良品数 
    private BigDecimal rejectProductNum;

}
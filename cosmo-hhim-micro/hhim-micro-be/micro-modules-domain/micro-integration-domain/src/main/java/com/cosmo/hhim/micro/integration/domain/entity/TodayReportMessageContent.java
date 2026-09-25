/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/22
 */
@Data
public class TodayReportMessageContent extends NotifyMessageContent {

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

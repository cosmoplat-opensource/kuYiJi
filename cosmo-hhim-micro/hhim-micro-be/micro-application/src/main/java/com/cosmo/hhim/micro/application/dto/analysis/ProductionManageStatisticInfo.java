/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 工易派 - 生产管理统计信息
 * @date 2023/5/5 14:19
 */
@Data
public class ProductionManageStatisticInfo {

    /**
     * 待排产的生产订单数量
     */
    private int waitingPeriodOrderNum = 0;

    /**
     * 待生产工单数量
     */
    private int waitingPeriodWorkOrderNum = 0;

    /**
     * 待入库数量
     */
    private BigDecimal waitingInBoundNum = BigDecimal.ZERO;
}

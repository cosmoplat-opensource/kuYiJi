/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.analysis;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 工易派预警统计信息
 * @date 2023/5/5 13:03
 */
@Data
public class WarnStatisticInfo {

    /**
     * 逾期的工单数
     */
    private int overDateWorkOrderNum = 0;

    /**
     * 库存预警的产品数
     */
    private int overSafetyStockProductNum = 0;

    /**
     * 有警示标示的工单
     */
    private int warnOrderNum = 0;
}

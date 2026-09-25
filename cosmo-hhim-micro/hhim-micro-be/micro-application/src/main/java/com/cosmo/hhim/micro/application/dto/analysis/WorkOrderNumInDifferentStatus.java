/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.analysis;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 不同状态工单数量统计
 * @date 2023/5/4 17:56
 */
@Data
public class WorkOrderNumInDifferentStatus {

    /**
     * 待生产数量
     */
    private Long waitingPeriodOrderNum = 0L;

    /**
     * 生产中工单数量
     */
    private Long inProductionOrderNum = 0L;

    /**
     * 逾期工单数量
     */
    private Long overDateOrderNum = 0L;

    /**
     * 已结束工单数量
     */
    private Long terminalOrderNum = 0L;

    /**
     * 总的工单数量
     */
    private Long totalOrderNum = 0L;
}

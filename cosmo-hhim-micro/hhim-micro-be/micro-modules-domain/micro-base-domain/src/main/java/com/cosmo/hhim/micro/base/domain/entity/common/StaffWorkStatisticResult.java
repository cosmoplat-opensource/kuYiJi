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
public class StaffWorkStatisticResult {

    /**
     * 已结算数量
     */
    private BigDecimal settledNum;
    /**
     * 未结算数量
     */
    private BigDecimal openNum;

    // 产品种类总数 
    private Long productSum = 0L;

    // 工单数量 
    private Long workOrderNum = 0L;

    // 记工总数 
    private BigDecimal workSubmitSum = BigDecimal.ZERO;

    // 待审总数 
    private BigDecimal waitCheckSum = BigDecimal.ZERO;

    /**
     * 驳回待处理数量
     */
    private BigDecimal rejectSum = BigDecimal.ZERO;

    /**
     * 驳回的报工记录条数
     */
    private Long rejectRecordNum = 0L;

    // 出勤天数 
    private Long workDays = 0L;

    // 平均日产能 
    private BigDecimal averageCapacityOfDay = BigDecimal.ZERO;

    // 平均良品率 
    private BigDecimal averageGoodProductRatioOfDay = BigDecimal.ZERO;
}

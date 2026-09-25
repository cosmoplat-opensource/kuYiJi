/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 生产订单详情页面的工单信息
 * @date 2023/5/10 11:19
 */
@Data
public class WorkOrderInfoFromDetailOrder {

    private Long id;

    /**
     * 工单号
     */
    private String workOrderNo;

    private String productSeq;

    private String productCode;

    private String productName;

    private String unit;

    /**
     * 计划数量
     */
    private BigDecimal planNum;

    /**
     * 已产数量
     */
    private BigDecimal finishNum;

    /**
     * 待生产数量
     */
    private BigDecimal waitProduceNum;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;

    /**
     * 工单状态
     */
    private String workOrderStatus;

    /**
     * 主工单标示
     */
    private String mainFlag;

    /**
     * 是否完工 0是，1否
     */
    private String isComplete;

    /**
     * 工单警示标示
     */
    private String workOrderWarnFlag;
}

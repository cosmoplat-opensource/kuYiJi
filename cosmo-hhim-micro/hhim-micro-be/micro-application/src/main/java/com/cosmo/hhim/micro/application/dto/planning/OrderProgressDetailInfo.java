/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 订单进度追踪
 * @date 2023/6/8 13:22
 */
@Data
public class OrderProgressDetailInfo {

    private String orderNo;

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    /** 客户编码 */
    private String owCode;

    /** 客户名称 */
    private String owName;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 交付日期
     */
    private Date deliveryDate;

    /**
     * 订单计划数量
     */
    private BigDecimal orderPlanNum;

    /**
     * 主工单号
     */
    private String workOrderNo;

    /**
     * 生产任务集合
     */
    private List<MicroManufactureTaskDto> taskDtoList;
}

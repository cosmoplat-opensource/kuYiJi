/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import com.cosmo.hhim.micro.base.domain.entity.custom.SimpleExtendFieldInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 生产订单详情信息
 * @date 2023/5/10 11:10
 */
@Data
public class DetailMicroManufactureOrderInfo {

    private Long id;

    private String orderNo;

    private String productSeq;

    private String productCode;

    private String productName;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 交付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deliverDate;

    /**
     * 计划数量
     */
    private BigDecimal planNum;

    /**
     * 生产订单状态
     */
    private String orderStatus;

    /**
     * 之前状态 - 用于关单重启
     */
    private String beforeStatus;

    /**
     * 生产订单警示标示
     */
    private String orderWarnFlag;

    /**
     * 自定义字段信息
     */
    private List<SimpleExtendFieldInfo> extendFieldInfoList;

    /**
     * 工单信息
     */
    private List<WorkOrderInfoFromDetailOrder> workOrderList;
}

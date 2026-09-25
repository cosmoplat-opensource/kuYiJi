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
 * @description: 工单详情信息
 * @date 2023/5/8 15:07
 */
@Data
public class DetailManufactureWorkOrderInfo {

    private Long id;

    /**
     * 工单号
     */
    private String orderNo;

    private String workOrderNo;

    private String productCode;

    private String productName;

    private String productUnit;

    private String workOrderStatus;

    /**
     * 计划数量
     */
    private BigDecimal planNum;

    /**
     * 车间名称
     */
    private String wshopName;

    /**
     * 产线名称
     */
    private String mlineName;

    /** 计划开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;

    /** 计划完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;

    /** 扩展字段 */
    private String extendContent;

    /**
     * 之前状态 - 用于关单重启
     */
    private String beforeStatus;

    /**
     * 主工单标示
     */
    private String mainFlag;

    /**
     * 是否完工标示
     *
     * 0-是，1-否
     */
    private String isComplete;

    /**
     * 工单警示标示
     */
    private String workOrderWarnFlag;

    /**
     * 生产任务进度
     */
    List<MicroManufactureTaskProcessInfo> microManufactureTaskProcessInfoList;

    /**
     * 自定义字段信息
     */
    private List<SimpleExtendFieldInfo> extendFieldInfoList;
}

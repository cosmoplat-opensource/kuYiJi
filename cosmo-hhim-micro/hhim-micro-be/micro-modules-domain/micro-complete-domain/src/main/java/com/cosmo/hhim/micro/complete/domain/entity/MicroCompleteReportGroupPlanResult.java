/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class MicroCompleteReportGroupPlanResult {

    // 工单ID 
    private Long workOrderId;

    /** 工单号 */
    private String workOrderNo;

    /** 生产订单号 */
    private String orderNo;

    /** 产品唯一编码 */
    private String productSeq;

    /** 产品编码 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 车间编码 */
    private String wshopCode;

    /** 车间名称 */
    private String wshopName;

    /** 生产线编码 */
    private String mlineCode;

    /** 生产线名称 */
    private String mlineName;

    /** 计划开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planStartDate;

    /** 计划完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planEndDate;

    /** 生产开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date produceStartDate;

    /** 生产完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date produceEndDate;

    // 完工数量 
    private BigDecimal completeNum;

    // 完工时间 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

    // 完工操作人ID 
    private Long completeUser;

    // 完工操作人昵称 
    private String completeUserNickName;

    // 完工报告单ID 
    private Long completeReportId;

    // 产品单位 
    private String unit;

}

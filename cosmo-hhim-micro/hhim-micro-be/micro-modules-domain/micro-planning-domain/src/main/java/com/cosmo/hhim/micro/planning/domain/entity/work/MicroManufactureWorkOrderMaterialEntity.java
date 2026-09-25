/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.entity.work;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 工单缺料实体
 *
 * @date 2023-03-06
 */
@Data
public class MicroManufactureWorkOrderMaterialEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 工单号
     */
    private String workOrderNo;

    private Long productId;
    /**
     * 产品唯一编码
     */
    private String productSeq;


    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;
    private String productUnit;


    /**
     * 工单数量
     */
    private BigDecimal workOrderNum;

    /**
     * 计划开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;

    /**
     * 计划完工时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;
    /**
     * 需求数量
     */
    private BigDecimal demandNum;
    /**
     * 库存数量
     */
    private BigDecimal stockNum;
    /**
     * 缺料数量
     */
    private BigDecimal shortageNum;

}

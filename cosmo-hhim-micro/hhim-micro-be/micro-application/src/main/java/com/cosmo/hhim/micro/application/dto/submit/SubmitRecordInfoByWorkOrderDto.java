/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.submit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 工易派 - 工单维度审核列表展示信息
 * @date 2023/3/23 10:10
 */
@Data
public class SubmitRecordInfoByWorkOrderDto {

    /**
     * 工单号
     */
    private String workOrderNo;

    private String productSeq;

    private String productCode;

    private String productName;

    /**
     * 产品单位
     */
    private String productUnit;

    /** 车间编码 */
    private String wshopCode;

    /** 车间名称 */
    private String wshopName;

    /** 生产线编码 */
    private String mlineCode;

    /** 生产线名称 */
    private String mlineName;

    /** 工单数量 */
    private BigDecimal workOrderNum;

    /**
     * 最后一道工序报工数
     */
    private BigDecimal passNum;

    /**
     * 记工数量
     */
    private int recordNum;

    /**
     * 工序数量
     */
    private int processNum;

    /**
     * 报工记录id汇总
     */
    private String ids;
}

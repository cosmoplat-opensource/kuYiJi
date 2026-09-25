/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 基于产品的工序纬度生产报工数据
 * @date 2023/6/5 10:59
 */
@Data
public class ProductiveSubmitAnalysisByProcessBaseProduct {

    private String productName;

    private String productSeq;

    private String productCode;

    private String productUnit;

    private String processSeq;

    private String processCode;

    private String processName;

    private Long submitStatus;

    /**
     * 以submitStatus用于区分未审核良品和已审核良品
     */
    private BigDecimal passNum;

    /**
     * 以submitStatus用于区分未审核不良品和已审核不良品
     */
    private BigDecimal ngNum;
}

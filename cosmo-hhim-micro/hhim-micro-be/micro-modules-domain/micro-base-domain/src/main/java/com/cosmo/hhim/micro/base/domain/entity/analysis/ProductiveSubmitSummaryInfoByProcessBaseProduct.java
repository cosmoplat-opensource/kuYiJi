/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 基于产品的工序纬度生产报工汇总数据
 * @date 2023/6/5 11:10
 */
@Data
public class ProductiveSubmitSummaryInfoByProcessBaseProduct {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private String processSeq;

    private String processCode;

    private String processName;

    private BigDecimal passNum  = BigDecimal.ZERO;

    private BigDecimal ngNum = BigDecimal.ZERO;

    private BigDecimal checkPassNum = BigDecimal.ZERO;

    private BigDecimal checkNgNum = BigDecimal.ZERO;

    private BigDecimal totalNum = BigDecimal.ZERO;
}

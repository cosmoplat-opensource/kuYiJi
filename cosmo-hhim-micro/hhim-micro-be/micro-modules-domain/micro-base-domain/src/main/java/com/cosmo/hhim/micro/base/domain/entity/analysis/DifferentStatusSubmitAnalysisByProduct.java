/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品维度不同状态下的报工数量，用于分页
 * @date 2023/6/7 15:58
 */
@Data
public class DifferentStatusSubmitAnalysisByProduct {

    private String productSeq;

    private Long submitStatus;

    private BigDecimal passNum;

    private BigDecimal ngNum;
}

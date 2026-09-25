/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品维度生产报工分析 - 工易派
 * @date 2023/6/2 17:00
 */
@Data
public class ProductiveSubmitAnalysisByProduct {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private BigDecimal totalNum;

    List<DifferentStatusSubmitAnalysisByProduct> differentStatusSubmitAnalysisByProductList;
}

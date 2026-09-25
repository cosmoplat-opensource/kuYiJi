/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 员工维度 - 具体员工的报工信息
 * @date 2023/6/5 15:23
 */
@Data
public class ProductiveSubmitAnalysisByProductAndProcessBaseUser {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private String processSeq;

    private String processCode;

    private String processName;

    private BigDecimal passNum;

    private BigDecimal ngNum;
}

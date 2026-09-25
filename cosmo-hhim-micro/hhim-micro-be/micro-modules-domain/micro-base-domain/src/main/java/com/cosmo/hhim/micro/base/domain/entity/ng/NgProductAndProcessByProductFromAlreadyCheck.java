/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品 + 工序维度的不良数量
 * @date 2023/4/4 16:11
 */
@Data
public class NgProductAndProcessByProductFromAlreadyCheck {

    private String productSeq;

    private String processSeq;

    private String processName;

    private BigDecimal ngNum;
}

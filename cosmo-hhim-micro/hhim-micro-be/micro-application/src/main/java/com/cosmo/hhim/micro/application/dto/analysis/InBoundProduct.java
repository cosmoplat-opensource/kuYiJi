/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 入库产品
 * @date 2023/6/5 18:15
 */
@Data
public class InBoundProduct {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private BigDecimal inboundNum;
}

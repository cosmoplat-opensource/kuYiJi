/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品 + 工序的实体对象（良品 + 不良品）
 * @date 2023/2/21 14:23
 */
@Data
public class RecordForProductAndProcess {

    private String productCode;

    private String productName;

    private String productSeq;

    private String processSeq;

    private String processName;

    private BigDecimal passNum;

    private BigDecimal ngNum;
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品到工序层面的数量实体
 * @date 2023/2/13 13:58
 */
@Data
public class NgProductAndProcess {

    private String productSeq;

    private String productName;

    private String productCode;

    private String processSeq;

    private String processName;

    private BigDecimal ngNum;
}

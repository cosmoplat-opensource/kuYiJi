/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 良品中产品+工序的实体对象
 * @date /2/21 13:31
 */
@Data
public class PassProductAndProcess {

    private String productSeq;

    private String productName;

    private String productCode;

    private String processSeq;

    private String processName;

    /**
     * 良品数
     */
    private BigDecimal passNum;
}

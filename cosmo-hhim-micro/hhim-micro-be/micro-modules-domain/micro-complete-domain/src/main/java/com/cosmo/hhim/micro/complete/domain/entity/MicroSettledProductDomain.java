/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品维度-已结算清单
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettledProductDomain implements Serializable {
    private String productSeq;
    private String productCode;
    private String productName;
    private String productUnit;
    /**
     * 工序数量
     */
    private Long processNum;
    /**
     * 员工数量
     */
    private Long employeeNum;
    /**
     * 完工数量
     */
    private BigDecimal storageNum;

}

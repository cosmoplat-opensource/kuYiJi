/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 审产后待结算实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettlementGenerateDomain implements Serializable {
    /**
     * 产品序列码
     */
    private String productSeq;
    private String productName;
    private String productCode;
    private String productUnit;

    /**
     * 工序序列码
     */
    private String operateProcessSeq;
    private String operateProcessCode;
    private String operateProcessName;
    /**
     * 审核后数量
     */
    private BigDecimal checkedNum;
    /**
     * 报工ID
     */
    private Long submitId;
    /**
     * 结算员工ID
     */
    private Long employeeId;
}

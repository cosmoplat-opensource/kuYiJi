/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettlementReportEmployeeDomain implements Serializable {
    /**
     * 员工ID
     */
    private Long employeeId;
    private String employeeName;
    private String employeeUserName;
    /**
     * 可调整总数
     */
    private BigDecimal totalAdjustedNum;
    private BigDecimal totalSettledNum;
}

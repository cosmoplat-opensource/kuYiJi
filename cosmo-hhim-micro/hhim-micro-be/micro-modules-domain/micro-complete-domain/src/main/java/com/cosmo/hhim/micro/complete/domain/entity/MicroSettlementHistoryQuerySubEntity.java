/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计件结算调整历史对象 micro_settlement_history
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Data
public class MicroSettlementHistoryQuerySubEntity implements Serializable {
    private Long employeeId;
    private String productSeq;
    private String productCode;
    private String productName;
    private String productUnit;
    private String operateProcessSeq;
    private String operateProcessCode;
    private String operateProcessName;
    private BigDecimal totalAdjustedNum;

}

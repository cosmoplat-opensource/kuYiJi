/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import lombok.Data;

import java.io.Serializable;

/**
 * 计件结算调整历史对象 micro_settlement_history
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Data
public class MicroSettlementHistorySubDTO implements Serializable {
    private String productSeq;
    private String productCode;
    private String productName;
    private String productUnit;
    private String operateProcessSeq;
    private String operateProcessCode;
    private String operateProcessName;
    private String totalAdjustedNum;

}

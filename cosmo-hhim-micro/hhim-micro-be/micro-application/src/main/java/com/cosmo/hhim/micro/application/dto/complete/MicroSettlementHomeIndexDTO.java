/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettlementHomeIndexDTO implements Serializable {
    /**
     * 待结算数量
     */
    private BigDecimal openSettlementNum;
    /**
     * 设计产品数量
     */
    private Long productNum;

}

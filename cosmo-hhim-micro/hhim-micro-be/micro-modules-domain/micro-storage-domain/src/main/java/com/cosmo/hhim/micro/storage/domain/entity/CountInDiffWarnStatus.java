/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 预警列表不同状态的产品数
 * @date 2023/5/9 18:12
 */
@Data
public class CountInDiffWarnStatus {

    private BigDecimal negativeStockFlagNum;

    private BigDecimal lowerSafetyStockFlagNum;

    private BigDecimal upperSafetyStockFlagNum;
}

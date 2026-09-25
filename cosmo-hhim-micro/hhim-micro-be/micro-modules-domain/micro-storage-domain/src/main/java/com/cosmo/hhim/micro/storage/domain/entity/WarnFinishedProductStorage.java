/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 库存预警信息
 * @date 2023/5/6 11:11
 */
@Data
public class WarnFinishedProductStorage {

    private String productCode;

    private String productName;

    private String productSeq;

    /**
     * 安全库存范围下限
     */
    private BigDecimal stockLowerLimit;

    /**
     * 安全库存范围上限
     */
    private BigDecimal stockUpperLimit;

    /**
     * 库存
     */
    private BigDecimal stockNum;

    /**
     * 预警标示
     */
    private String stockWarnFlag;
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 库存总数
 * @date 2023/3/2 09:21
 */
@Data
public class TotalStock {

    /**
     * 总的良品数
     */
    private BigDecimal totalPassNum;

    /**
     * 总的不良品数
     */
    private BigDecimal totalNgNum;
}

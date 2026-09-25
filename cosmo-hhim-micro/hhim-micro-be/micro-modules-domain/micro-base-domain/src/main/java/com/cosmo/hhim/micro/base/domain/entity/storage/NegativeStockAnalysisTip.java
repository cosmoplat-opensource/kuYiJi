/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author cosmo-hhim-open Team
 * @description: 负库存分析提示
 * @date 2023/2/20 16:42
 */
@Data
public class NegativeStockAnalysisTip {

    /**
     * 提示语
     */
    private String message;

    /**
     * 异常出现时间
     */
    private LocalDate occurredDate;
}

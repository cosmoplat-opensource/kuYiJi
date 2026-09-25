/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/27
 */
@Data
public class StorageChangedIndexResult {

    // 入库产品款数 
    private Long inBoundProductCategoryNum;

    // 出库产品款数 
    private Long outBoundProductCategoryNum;


    // 入库数 
    private BigDecimal inBoundNum;

    // 出库数 
    private BigDecimal outBoundNum;
}

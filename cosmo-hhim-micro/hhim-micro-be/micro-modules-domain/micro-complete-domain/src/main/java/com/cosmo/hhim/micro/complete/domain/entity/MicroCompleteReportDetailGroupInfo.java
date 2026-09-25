/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class MicroCompleteReportDetailGroupInfo {
    // 产品编码 
    private String productSeq;

    // 产品名称 
    private String productName;

    // 完工数量 
    private BigDecimal completeTotalNum;
}

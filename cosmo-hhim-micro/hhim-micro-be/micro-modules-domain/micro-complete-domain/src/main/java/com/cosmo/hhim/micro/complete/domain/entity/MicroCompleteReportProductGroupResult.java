/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class MicroCompleteReportProductGroupResult {

    // 产品唯一码 
    private String productSeq;

    // 产品编码 
    private String productCode;

    // 产品名称 
    private String productName;

    // 完工数量 
    private BigDecimal completeTotalNum;

}

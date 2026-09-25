/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品 + 工序的数量
 * @date 2023/1/3 17:07
 */
@Data
public class CountForProductAndProcess {

    /**
     * productSeq + processSeq
     */
    private String seqKey;

    /**
     * 总的数量 （良品 + 不良品）
     */
    private BigDecimal totalNum;

    /**
     * 总良品数量
     */
    private BigDecimal totalPassNum;

    /**
     * 总不良品数量
     */
    private BigDecimal totalNgNum;
}

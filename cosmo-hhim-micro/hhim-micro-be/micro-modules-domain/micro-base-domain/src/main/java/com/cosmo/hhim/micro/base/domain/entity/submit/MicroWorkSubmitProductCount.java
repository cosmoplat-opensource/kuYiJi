/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工数量
 * @date 2022/10/18 10:30 上午
 */
@Data
public class MicroWorkSubmitProductCount {

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 工序序列码
     */
    private String processSeq;

    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 总的报工产品数量
     */
    private BigDecimal totalCounts;

    /**
     * 良品数量
     */
    private BigDecimal totalPassNum;

    /**
     * 不良品数量
     */
    private BigDecimal totalNgNum;

    /**
     * 良品率
     */
    private BigDecimal passRate;

    /**
     * 不良品率
     */
    private BigDecimal ngRate;

    /**
     * 报工日
     */
    private String submitDay;
}

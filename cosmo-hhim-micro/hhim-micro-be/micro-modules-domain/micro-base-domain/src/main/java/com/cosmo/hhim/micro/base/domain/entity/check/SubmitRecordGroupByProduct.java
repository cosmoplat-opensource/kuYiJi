/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品维度审核的展示对象
 * @date 2023/2/8 10:10
 */
@Data
public class SubmitRecordGroupByProduct {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品维度汇总之后的报工记录id
     */
    private String ids;

    /**
     * 工序数量
     */
    private Integer processCount;

    /**
     * 报工记录数
     */
    private Integer recordCount;

    /**
     * 总的报工数量
     */
    private BigDecimal totalNum;

    /**
     * 警示标示符号
     */
    private String warnFlag;

    /**
     * 是否有标准工艺
     */
    private boolean standard;

    private String productUnit;

    /**
     * 前工序和当前工序合并
     */
    private String processSeq;
}
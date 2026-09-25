/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroProductEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品描述
     */
    private String productDesc;

    /**
     * 产品类型(成品、半成品)
     */
    private String productType;
    /**
     * 生产方式(10自制20外购30委外)
     */
    private String productionMode;

    /**
     * 产品图片
     */
    private String picture;

    /**
     * 单位
     */
    private String unit;

    /**
     * 规格
     */
    private String standards;

    private String remark;

    /**
     * 安全库存上限
     */
    private BigDecimal stockUpperLimit;
    /**
     * 安全库存下限
     */
    private BigDecimal stockLowerLimit;


}

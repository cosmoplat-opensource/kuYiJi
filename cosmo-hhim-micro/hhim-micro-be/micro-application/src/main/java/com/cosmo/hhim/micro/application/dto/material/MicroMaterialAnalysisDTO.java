/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.material;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 物料需求分析传输实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroMaterialAnalysisDTO implements Serializable {

    private Long productId;
    private String productSeq;
    private String productCode;
    private String productName;
    private String productUnit;
    /**
     * 需求数量
     */
    private BigDecimal demandNum;
    /**
     * 库存数量
     */
    private BigDecimal stockNum;
    /**
     * 缺料数量
     */
    private BigDecimal shortageNum;

}


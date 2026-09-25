/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

import java.util.List;

/**
 * 用于对比相同工艺链是否还挂载其他产品
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroTechDiffDomain {
    private Long techId;
    private Long productId;
    private String productCode;
    private String productSeq;
    private String productName;
    private List<String> chainStrArray;
}

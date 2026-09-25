/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSingleTechChainEntity implements Serializable {
    private Long techId;
    private Long productId;
    private String productSeq;
    private String productName;
    private String techType;
    private Integer techPattern;
    private String techName;
    List<MicroSaveChainNodeEntity> processChainList;

}

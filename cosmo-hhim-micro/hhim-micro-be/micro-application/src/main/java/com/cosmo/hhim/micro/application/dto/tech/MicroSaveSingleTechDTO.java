/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.tech;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSaveSingleTechDTO {
    private Long productId;
    private String productSeq;
    private String productName;
    private String techType;
    private String techPattern;
    List<MicroSaveSingleChainDTO> processChainList;

}

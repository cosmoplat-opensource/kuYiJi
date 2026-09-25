/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroRecommendParam implements Serializable {
    private String productSeq;
    private String operateProcessSeq;
    private String preProcessSeq;
    private Boolean standard;
}

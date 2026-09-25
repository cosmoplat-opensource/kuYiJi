/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroProcessChainRecommendEntity implements Serializable {
    private String productSeq;
    private String operateProcessSeq;
    private String operateProcessName;
    private String operateProcessCode;
    private String preProcessSeq;
    private String preProcessName;
    private String preProcessCode;
}

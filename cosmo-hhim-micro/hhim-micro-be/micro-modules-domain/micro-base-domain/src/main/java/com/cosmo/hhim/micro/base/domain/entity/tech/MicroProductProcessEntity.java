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
public class MicroProductProcessEntity implements Serializable {

    private String productSeq;

    private String operateProcessSeq;

    private Long preProcessId;
    private String preProcessSeq;
    private String preProcessCode;
    private String preProcessName;

    /**
     * 是否为首序
     */
    private Boolean isFirstProcess = false;

    /**
     * 是否为尾序
     */
    private Boolean isLastProcess = false;

}

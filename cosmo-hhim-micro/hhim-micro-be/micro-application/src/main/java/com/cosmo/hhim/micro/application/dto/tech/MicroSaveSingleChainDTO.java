/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.tech;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSaveSingleChainDTO implements Serializable {
    private Long processId;
    private String processSeq;
    private String processName;
    private String processCode;
    /**
     * 0是1否
     */
    private String isLastProcess;

    /**
     * 首序 0是1否
     */
    private String isFirstProcess;
}
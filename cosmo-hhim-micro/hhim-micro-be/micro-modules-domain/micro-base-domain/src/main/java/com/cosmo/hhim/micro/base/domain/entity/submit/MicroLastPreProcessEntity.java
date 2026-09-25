/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroLastPreProcessEntity implements Serializable {
    private String productAndProcess;
    private String productSeq;
    private String operateProcessSeq;
    private Long preProcessId;
    private String preProcessSeq;
    private String preProcessCode;
    private String preProcessName;
    private Boolean isLastProcess = false;
    private Boolean isFirstProcess = false;

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProcessMixedSelectDTO implements Serializable {

    private String searchKey;
    private String productSeq;
    private String productCode;
    private String productName;
    private String operateProcessSeq;
    private String operateProcessCode;
    private String operateProcessName;
    private Boolean standard;
}

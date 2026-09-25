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
public class MicroTechChainResult implements Serializable {
    /**
     * 是否标准工艺
     */
    private Boolean standard;
    /**
     * 工艺形式
     */
    private Integer techPattern;
    /**
     * 工艺信息
     */
    private Object techData;
}

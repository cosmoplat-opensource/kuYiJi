/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroTechBindCondition {
    /**
     * 是否是标准工艺
     */
    private Boolean standard;
    /**
     * 是否为拷贝工艺
     */
    private Boolean clone = true;
}

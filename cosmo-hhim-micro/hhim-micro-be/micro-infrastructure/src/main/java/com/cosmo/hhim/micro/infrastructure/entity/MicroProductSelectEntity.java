/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 选择实体
 *
 * @author cosmo-hhim-open Team
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MicroProductSelectEntity extends MicroSelectEntity {
    /**
     * 节点序列号
     */
    private String itemUnit;
    /**
     * 是否标准
     */
    private Boolean standard;
}

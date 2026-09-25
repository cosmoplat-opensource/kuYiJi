/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 * 选择实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MicroSelectEntity extends MicroSelectBaseEntity implements Serializable {
    /**
     * 节点序列号
     */
    private String itemSeq;
    private Long itemId;
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 选择实体
 *
 * @author cosmo-hhim-open Team
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MicroExistEntity extends MicroSelectEntity {
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private String existItem;
}

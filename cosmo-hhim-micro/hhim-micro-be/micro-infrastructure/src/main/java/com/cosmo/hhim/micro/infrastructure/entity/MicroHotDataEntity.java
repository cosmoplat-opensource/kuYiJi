/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 选择实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroHotDataEntity implements Serializable {
    private Long score;
    private MicroSelectEntity value;
}

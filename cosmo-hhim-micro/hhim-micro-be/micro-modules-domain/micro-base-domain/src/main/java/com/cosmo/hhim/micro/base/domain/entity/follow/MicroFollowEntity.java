/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.follow;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroFollowEntity implements Serializable {
    private Long id;
    private Long selfId;
    private Long followId;
    private String followType;

    private String tenantCode;
}

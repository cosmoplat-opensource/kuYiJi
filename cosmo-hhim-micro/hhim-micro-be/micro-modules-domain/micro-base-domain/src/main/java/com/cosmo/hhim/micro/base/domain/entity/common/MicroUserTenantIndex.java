/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户-租户索引对象 micro_user_tenant_index
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroUserTenantIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 当前租户下的 micro_user.id
     */
    private Long localUserId;

    /**
     * 是否主租户 1=是 0=否
     */
    private Integer isPrimary;

    /**
     * 创建时间
     */
    private Date createdAt;
}

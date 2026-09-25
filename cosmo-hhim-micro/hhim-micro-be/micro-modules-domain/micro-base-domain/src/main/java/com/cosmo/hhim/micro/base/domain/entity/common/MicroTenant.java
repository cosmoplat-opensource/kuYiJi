/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 租户元数据对象 micro_tenant
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroTenant implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 租户编码（主键）
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 状态 1=启用 0=停用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

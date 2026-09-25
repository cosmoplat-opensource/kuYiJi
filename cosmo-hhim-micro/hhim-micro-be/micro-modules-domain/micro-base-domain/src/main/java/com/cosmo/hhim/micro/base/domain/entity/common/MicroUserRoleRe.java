/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 【请填写功能名称】对象 micro_user_role_re
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-22
 */
@Data
public class MicroUserRoleRe implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 用户ID
     */
    private Long userId;
}

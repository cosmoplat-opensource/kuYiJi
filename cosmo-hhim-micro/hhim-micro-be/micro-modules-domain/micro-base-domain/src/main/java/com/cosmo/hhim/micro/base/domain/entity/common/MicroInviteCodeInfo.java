/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/22
 */
@Data
public class MicroInviteCodeInfo {

    // 租户编码 
    private String customerCode;

    // 数据库schema 
    private String databaseName;

    // 数据源 
    private String dataSource;

    // 角色编码 
    private RoleCodeEnum roleCode;

    // 有效期（单位：min） 
    private Integer expireTime;

    // 应用编码 
    private String appCode;
}

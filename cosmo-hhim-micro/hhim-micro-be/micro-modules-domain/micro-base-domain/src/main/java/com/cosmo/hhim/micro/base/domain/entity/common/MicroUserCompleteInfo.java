/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-22
 */
@Data
public class MicroUserCompleteInfo {

    // 租户编码 
    private String customer;

    // 租户名称 
    private String customerName;

    // 数据库schema 
    private String schema;

    // 数据源 
    private String dataSource;

    // 登录时间 
    private Long loginTime;

    // 登录类型 
    private String loginType;

    // 过期时间 
    private Long expireTime;

    // token 
    private String token;

    // 用户基本信息 
    private MicroUser microUser;

    // 用户角色信息 
    private List<MicroRole> microRoles;

    // 用户与平台关联信息 
    private MicroUserPlatformRe microUserPlatformRe;

    // 用户与应用关联信息 
    private List<MicroUserAppRe> microUserAppRes;

    // 试用者手机号 
    private String trialPhone;
}

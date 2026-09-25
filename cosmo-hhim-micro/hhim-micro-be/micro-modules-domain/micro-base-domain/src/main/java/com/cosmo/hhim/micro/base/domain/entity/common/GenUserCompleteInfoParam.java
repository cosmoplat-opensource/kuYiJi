/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomer;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-22
 */
@Data
public class GenUserCompleteInfoParam {

    // token 
    private String token;

    // 登录时间 
    private Long loginTime;

    // 登录过期时间 
    private Long expireTime;

    // 登录应用标识 
    private String applicationSign;

    // 登录平台类型 
    private String platformType;

    // 平台用户OpenId 
    private String openid;

    // 用户名 
    private String userName;

    // 运营平台租户用户信息 
    private HyzzCustomer hyzzCustomer;

    // 角色 
    private RoleCodeEnum roleCodeEnum;

    // 应用编码 
    private String appCode;

}

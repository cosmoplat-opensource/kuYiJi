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
public class UserBaseInfo {

    // 账号 
    private String userName;

    // 手机号 
    private String phoneNumber;

    // token 
    private String token;

    // 角色编码 
    private String roleCode;

    // 微信用户openId 
    private String openId;

    // === 多租户登录新增字段（decouple-from-ops-platform）===

    /**
     * 是否多租户（true=需要弹窗选择）
     */
    private Boolean multiTenant;

    /**
     * 临时 token（多租户选择前使用）
     */
    private String tempToken;

    /**
     * 关联租户列表
     */
    private List<TenantOption> tenantList;

    /**
     * 租户选项
     */
    @Data
    public static class TenantOption {
        private String tenantCode;
        private String tenantName;
        private String customerName;
        private Integer isPrimary;
    }
}

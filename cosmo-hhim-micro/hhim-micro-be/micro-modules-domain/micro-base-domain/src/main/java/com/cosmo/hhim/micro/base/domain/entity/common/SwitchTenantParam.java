/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;

/**
 * 切换租户入参
 *
 * <p>已登录用户（Bearer token 有效）调用此接口切换到另一个已关联的租户。
 * 安全：目标租户必须已关联当前用户（micro_user_tenant_index 存在对应行）。
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SwitchTenantParam {
    /** 目标租户编码 */
    @NotBlank(message = "tenantCode 不能为空")
    private String tenantCode;
}

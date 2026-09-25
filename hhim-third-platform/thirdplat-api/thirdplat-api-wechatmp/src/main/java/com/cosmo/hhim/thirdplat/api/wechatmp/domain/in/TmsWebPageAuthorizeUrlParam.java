/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-15
 */
@Data
public class TmsWebPageAuthorizeUrlParam extends WebPageAuthorizeUrlParam{

    // 用户名
    @NotBlank(message = "用户名不允许为空！")
    private String username;

    // 用户身份标识
    @NotBlank(message = "用户身份标识不允许为空！")
    private String userIdentity;

}

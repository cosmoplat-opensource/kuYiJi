/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-04-22
 */
@Data
public class WechatUserDetailInfoParam {
    // 公众号唯一标识
    @NotBlank(message = "公众号唯一标识不允许为空！")
    private String openid;

    // 用户账号
    @NotBlank(message = "用户账号不允许为空！")
    private String username;

    // 用户身份(0：司机，1：供应商，2：仓管员，3：TE货代)
    @NotBlank(message = "用户身份不允许为空！")
    private String userIdentity;
}

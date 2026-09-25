/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
@Data
public class WebPageUserInfoParam {
    // 用户唯一标识
    @NotBlank(message = "用户唯一标识不允许为空！")
    private String openId;

    // token
    @NotBlank(message = "网页授权access token不允许为空！")
    private String accessToken;
}

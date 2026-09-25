/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.out;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
@Data
public class WebPageAccessTokenResult {
    // token
    private String accessToken;

    // refresh token
    private String refreshToken;

    // 有效时长（单位：s）
    private Long expireTime;

    // 用户唯一标识
    private String openId;

    // 用户授权的作用域，使用逗号（,）分隔
    private String scope;
}

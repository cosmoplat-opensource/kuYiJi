/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.in;

import com.cosmo.hhim.thirdplat.api.wechatmp.constants.WebPageScopeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
@Data
public class WebPageAuthorizeUrlParamV2 {

    // 授权后重定向的回调链接地址(不需要URL encode)
    // 协议白名单：仅允许 http/https 开头的 URL 字符（防 javascript: 等协议注入，扫描报告"弱验证"加固）
    @NotBlank(message = "回调地址不允许为空！")
    @Size(max = 512, message = "回调地址长度不能超过512")
    @Pattern(regexp = "^https?://[a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=%-\\u0080-\\uffff]+$", message = "回调地址仅允许 http/https 协议 URL")
    private String redirectUri;

    // 应用授权作用域，snsapi_base （不弹出授权页面，直接跳转，只能获取用户openid），snsapi_userinfo （弹出授权页面，可通过openid拿到昵称、性别、所在地。并且， 即使在未关注的情况下，只要用户授权，也能获取其信息 ）
    @NotNull(message = "授权作用域不能为空")
    private WebPageScopeEnum webPageScopeEnum;

    // 重定向后会带上state参数，开发者可以填写a-zA-Z0-9的参数值，最多128字节（注意：不需要拼接租户，三方平台自动拼接）
    @Pattern(regexp = "^[a-zA-Z0-9]{0,128}$", message = "state仅允许字母数字，最长128字节")
    private String state;

    // 公众号AppId
    @NotBlank(message = "公众号AppId不允许为空！")
    private String appId;

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;

/**
 * 短信验证码登录入参
 *
 * @author cosmo-hhim-open Team
 * @createTime 2026-06-07
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsLoginParam {

    /** 微信小程序登录临时code，H5端可为空 */
    private String code;

    /** 是否H5端登录 */
    private Boolean isH5;

    /** 手机号 */
    @NotBlank(message = "手机号不允许为空！")
    private String phoneNumber;

    /** 短信验证码 */
    @NotBlank(message = "短信验证码不允许为空！")
    private String smsCode;

    /** 应用编码 */
    private String appCode;
}

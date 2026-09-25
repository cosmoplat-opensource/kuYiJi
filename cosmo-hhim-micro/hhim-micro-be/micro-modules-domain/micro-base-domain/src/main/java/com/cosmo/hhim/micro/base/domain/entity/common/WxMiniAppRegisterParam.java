/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitParam;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxMiniAppRegisterParam {

    // 微信用户openId 
    @NotBlank(message = "微信用户openId不允许为空！")
    private String openId;

    // 手机号 
    @SubmitParam
    @NotBlank(message = "手机号不允许为空！")
    private String phoneNum;

    // 用户昵称 
    @NotBlank(message = "用户昵称不允许为空！")
    private String nickName;

    // 邀请码 
    @NotBlank(message = "邀请码不允许为空！")
    private String inviteCode;

}

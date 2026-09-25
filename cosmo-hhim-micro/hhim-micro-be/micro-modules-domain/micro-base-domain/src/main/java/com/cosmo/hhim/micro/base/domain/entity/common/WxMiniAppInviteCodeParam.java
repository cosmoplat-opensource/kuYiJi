/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/24
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxMiniAppInviteCodeParam {

    // 邀请角色编码 
    @NotBlank(message = "角色编码不允许为空！")
    private String roleCode;

    // 邀请码有效时间(单位：min) 
    @NotNull(message = "邀请码有效时间不允许为空！")
    private Integer validTime;

    // 跳转至前端Page页（默认首页） 
    private String inviteToPage;

    // 跳转的小程序版本，默认是正式版（正式版为 "release"，体验版为 "trial"，开发版为 "develop"） 
    private String inviteToEnv;

    // 检查page 是否存在，默认是false
    // (为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）；
    // 为 false 时允许小程序未发布或者 page 不存在， 但page 有数量上限（60000个）请勿滥用) 
    private Boolean checkPath;

}

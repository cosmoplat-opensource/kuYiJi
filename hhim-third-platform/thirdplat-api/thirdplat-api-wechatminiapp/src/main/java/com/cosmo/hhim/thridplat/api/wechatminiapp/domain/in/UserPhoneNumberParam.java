/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Data
public class UserPhoneNumberParam {

    // 手机号获取凭证
    @NotBlank(message = "手机号获取凭证不允许为空！")
    private String code;

}

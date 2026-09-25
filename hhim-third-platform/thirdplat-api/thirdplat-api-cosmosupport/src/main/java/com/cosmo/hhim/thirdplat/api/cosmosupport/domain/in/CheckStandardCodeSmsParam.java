/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-04
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckStandardCodeSmsParam {

    // 验证码
    @NotBlank(message = "验证码不允许为空！")
    private String code;

    // 手机号
    @NotBlank(message = "手机号不允许为空！")
    private String phone;
}

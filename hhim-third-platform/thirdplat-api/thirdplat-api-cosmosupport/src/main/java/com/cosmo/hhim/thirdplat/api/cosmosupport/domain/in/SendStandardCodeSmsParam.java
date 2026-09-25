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
public class SendStandardCodeSmsParam {

    // 接收者手机号
    @NotBlank(message = "接收者手机号不允许为空！")
    private String receiver;

    // 有效时长（默认10min，单位：min）
    private String expireIn;

    // 验证码（默认6位数值）
    private String code;
}

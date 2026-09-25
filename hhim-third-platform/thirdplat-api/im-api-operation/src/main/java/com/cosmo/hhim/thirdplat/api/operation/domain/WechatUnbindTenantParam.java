/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-07
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatUnbindTenantParam extends WechatUnbindParam {

    // 租户编码
    @NotBlank(message = "租户编码不允许为空！")
    private String customerCode;
}

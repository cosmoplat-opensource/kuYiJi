/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-04
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendSmsParam {

    // 接收者手机号
    @NotBlank(message = "接收者手机号不允许为空！")
    private String receiver;

    // 短信模版ID
    @NotBlank(message = "短信模版ID不允许为空！")
    private String tempId;

    // 发送短信需替换内容
    private Map<String, String> params;

}

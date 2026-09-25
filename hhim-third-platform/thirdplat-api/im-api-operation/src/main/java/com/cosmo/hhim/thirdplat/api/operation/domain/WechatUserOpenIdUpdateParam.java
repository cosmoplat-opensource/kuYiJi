/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author cosmo-hhim-open Team
 * 更新微信用户openId请求参数
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatUserOpenIdUpdateParam {

    /**
     * 旧openid
     */
    private String oldOpenId;

    /**
     * 新openid
     */
    private String newOpenId;
}

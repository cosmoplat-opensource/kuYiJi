/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out;

import com.cosmo.hhim.thirdplat.modules.wechatmp.response.WxResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Data
public class AccessTokenInfo extends WxResponseResult {
    // 获取到的凭证
    private String access_token;

    // 凭证有效时间，单位：秒
    private Long expires_in;
}

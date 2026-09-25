/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out;

import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-19
 */
@Data
public class AppAccessTokenInfo extends WxAppResponseResult {

    // 获取到的凭证
    private String access_token;

    // 凭证有效时间，单位：秒 (目前是7200秒之内的值)
    private Long expires_in;

}

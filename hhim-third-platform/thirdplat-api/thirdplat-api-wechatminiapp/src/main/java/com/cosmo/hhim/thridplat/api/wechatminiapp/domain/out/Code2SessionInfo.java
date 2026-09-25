/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out;

import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
@Data
public class Code2SessionInfo extends WxAppResponseResult {

    // 会话密钥
    private String session_key;

    // 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回，详见 UnionID 机制说明。
    private String unionid;

    // 用户唯一标识
    private String openid;

}

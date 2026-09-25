/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.out;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-05-12
 */
@Data
public class WxJsSdkConfigResult {

    // 公众号appid
    private String appId;

    // 生成签名的时间戳
    private String timestamp;

    // 生成签名的随机串
    private String nonceStr;

    // 签名
    private String signature;
}

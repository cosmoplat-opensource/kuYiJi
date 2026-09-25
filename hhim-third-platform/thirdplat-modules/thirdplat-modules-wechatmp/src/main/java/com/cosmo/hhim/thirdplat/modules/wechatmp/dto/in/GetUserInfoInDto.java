/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Data
public class GetUserInfoInDto {
    // 普通用户的标识，对当前公众号唯一(Y)
    private String openid;

    // 返回国家地区语言版本，zh_CN 简体，zh_TW 繁体，en 英语(N)
    private String lang;
}

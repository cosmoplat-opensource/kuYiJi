/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out;

import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Data
public class WaterMark extends WxAppResponseResult {

    // 用户获取手机号操作的时间戳
    private Long timestamp;

    // 小程序appid
    private String appid;
}

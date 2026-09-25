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
public class UserPhoneInfo extends WxAppResponseResult {

    // 用户手机号信息
    private PhoneInfo phone_info;

    /**
     * 用户手机号信息
     */
    @Data
    public static class PhoneInfo {

        // 用户绑定的手机号（国外手机号会有区号）
        private String phoneNumber;

        // 没有区号的手机号
        private String purePhoneNumber;

        // 区号
        private String countryCode;

        // 数据水印
        private WaterMark waterMark;
    }

}

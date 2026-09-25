/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.common.constants;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
public class Constant {

    public static final String THIRDPLAT_SERVICE = "hhim-third-platform";

    public enum RedisKeys {
        // uniapp消息推送模块
        UNIPUSH("thirdpart:uniapp:push:"),
        // 微信公众号模块
        WECHATMP("thirdpart:wechat:mp:"),
        // 微信小程序模块
        WECHATMINIAPP("thirdpart:wechat:miniapp:"),
        // 卡奥斯服务支持模块
        COSMOSUPPORT("thirdpart:cosmoplat:support:"),
        // 海尔数据平台支持模块
        HAIERDATAPLAT("thirdpart:sqm:haierDataPlat:");

        private String key;

        RedisKeys(String key) {
            this.key = key;

        }

        public String value(String value) {
            return this.key + value;
        }
    }

    public static final String WECHAT_MP_CONFIG_REDIS_KEY = "wechatMpConfig";
    public static final String WECHAT_MP_ALL_CONFIG_REDIS_KEY = "wechatMpAllConfig";
    public static final String WECHAT_ACCESSTOKEN_REDIS_KEY = "wechatAccessToken";
    public static final String WECHAT_WEB_ACCESSTOKEN_REDIS_KEY = "wechatWebAccessToken";
    public static final String COSMOSUPPORT_IOT_ACCESSTOKEN_REDIS_KEY = "cosmoSupportIOTAccessToken";
    public static final String COSMOSUPPORT_IOT_CONFIG_REDIS_KEY = "cosmoSupportIOTConfig";
    public static final String WECHAT_MINIAPP_CONFIG_REDIS_KEY = "wechatMiniAppConfig";
    public static final String WECHAT_MINIAPP_ACCESSTOKEN_REDIS_KEY = "wechatMiniAppAccessToken";
    public static final String COSMO_SUPPORT_SQM_ACCESS_TOKEN_REDIS_KEY = "cosmoSupportSQMAccessToken";
    public static final String COSMO_SUPPORT_SQM_CONFIG_REDIS_KEY = "cosmoSupportSQMConfig";

    public static final String TENANT_CODE = "tenantCode";
    public static final String WXMP_APPID = "wechatMpAppId";
    public static final String WXMINIAPP_APPID = "wechatMiniAppId";

}

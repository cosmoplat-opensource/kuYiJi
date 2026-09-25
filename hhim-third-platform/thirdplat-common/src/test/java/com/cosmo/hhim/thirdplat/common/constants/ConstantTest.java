/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.common.constants;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConstantTest {

    @Test
    public void testRedisKeys_unipush() {
        assertEquals("thirdpart:uniapp:push:test123", Constant.RedisKeys.UNIPUSH.value("test123"));
    }

    @Test
    public void testRedisKeys_wechatmp() {
        assertEquals("thirdpart:wechat:mp:test", Constant.RedisKeys.WECHATMP.value("test"));
    }

    @Test
    public void testRedisKeys_wechatMiniApp() {
        assertEquals("thirdpart:wechat:miniapp:test", Constant.RedisKeys.WECHATMINIAPP.value("test"));
    }

    @Test
    public void testRedisKeys_cosmosupport() {
        assertEquals("thirdpart:cosmoplat:support:test", Constant.RedisKeys.COSMOSUPPORT.value("test"));
    }

    @Test
    public void testRedisKeys_haierDataPlat() {
        assertEquals("thirdpart:sqm:haierDataPlat:test", Constant.RedisKeys.HAIERDATAPLAT.value("test"));
    }

    @Test
    public void testConstants() {
        assertEquals("tenantCode", Constant.TENANT_CODE);
        assertEquals("wechatMpAppId", Constant.WXMP_APPID);
        assertEquals("wechatMiniAppId", Constant.WXMINIAPP_APPID);
        assertEquals("hhim-third-platform", Constant.THIRDPLAT_SERVICE);
        assertEquals("wechatMpConfig", Constant.WECHAT_MP_CONFIG_REDIS_KEY);
        assertEquals("wechatAccessToken", Constant.WECHAT_ACCESSTOKEN_REDIS_KEY);
    }
}

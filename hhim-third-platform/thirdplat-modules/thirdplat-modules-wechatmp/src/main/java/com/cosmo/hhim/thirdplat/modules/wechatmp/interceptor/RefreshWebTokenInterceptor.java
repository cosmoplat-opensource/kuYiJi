/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.WebAccessTokenInfo;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
public class RefreshWebTokenInterceptor implements Interceptor<WebAccessTokenInfo> {

    private static final String GRANT_TYPE = "grant_type";
    private static final String APP_ID = "appid";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.info("http请求前置拦截器--->web refresh token的请求参数赋值");
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);
        if (!StringUtils.hasText(appId)) {
            return false;
        }

        // 通过appId查询redis缓存中的公众号配置
        String wechatMpConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY));
        if (!StringUtils.hasText(wechatMpConfigStr)) {
            return false;
        }
        HyzzWechatConfig wechatConfig = JSON.parseObject(wechatMpConfigStr, HyzzWechatConfig.class);
        request.addQuery(APP_ID, wechatConfig.getAppId()).addQuery(GRANT_TYPE, "refresh_token");
        return true;
    }
}

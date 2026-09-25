/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.AppAccessTokenInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.HyzzMicroMiniappConfig;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-19
 */
@Slf4j
public class GainAppAccessTokenInterceptor implements Interceptor<AppAccessTokenInfo> {

    private static final String GRANT_TYPE = "grant_type";
    private static final String APP_ID = "appid";
    private static final String APP_SECRET = "secret";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.info("微信小程序http请求前置拦截器--->access token的请求参数赋值");
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMINIAPP_APPID);
        if (!StringUtils.hasText(appId)) {
            return false;
        }

        // 从redis中获取缓存的token，存在则不发起请求
        String redisResult = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_ACCESSTOKEN_REDIS_KEY));
        if (StringUtils.hasText(redisResult)) {
            return false;
        }

        // 通过appId查询redis缓存中的小程序配置
        String wechatMiniAppConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_CONFIG_REDIS_KEY));
        if (!StringUtils.hasText(wechatMiniAppConfigStr)) {
            return false;
        }
        HyzzMicroMiniappConfig wechatMiniAppConfig = JSON.parseObject(wechatMiniAppConfigStr, HyzzMicroMiniappConfig.class);
        request.addBody(GRANT_TYPE, "client_credential")
                .addBody(APP_ID, wechatMiniAppConfig.getAppId())
                .addBody(APP_SECRET, wechatMiniAppConfig.getAppSecret());

        return true;
    }

    @Override
    public void onSuccess(AppAccessTokenInfo data, ForestRequest request, ForestResponse response) {
        log.info("微信小程序http请求后置成功拦截器--->access token缓存redis");
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMINIAPP_APPID);
        if (StringUtils.hasText(appId)) {
            String key = appId + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_ACCESSTOKEN_REDIS_KEY);
            String value = JSON.toJSONString(data);
            // 提前 5 分钟过期，避免边界时间点 token 失效，同时防止并发刷新导致旧 token 被踢
            long expireSeconds = data.getExpires_in() - 300;
            redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        }
    }
}

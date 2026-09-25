/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.HyzzMicroMiniappConfig;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
@Slf4j
public class Code2SessionInterceptor implements Interceptor<Code2SessionInfo> {

    private static final String GRANT_TYPE = "grant_type";
    private static final String APP_ID = "appid";
    private static final String APP_SECRET = "secret";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.info("微信小程序http请求前置拦截器--->code换session的请求参数赋值");
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMINIAPP_APPID);
        if (!StringUtils.hasText(appId)) {
            return false;
        }

        // 通过appId查询redis缓存中的小程序配置
        String wechatMiniAppConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_CONFIG_REDIS_KEY));
        if (!StringUtils.hasText(wechatMiniAppConfigStr)) {
            return false;
        }
        HyzzMicroMiniappConfig wechatMiniAppConfig = JSON.parseObject(wechatMiniAppConfigStr, HyzzMicroMiniappConfig.class);

        request.addQuery(GRANT_TYPE, "authorization_code")
                .addQuery(APP_ID, wechatMiniAppConfig.getAppId())
                .addQuery(APP_SECRET, wechatMiniAppConfig.getAppSecret());
        return true;
    }
}

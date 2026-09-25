/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.core.utils.SpringContextUtil;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.AccessTokenInfo;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Slf4j
public class GainTokenInterceptor implements Interceptor<AccessTokenInfo> {

    private static final String GRANT_TYPE = "grant_type";
    private static final String APP_ID = "appid";
    private static final String APP_SECRET = "secret";

    private RedisTemplate<String, String> redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean beforeExecute(ForestRequest request) {
        if (redisTemplate == null) {
            try {
                redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
                log.info("[GainToken] 通过SpringContextUtil获取RedisTemplate成功");
            } catch (Exception e) {
                log.error("[GainToken] SpringContextUtil获取Bean失败, 异常信息={}", e.getMessage(), e);
                return false;
            }
        }
        log.info("[GainToken] http请求前置拦截器--->access token的请求参数赋值");
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);
        log.info("[GainToken] 从ThreadLocalCache获取appId, appId={}", appId);
        if (!StringUtils.hasText(appId)) {
            log.error("[GainToken] appId为空, 无法获取access_token");
            return false;
        }

        // 从redis中获取缓存的token，存在则不发起请求
        String redisKey = appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_ACCESSTOKEN_REDIS_KEY);
        try {
            String redisResult = redisTemplate.opsForValue().get(redisKey);
            log.info("[GainToken] Redis查询access_token缓存, key={}, 是否存在={}", redisKey, StringUtils.hasText(redisResult));
            if (StringUtils.hasText(redisResult)) {
                log.info("[GainToken] Redis中已有access_token缓存, 跳过请求");
                return false;
            }
        } catch (Exception e) {
            log.error("[GainToken] Redis读取异常, 继续请求微信接口, 异常信息={}", e.getMessage(), e);
        }

        // 通过appId查询redis缓存中的公众号配置
        String configKey = appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY);
        String wechatMpConfigStr = null;
        try {
            wechatMpConfigStr = redisTemplate.opsForValue().get(configKey);
            log.info("[GainToken] Redis查询公众号配置, key={}, 是否存在={}", configKey, StringUtils.hasText(wechatMpConfigStr));
        } catch (Exception e) {
            log.error("[GainToken] Redis读取公众号配置异常, 异常信息={}", e.getMessage(), e);
        }
        if (!StringUtils.hasText(wechatMpConfigStr)) {
            log.error("[GainToken] 公众号配置为空, configKey={}, 无法获取access_token", configKey);
            return false;
        }
        HyzzWechatConfig wechatConfig = JSON.parseObject(wechatMpConfigStr, HyzzWechatConfig.class);
        log.info("[GainToken] 公众号配置解析成功, appId={}, appSecret前缀={}", wechatConfig.getAppId(),
                StringUtils.hasText(wechatConfig.getAppSecret()) ? wechatConfig.getAppSecret().substring(0, Math.min(6, wechatConfig.getAppSecret().length())) + "..." : "null");
        request.addQuery(GRANT_TYPE, "client_credential")
                .addQuery(APP_ID, wechatConfig.getAppId())
                .addQuery(APP_SECRET, wechatConfig.getAppSecret());

        return true;
    }

    @Override
    public void onSuccess(AccessTokenInfo data, ForestRequest request, ForestResponse response) {
        log.info("[GainToken] http请求后置成功拦截器--->access token缓存redis");
        if (redisTemplate == null) {
            try {
                redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
            } catch (Exception e) {
                log.error("[GainToken] onSuccess获取RedisTemplate失败, token未缓存, 异常信息={}", e.getMessage(), e);
                return;
            }
        }
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);
        if (StringUtils.hasText(appId)) {
            String key = appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_ACCESSTOKEN_REDIS_KEY);
            String value = JSON.toJSONString(data);
            redisTemplate.opsForValue().set(key, value, data.getExpires_in(), TimeUnit.SECONDS);
            log.info("[GainToken] access_token已缓存到Redis, key={}, expiresIn={}", key, data.getExpires_in());
        }
    }
}

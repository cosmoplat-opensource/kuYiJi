/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.api.WxAppAccessTokenApi;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.AppAccessTokenInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
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
public class WxAppAccessTokenInjectInterceptor implements Interceptor {

    public static final String ACCESSTOKEN = "access_token";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WxAppAccessTokenApi wxAppAccessTokenApi;

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.info("微信小程序http请求前置拦截器--->请求URL添加access token参数");
        String accessToken = null;
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMINIAPP_APPID);
        if (StringUtils.hasText(appId)) {
            String redisResult = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_ACCESSTOKEN_REDIS_KEY));
            if (StringUtils.hasText(redisResult)) {
                AppAccessTokenInfo accessTokenInfo = JSON.parseObject(redisResult, AppAccessTokenInfo.class);
                accessToken = accessTokenInfo.getAccess_token();
            } else {
                ForestResponse<AppAccessTokenInfo> response = wxAppAccessTokenApi.getStableAccessToken();
                if (response.isSuccess() && null != response.getResult()) {
                    accessToken = response.getResult().getAccess_token();
                }
            }
        }

        if (StringUtils.hasText(accessToken)) {
            request.addQuery(ACCESSTOKEN, accessToken);
        } else {
            log.error("无法发起请求, access token获取失败!!!");
            return false;
        }
        return true;
    }

    @Override
    public void afterExecute(ForestRequest request, ForestResponse response) {
        if (response != null && response.getContent() != null) {
            try {
                WxAppResponseResult result = JSON.parseObject(response.getContent(), WxAppResponseResult.class);
                if (result.getErrcode() != null && result.getErrcode() == 40001) {
                    // access_token 失效，清除 Redis 缓存，下次请求将自动获取新 token
                    String appId = (String) ThreadLocalCache.getCache(Constant.WXMINIAPP_APPID);
                    if (StringUtils.hasText(appId)) {
                        String key = appId + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_ACCESSTOKEN_REDIS_KEY);
                        redisTemplate.delete(key);
                        log.warn("检测到微信返回 40001 (invalid credential)，已清除 Redis 中的失效 access_token，下次请求将自动获取新 token");
                    }
                }
            } catch (Exception e) {
                // 响应可能是二进制数据（如小程序码图片），解析失败属于正常情况，忽略即可
            }
        }
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.core.utils.SpringContextUtil;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxAccessTokenApi;
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
 * @createTime 2022/3/1
 */
@Slf4j
public class AccessTokenUrlInjectInterceptor implements Interceptor {

    public static final String ACCESSTOKEN = "access_token";

    private RedisTemplate<String, String> redisTemplate;

    private WxAccessTokenApi wxAccessTokenApi;

    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setWxAccessTokenApi(WxAccessTokenApi wxAccessTokenApi) {
        this.wxAccessTokenApi = wxAccessTokenApi;
    }

    @Override
    public boolean beforeExecute(ForestRequest request) {
        // Forest 通过反射创建拦截器实例，@Autowired 不会生效，需要从 Spring 容器手动获取
        if (redisTemplate == null || wxAccessTokenApi == null) {
            try {
                if (redisTemplate == null) {
                    redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
                }
                if (wxAccessTokenApi == null) {
                    wxAccessTokenApi = SpringContextUtil.getBean(WxAccessTokenApi.class);
                }
                log.info("[微信JS-SDK] 通过SpringContextUtil获取Bean成功");
            } catch (Exception e) {
                log.error("[微信JS-SDK] SpringContextUtil获取Bean失败, 异常信息={}", e.getMessage(), e);
            }
        }
        log.info("[微信JS-SDK] http请求前置拦截器--->请求URL添加access token参数, requestUrl={}", request.getUrl());
        String accessToken = null;
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);
        log.info("[微信JS-SDK] 从ThreadLocalCache获取appId, appId={}", appId);
        if (StringUtils.hasText(appId)) {
            String redisKey = appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_ACCESSTOKEN_REDIS_KEY);
            String redisResult = null;
            try {
                log.info("[微信JS-SDK] 从Redis获取access_token, redisKey={}", redisKey);
                redisResult = redisTemplate.opsForValue().get(redisKey);
                log.info("[微信JS-SDK] Redis查询结果, redisResult是否存在={}", StringUtils.hasText(redisResult));
            } catch (Exception e) {
                log.error("[微信JS-SDK] Redis读取异常, redisKey={}, 降级到实时刷新, 异常信息={}", redisKey, e.getMessage(), e);
            }
            if (StringUtils.hasText(redisResult)) {
                try {
                    log.info("[微信JS-SDK] Redis原始数据={}", redisResult.length() > 200 ? redisResult.substring(0, 200) + "..." : redisResult);
                    AccessTokenInfo accessTokenInfo = JSON.parseObject(redisResult, AccessTokenInfo.class);
                    accessToken = accessTokenInfo.getAccess_token();
                    if (!StringUtils.hasText(accessToken)) {
                        log.warn("[微信JS-SDK] Redis缓存数据损坏(access_token为空), 自动清除并降级刷新, redisKey={}", redisKey);
                        redisTemplate.delete(redisKey);
                    } else {
                        log.info("[微信JS-SDK] 从Redis解析到access_token, accessToken前缀={}",
                                accessToken.substring(0, Math.min(10, accessToken.length())) + "...");
                    }
                } catch (Exception e) {
                    log.error("[微信JS-SDK] Redis数据解析异常, 自动清除并降级到实时刷新, redisKey={}, 异常信息={}", redisKey, e.getMessage(), e);
                    redisTemplate.delete(redisKey);
                }
            }
            if (!StringUtils.hasText(accessToken)) {
                log.info("[微信JS-SDK] 开始实时调用微信接口刷新access_token");
                ForestResponse<AccessTokenInfo> response = null;
                try {
                    response = wxAccessTokenApi.getAccessToken();
                } catch (Exception e) {
                    log.error("[微信JS-SDK] 调用微信接口异常, 异常信息={}", e.getMessage(), e);
                }
                if (response == null) {
                    log.error("[微信JS-SDK] 调用微信接口返回null, wxAccessTokenApi可能未正确注入");
                } else {
                    log.info("[微信JS-SDK] 微信接口返回, isSuccess={}, result是否为空={}", response.isSuccess(), response.getResult() == null);
                }
                if (response != null && response.isSuccess() && null != response.getResult()) {
                    accessToken = response.getResult().getAccess_token();
                    log.info("[微信JS-SDK] 实时刷新成功, accessToken前缀={}",
                            StringUtils.hasText(accessToken) ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : "null");

                    try {
                        AccessTokenInfo tokenInfo = new AccessTokenInfo();
                        tokenInfo.setAccess_token(accessToken);
                        tokenInfo.setExpires_in(response.getResult().getExpires_in());
                        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(tokenInfo), 7000, TimeUnit.SECONDS);
                        log.info("[微信JS-SDK] access_token已缓存到Redis, key={}, 过期时间=7000秒", redisKey);
                    } catch (Exception e) {
                        log.error("[微信JS-SDK] Redis写入异常, access_token未缓存但不影响本次请求, 异常信息={}", e.getMessage(), e);
                    }
                } else if (response != null) {
                    log.error("[微信JS-SDK] 实时刷新access_token失败, responseContent={}", response.getContent());
                } else {
                    log.error("[微信JS-SDK] 实时刷新access_token失败, response为null");
                }
            }
        } else {
            log.error("[微信JS-SDK] ThreadLocalCache中未找到appId, 无法获取access_token");
        }

        if (StringUtils.hasText(accessToken)) {
            request.addQuery(ACCESSTOKEN, accessToken);
            log.info("[微信JS-SDK] access_token注入成功");
        } else {
            log.error("[微信JS-SDK] 无法发起请求, access token获取失败!!!");
            return false;
        }
        return true;
    }
}

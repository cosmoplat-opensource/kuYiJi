/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxAccessTokenApi;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpBaseService;
import com.cosmo.hhim.thirdplat.web.utils.SHA1;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-06-29
 */
@Slf4j
@Service
public class MpBaseServiceImpl implements IMpBaseService {

    @Autowired
    private WxAccessTokenApi wxAccessTokenApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 微信公众号服务器接入验签
     *
     * @param timestamp
     * @param nonce
     * @param signature
     * @return
     */
    @Override
    public APIResponse<Boolean> checkSignature(String timestamp, String nonce, String signature) {
        boolean result = false;
        try {
            // 通过appId查询redis缓存中的公众号配置
            String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);
            if (StringUtils.hasText(appId)) {
                String wechatMpConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY));
                if (StringUtils.hasText(wechatMpConfigStr)) {
                    HyzzWechatConfig wechatConfig = JSON.parseObject(wechatMpConfigStr, HyzzWechatConfig.class);
                    if (StringUtils.hasText(wechatConfig.getServerToken())) {

                        // SHA1验签
                        result = SHA1.gen(wechatConfig.getServerToken(), timestamp, nonce).equals(signature);
                    }
                }
            }
        } catch (Exception e) {
            log.error("验签失败！reason:{}", Throwables.getStackTraceAsString(e));
        }
        return APIResponse.success(result);
    }
}

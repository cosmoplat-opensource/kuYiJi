/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.runner;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzMicroMiniappConfigMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzWechatConfigMapper;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.HyzzMicroMiniappConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/3/1
 */
@Slf4j
@Component
public class CacheWechatConfigRunner implements ApplicationRunner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private HyzzWechatConfigMapper hyzzWechatConfigMapper;

    @Autowired
    private HyzzMicroMiniappConfigMapper hyzzMicroMiniappConfigMapper;

    @Value("${forest.variables.wxmpAppId}")
    private String wxmpAppId;

    @Value("${forest.variables.wxmpAppSecret}")
    private String wxmpAppSecret;

    @Value("${forest.variables.wxmpServerToken}")
    private String wxmpServerToken;

    @Value("${forest.variables.wxnoServerEncodeAesKey}")
    private String wxnoServerEncodeAesKey;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<HyzzWechatConfig> wechatConfigs = hyzzWechatConfigMapper.selectHyzzWechatConfigList();

            // 添加平台默认公众号配置（备注：支持多租户的公众号）
            HyzzWechatConfig hyzzPlatformWechatConfig = new HyzzWechatConfig();
            hyzzPlatformWechatConfig.setAppId(wxmpAppId);
            hyzzPlatformWechatConfig.setAppSecret(wxmpAppSecret);
            hyzzPlatformWechatConfig.setServerToken(wxmpServerToken);
            hyzzPlatformWechatConfig.setServerEncodeAeskey(wxnoServerEncodeAesKey);
            wechatConfigs.add(hyzzPlatformWechatConfig);

            if (!CollectionUtils.isEmpty(wechatConfigs)) {
                // 根据AppId存储对应的微信公众号配置信息到Redis
                for (HyzzWechatConfig wechatConfig : wechatConfigs) {
                    String key = wechatConfig.getAppId() + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY);
                    String value = JSON.toJSONString(wechatConfig);
                    redisTemplate.opsForValue().set(key, value);
                }

                // 同步全部微信公众配置信息到Redis
                String allConfigKey = Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_ALL_CONFIG_REDIS_KEY);
                redisCache.deleteObject(allConfigKey);
                redisCache.setCacheList(allConfigKey, wechatConfigs);
            }

            // 查询小程序配置信息表
            List<HyzzMicroMiniappConfig> miniAppConfigs = hyzzMicroMiniappConfigMapper.selectAllMicroMiniAppConfig();
            if (!CollectionUtils.isEmpty(miniAppConfigs)) {
                // 根据应用标识存储对应的微信小程序配置信息到redis
                for (HyzzMicroMiniappConfig miniAppConfig : miniAppConfigs) {
                    String redisKey = miniAppConfig.getAppId() + ":" + Constant.RedisKeys.WECHATMINIAPP.value(Constant.WECHAT_MINIAPP_CONFIG_REDIS_KEY);
                    String redisValue = JSON.toJSONString(miniAppConfig);
                    redisTemplate.opsForValue().set(redisKey, redisValue);
                }
            }
        } catch (Exception e) {
            log.warn("CacheWechatConfigRunner 启动缓存微信配置失败（数据库未就绪或 schema 未配置），跳过缓存加载: {}", e.getMessage());
        }
    }
}

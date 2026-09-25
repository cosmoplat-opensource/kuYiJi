/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.application.service.base.IMicroUserFacadeService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.events.FlushCacheUserInfoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 刷新用户信息监听
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class FlushUserInfoListener implements ApplicationListener<FlushCacheUserInfoEvent> {
    @Autowired
    private IMicroUserFacadeService userFacadeService;
    @Autowired
    private RedisCache redisCache;
    @Value("${spring.profiles.active}")
    private String env;

    @Async("asyncEventExecutor")
    @Override
    public void onApplicationEvent(FlushCacheUserInfoEvent flushCacheUserInfoEvent) {
        if (!CommonConstants.ACTIVE_ENV_SET.contains(env)) {
            return;
        }
        log.info("监听到:刷新用户信息事件,开始执行");
        String customerCode = flushCacheUserInfoEvent.getMessage();
        Map<String, String> cacheMap = redisCache.getCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY);
        userFacadeService.cacheAllCustomerUserInfo(cacheMap, customerCode);
        log.info("刷新用户信息事件结束:{}", customerCode);
    }
}

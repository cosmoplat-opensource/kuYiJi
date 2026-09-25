/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.schedule;

import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.micro.application.service.base.IMicroThirdPlatFacadeService;
import com.cosmo.hhim.micro.application.service.base.IMicroUserFacadeService;
import com.cosmo.hhim.micro.application.service.storage.IMicroProcessStorageFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * decouple-from-ops-platform：定时刷新缓存信息
 *  - 不再调 im-api-operation / RemoteCustomerService
 *  - 直接查本地 micro_tenant 表
 *  - 单库场景：DBControlUtil 只设 tenantCode 即可
 *
 * @author cosmo-hhim-open Team
 */
@Component
@EnableScheduling
@Slf4j
public class FlushCacheSchedule {

    @Autowired
    private IMicroThirdPlatFacadeService thirdPlatFacadeService;
    @Autowired
    private IMicroUserFacadeService microUserFacadeService;
    @Autowired
    private IMicroProcessStorageFacadeService processStorageFacadeService;
    @Autowired
    private IMicroTenantService microTenantService;
    @Autowired
    private RedisLockHelper redisLockHelper;

    @Value("${spring.profiles.active}")
    private String env;

    private static final int MICRO_PRODUCT_TYPE = 1;

    /**
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    private void cacheUserInfo() {
        if (!CommonConstants.ACTIVE_ENV_SET.contains(env)) {
            return;
        }
        String lockKey = "flush_cache_schedule";
        int timeout = 60 * 30;
        if (!redisLockHelper.lock(lockKey, IdUtils.simpleUUID(), timeout, TimeUnit.SECONDS)) {
            log.info("任务已由其他节点执行");
            return;
        }
        log.info(">>>>flush_cache_schedule : begin!");

        Map<String, String> configMap = thirdPlatFacadeService.cacheMicroApplicationConfig();
        if (CollectionUtils.isEmpty(configMap)) {
            log.warn("无法从应用配置表获取 app_sign/app_code 映射");
            // 不直接 return；只是不预热 user 缓存
        }

        // 查本地 micro_tenant（不再调 im-api-operation）
        List<MicroTenant> tenants = microTenantService.listActive();
        if (CollectionUtils.isEmpty(tenants)) {
            log.warn("本地 micro_tenant 为空，跳过缓存预热");
            log.info(">>>>flush_cache_schedule : end (no tenants)!");
            return;
        }
        for (MicroTenant tenant : tenants) {
            String customerCode = tenant.getTenantCode();
            // 单库场景：只设 tenantCode
            DBControlUtil.setDbAndSchema("db0", "im_micro", customerCode);
            if (!CollectionUtils.isEmpty(configMap)) {
                microUserFacadeService.cacheAllCustomerUserInfo(configMap, customerCode);
            }
            processStorageFacadeService.flushWarningMetrics(customerCode);
        }

        log.info(">>>>flush_cache_schedule : end!");
    }
}

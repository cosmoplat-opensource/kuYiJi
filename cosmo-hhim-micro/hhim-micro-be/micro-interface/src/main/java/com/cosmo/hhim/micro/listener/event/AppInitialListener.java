/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.micro.application.service.base.IMicroThirdPlatFacadeService;
import com.cosmo.hhim.micro.application.service.base.IMicroUserFacadeService;
import com.cosmo.hhim.micro.application.service.storage.IMicroProcessStorageFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 项目启动后的一些业务操作
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class AppInitialListener implements ApplicationRunner {

    @Autowired
    private IMicroThirdPlatFacadeService thirdPlatFacadeService;
    @Autowired
    private IMicroUserFacadeService microUserFacadeService;
    @Autowired
    private IMicroProcessStorageFacadeService processStorageFacadeService;
    @Autowired
    private IMicroTenantService microTenantService;
    @Value("${spring.profiles.active}")
    private String env;

    /**
     * 项目启动后
     * 1.缓存app_sign与app_code映射关系
     * 2.缓存用户信息
     *
     * @param args incoming application arguments
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!CommonConstants.ACTIVE_ENV_SET.contains(env)) {
            return;
        }
        log.debug(">>>>app_initial_listener : begin!");
        Map<String, String> configMap = thirdPlatFacadeService.cacheMicroApplicationConfig();
        if (CollectionUtils.isEmpty(configMap)) {
            log.warn("无法从应用配置表获取 app_sign/app_code 映射");
            // 不直接 return；只是不预热 user 缓存
        }

        // 查本地 micro_tenant（不再调 im-api-operation）
        List<MicroTenant> tenants = microTenantService.listActive();
        if (CollectionUtils.isEmpty(tenants)) {
            log.warn("本地 micro_tenant 为空，跳过缓存预热");
            log.debug(">>>>app_initial_listener : end!");
            return;
        }
        for (MicroTenant tenant : tenants) {
            String customerCode = tenant.getTenantCode();
            // 单库场景：datasource 固定 db0，schema 固定 im_micro
            DBControlUtil.setDbAndSchema("db0", CommonConstants.MICRO_SCHEMA, customerCode);
            if (!CollectionUtils.isEmpty(configMap)) {
                microUserFacadeService.cacheAllCustomerUserInfo(configMap, customerCode);
            }
            processStorageFacadeService.flushWarningMetrics(customerCode);
        }
        log.debug(">>>>app_initial_listener : end!");
    }
}

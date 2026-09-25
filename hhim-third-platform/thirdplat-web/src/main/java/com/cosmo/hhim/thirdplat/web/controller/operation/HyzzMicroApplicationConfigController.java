/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzMicroApplicationConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzMicroApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 微应用-应用配置信息Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
@RestController
@RequestMapping("/micro/config")
public class HyzzMicroApplicationConfigController {
    @Autowired
    private IHyzzMicroApplicationConfigService hyzzMicroApplicationConfigService;


    /**
     * 获取微应用-根据应用编码
     */
    @GetMapping(value = "/getApplicationConfigByAppCode")
    public APIResponse<HyzzMicroApplicationConfig> getApplicationConfigByAppCode(@RequestParam("appCode") String appCode) {
        return hyzzMicroApplicationConfigService.selectMicroApplicationConfigByAppCode(appCode);
    }

    /**
     * 获取微应用-根据应用标识
     */
    @GetMapping(value = "/getApplicationConfigByAppSign")
    public APIResponse<HyzzMicroApplicationConfig> getApplicationConfigByAppSign(@RequestParam("appSign") String appSign) {
        return hyzzMicroApplicationConfigService.selectMicroApplicationConfigByAppSign(appSign);
    }

    /**
     * 获取所有微应用配置信息
     */
    @GetMapping(value = "/getAllMapping")
    public APIResponse<List<HyzzMicroApplicationConfig>> getApplicationConfigAllMapping() {
        return hyzzMicroApplicationConfigService.selectMicroApplicationAllMapping();
    }

    /**
     * 获取当前租户所购买的所有应用信息
     */
    @GetMapping("/getAllAppDetailInfoForCurrentTenant")
    public APIResponse<List<HyzzMicroApplicationConfig>> getAllAppDetailInfoForCurrentTenant(@RequestParam("productType") Integer productType){
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        return hyzzMicroApplicationConfigService.selectMicroApplicationDetailInfo(tenantCode, productType);
    }


}

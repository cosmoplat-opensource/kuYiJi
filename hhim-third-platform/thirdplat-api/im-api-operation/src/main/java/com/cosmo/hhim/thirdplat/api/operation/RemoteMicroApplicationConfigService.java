/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzMicroApplicationConfig;
import com.cosmo.hhim.thirdplat.api.operation.factory.RemoteMicroApplicationConfigFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/8
 */
@FeignClient(contextId = "remoteMicroApplicationConfigService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteMicroApplicationConfigFallbackFactory.class)
public interface RemoteMicroApplicationConfigService {

    /**
     * 获取微应用-根据应用编码
     */
    @GetMapping(value = "/micro/config/getApplicationConfigByAppCode")
    APIResponse<HyzzMicroApplicationConfig> getApplicationConfigByAppCode(@RequestParam("appCode") String appCode);

    /**
     * 获取微应用-根据应用标识
     */
    @GetMapping(value = "/micro/config/getApplicationConfigByAppSign")
    APIResponse<HyzzMicroApplicationConfig> getApplicationConfigByAppSign(@RequestParam("appSign") String appSign);

    /**
     * 获取所有微应用配置信息
     */
    @GetMapping(value = "/micro/config/getAllMapping")
    APIResponse<List<HyzzMicroApplicationConfig>> getApplicationConfigAllMapping();

    /**
     * 获取当前租户购买的所有的应用信息
     * @return
     */
    @GetMapping("/micro/config/getAllAppDetailInfoForCurrentTenant")
    APIResponse<List<HyzzMicroApplicationConfig>> getAllAppDetailConfigForCurrentTenant(@RequestParam("productType") Integer productType);

}

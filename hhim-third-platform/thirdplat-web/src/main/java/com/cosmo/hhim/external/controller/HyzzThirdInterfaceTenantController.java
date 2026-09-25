/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.controller;

import com.cosmo.hhim.external.service.ThirdTenantService;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzThirdInterfaceTenant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * @version 1.0.0
 * @date 2023/3/6 13:47
 */
@RestController
@RequestMapping("/thirdclient/tenant")
public class HyzzThirdInterfaceTenantController {

    @Autowired
    private ThirdTenantService thirdTenantService;

    @GetMapping("/findByTenantAndClient")
    public APIResponse<HyzzThirdInterfaceTenant> findByTenantAndClient(String tenantCode, String clientSupport){
        return APIResponse.success(thirdTenantService.findByTenantCodeAndClientSupport(tenantCode,clientSupport));
    }
}

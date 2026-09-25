/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.service;

import com.cosmo.hhim.external.mapper.HyzzThirdInterfaceTenantMapper;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzThirdInterfaceTenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ThirdTenantService {
    private static final Logger logger = LoggerFactory.getLogger(ThirdTenantService.class);

    @Resource
    private HyzzThirdInterfaceTenantMapper thirdInterfaceTenantMapper;

    public HyzzThirdInterfaceTenant findByTenantCodeAndClientSupport(String tenantCode, String clientSupport){
        return thirdInterfaceTenantMapper.findInfoByTenantCodeAndClient(tenantCode,clientSupport);
    }

}

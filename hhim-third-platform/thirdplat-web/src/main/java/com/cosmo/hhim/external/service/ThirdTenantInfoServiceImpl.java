/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.service;

import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.tenant.ThirdTenantClientInfoService;
import org.springframework.stereotype.Service;

@Service
public class ThirdTenantInfoServiceImpl implements ThirdTenantClientInfoService {

    @Override
    public ThirdClientTenant getThirdTenantInfo(String tenantCode, String clientSupport) {
        //TODO 从缓存中获取租户信息(mysql兜底)
        return new ThirdClientTenant();
    }
}

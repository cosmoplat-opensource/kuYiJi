/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.tenant;

import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;

public interface ThirdTenantClientInfoService {
    /**
     * 从缓存中或DB中获取第三方租户请求参数
     *
     * @param tenantCode    租户编码
     * @param clientSupport 租户平台渠道(用友/金蝶/海云智造)
     * @return
     */
    ThirdClientTenant getThirdTenantInfo(String tenantCode, String clientSupport);
}

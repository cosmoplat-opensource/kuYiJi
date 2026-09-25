/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.util;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.tenant.ThirdTenantClientInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThirdYonyouU8Util {
    private static ThirdTenantClientInfoService clientInfoService;

    @Autowired
    public ThirdYonyouU8Util(ThirdTenantClientInfoService clientInfoService) {
        ThirdYonyouU8Util.clientInfoService = clientInfoService;
    }

    public static JSONObject getU8Info(String tenantCode) {
        ThirdClientTenant tenantInfo = clientInfoService.getThirdTenantInfo(tenantCode, "yonyouU8");
        JSONObject object = new JSONObject();
        object.putAll(tenantInfo.getAuthInfo());
        return object;
    }

}

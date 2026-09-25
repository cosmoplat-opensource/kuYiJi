/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.mapper;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzThirdInterfaceTenant;
import org.apache.ibatis.annotations.Param;

/**
 * 调用第三方接口租户配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-09-20
 */
public interface HyzzThirdInterfaceTenantMapper {

    /**
     * 获取租户三方订阅信息
     * @param tenantCode
     * @param clientSupport
     * @return
     */
    HyzzThirdInterfaceTenant findInfoByTenantCodeAndClient(@Param("tenantCode") String tenantCode, @Param("clientSupport") String clientSupport);
}

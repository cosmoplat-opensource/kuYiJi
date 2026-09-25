/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.mapper;

import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceMethodMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HyzzThirdInterfaceMethodMappingMapper {
    List<HyzzThirdInterfaceMethodMapping> selectClientSupportList(@Param("method") String method, @Param("version") String version, @Param("tenantCode") String tenantCode);
}

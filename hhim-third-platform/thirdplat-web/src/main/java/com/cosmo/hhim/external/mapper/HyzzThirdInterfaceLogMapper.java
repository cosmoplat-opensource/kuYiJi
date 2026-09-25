/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.mapper;

import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLog;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLogDetail;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceRetry;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HyzzThirdInterfaceLogMapper {

    int insertLog(HyzzThirdInterfaceLog hyzzThirdInterfaceLog);

    int selectByRequestId(String requestId, String clientSupport);

    int insertDetailLog(HyzzThirdInterfaceLogDetail detail);

    ThirdInterfaceTenant getTenantInfo(@Param("tenantCode") String tenantCode, @Param("strategy") String strategy);

    List<HyzzThirdInterfaceRetry> getScheduleRetryTask(Integer shardIndex, Integer shardTotal);
}

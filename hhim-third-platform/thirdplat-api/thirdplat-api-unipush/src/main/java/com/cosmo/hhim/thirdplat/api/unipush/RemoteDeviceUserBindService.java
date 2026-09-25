/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.DeviceUserBindInfo;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.DeviceUserBind;
import com.cosmo.hhim.thirdplat.api.unipush.factory.RemoteDeviceUserBindFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-10-09
 */
@FeignClient(contextId = "remoteDeviceUserBindService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteDeviceUserBindFallbackFactory.class)
public interface RemoteDeviceUserBindService {
    @PutMapping("/manager/bind/reset")
    APIResponse<Boolean> resetBind(DeviceUserBindInfo bindInfo);

    @GetMapping("/manager/bind/findByUserName")
    APIResponse<List<DeviceUserBind>> findByUserName(@RequestParam("userName") List<String> userName);
}

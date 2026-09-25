/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.factory;

import com.cosmo.hhim.thirdplat.api.unipush.RemoteDeviceUserBindService;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.DeviceUserBindInfo;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.DeviceUserBind;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-10-09
 */
@Slf4j
@Component
public class RemoteDeviceUserBindFallbackFactory implements FallbackFactory<RemoteDeviceUserBindService> {
    @Override
    public RemoteDeviceUserBindService create(Throwable throwable) {
        return new RemoteDeviceUserBindService() {
            @Override
            public APIResponse<Boolean> resetBind(DeviceUserBindInfo bindInfo) {
                return APIResponse.fail("重置设备绑定的用户账号操作失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<DeviceUserBind>> findByUserName(List<String> userName) {
                return APIResponse.fail("查询设备id失败：" + throwable, 500);
            }
        };
    }
}

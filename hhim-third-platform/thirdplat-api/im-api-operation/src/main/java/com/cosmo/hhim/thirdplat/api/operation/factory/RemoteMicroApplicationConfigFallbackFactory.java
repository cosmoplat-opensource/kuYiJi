/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.factory;

import com.cosmo.hhim.thirdplat.api.operation.RemoteMicroApplicationConfigService;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzMicroApplicationConfig;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/8
 */
public class RemoteMicroApplicationConfigFallbackFactory implements FallbackFactory<RemoteMicroApplicationConfigService> {
    @Override
    public RemoteMicroApplicationConfigService create(Throwable throwable) {
        return new RemoteMicroApplicationConfigService() {
            @Override
            public APIResponse<HyzzMicroApplicationConfig> getApplicationConfigByAppCode(String appCode) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzMicroApplicationConfig> getApplicationConfigByAppSign(String appSign) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzMicroApplicationConfig>> getApplicationConfigAllMapping() {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzMicroApplicationConfig>> getAllAppDetailConfigForCurrentTenant(Integer productType) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }
        };
    }
}

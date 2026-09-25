/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.factory;

import com.cosmo.hhim.thirdplat.api.operation.RemoteThirdTaskService;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;

public class RemoteThirdTaskFallbackFactory implements FallbackFactory<RemoteThirdTaskService> {

    @Override
    public RemoteThirdTaskService create(Throwable throwable) {
        return (shardIndex, shardTotal) -> APIResponse.fail("调用远程服务失败：" + throwable, 500);
    }
}

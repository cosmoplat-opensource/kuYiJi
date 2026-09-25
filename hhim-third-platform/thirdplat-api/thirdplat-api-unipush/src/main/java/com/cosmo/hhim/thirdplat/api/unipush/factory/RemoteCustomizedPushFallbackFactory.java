/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.factory;

import com.cosmo.hhim.thirdplat.api.unipush.RemoteCustomizedPushService;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.StandardMessage;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-28
 */
@Slf4j
@Component
public class RemoteCustomizedPushFallbackFactory implements FallbackFactory<RemoteCustomizedPushService> {
    @Override
    public RemoteCustomizedPushService create(Throwable throwable) {
        return new RemoteCustomizedPushService() {
            @Override
            public APIResponse<Boolean> userStandardPush(StandardMessage message) {
                return APIResponse.fail("按照用户账号进行标准模版推送失败：" + throwable, 500);
            }
        };
    }
}

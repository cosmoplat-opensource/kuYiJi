/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.factory;

import com.cosmo.hhim.thirdplat.api.operation.RemoteCustomerFaqService;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqQueryEntity;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;

/**
 * @author cosmo-hhim-open Team
 */
public class RemoteCustomerFaqFallbackFactory implements FallbackFactory<RemoteCustomerFaqService> {
    @Override
    public RemoteCustomerFaqService create(Throwable throwable) {
        return new RemoteCustomerFaqService() {
            @Override
            public APIResponse listByRoleOrIndex(HyzzFaqQueryEntity queryEntity) {
                return APIResponse.fail("调用远程服务，获取问答数据失败：" + throwable, 500);
            }

            @Override
            public APIResponse getIndexByRole(HyzzFaqQueryEntity entity) {
                return APIResponse.fail("调用远程服务，获取问答数据失败：" + throwable, 500);
            }

            @Override
            public APIResponse getFixedAnswer(Long qId) {
                return APIResponse.fail("调用远程服务，获取问答数据失败：" + throwable, 500);
            }

            @Override
            public APIResponse postQuestion(HyzzFaqQueryEntity entity) {
                return APIResponse.fail("调用远程服务，获取问答数据失败：" + throwable, 500);
            }

            @Override
            public APIResponse segmentContent(HyzzFaqQueryEntity entity) {
                return APIResponse.fail("调用远程服务，获取问答数据失败：" + throwable, 500);
            }
        };
    }


}

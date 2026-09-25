/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.factory;

import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.thirdplat.api.operation.RemoteJobService;
import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description
 * @createTime 2021-10-26
 */
public class RemoteJobFallbackFactory implements FallbackFactory<RemoteJobService> {

    @Override
    public RemoteJobService create(Throwable throwable) {
        return new RemoteJobService() {

            @Override
            public APIResponse<TableDataInfo> list(HyzzCustomJob hyzzCustomJob) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzCustomJob> getInfo(Long id) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzCustomJob> add(HyzzCustomJob hyzzCustomJob) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Integer> edit(HyzzCustomJob hyzzCustomJob) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Integer> remove(Long[] ids) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Integer> edit(List<HyzzCustomJob> hyzzCustomJob) {
                return APIResponse.fail("调用远程服务失败：" + throwable, 500);
            }
        };
    }
}

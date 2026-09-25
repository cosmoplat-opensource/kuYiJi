/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.factory;

import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.thirdplat.api.operation.RemoteCustomerSuggestService;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestion;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-10-26
 */
public class RemoteCustomerSuggestFallbackFactory implements FallbackFactory<RemoteCustomerSuggestService> {
    @Override
    public RemoteCustomerSuggestService create(Throwable throwable) {
        return new RemoteCustomerSuggestService() {


            @Override
            public APIResponse<TableDataInfo> list(HyzzPortalSuggestion hyzzPortalSuggestion) {
                return APIResponse.fail("调用远程服务，获取意见反馈列表失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzPortalSuggestion> getInfo(Long id) {
                return APIResponse.fail("调用远程服务，获取意见反馈详情失败：" + throwable, 500);
            }

            @Override
            public APIResponse<TableDataInfo> chatHistory(HyzzPortalSuggestion hyzzPortalSuggestion) {
                return APIResponse.fail("调用远程服务，获取意见反馈详情失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzPortalSuggestion> add(HyzzPortalSuggestion hyzzPortalSuggestion) {
                return APIResponse.fail("调用远程服务，新增意见反馈失败：" + throwable, 500);
            }
        };
    }


}

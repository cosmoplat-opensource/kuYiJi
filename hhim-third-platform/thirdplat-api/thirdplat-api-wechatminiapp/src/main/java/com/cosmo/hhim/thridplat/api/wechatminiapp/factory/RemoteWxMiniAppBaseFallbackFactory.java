/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.factory;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppBaseService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@Slf4j
public class RemoteWxMiniAppBaseFallbackFactory implements FallbackFactory<RemoteWxMiniAppBaseService> {
    @Override
    public RemoteWxMiniAppBaseService create(Throwable throwable) {
        return new RemoteWxMiniAppBaseService() {
            @Override
            public APIResponse<Code2SessionInfo> wxMiniAppLoginIn(String jsCode) {
                return APIResponse.fail("调用远程服务，调用微信小程序登录Feign接口失败：" + throwable, 500);
            }
        };
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.factory;

import com.cosmo.hhim.thirdplat.api.wechatmp.RemoteWxBaseService;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Slf4j
public class RemoteWxBaseFallbackFactory implements FallbackFactory<RemoteWxBaseService> {
    @Override
    public RemoteWxBaseService create(Throwable throwable) {
        return new RemoteWxBaseService() {
            @Override
            public APIResponse<Boolean> checkSignature(String timestamp, String nonce, String signature) {
                return APIResponse.fail("调用远程服务，调用微信服务器接入验签Feign接口失败：" + throwable, 500);
            }
        };
    }
}

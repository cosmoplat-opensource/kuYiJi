/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.factory;

import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppUserInfoService;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Slf4j
public class RemoteWxMiniAppUserInfoFallbackFactory implements FallbackFactory<RemoteWxMiniAppUserInfoService> {
    @Override
    public RemoteWxMiniAppUserInfoService create(Throwable throwable) {
        return null;
    }
}

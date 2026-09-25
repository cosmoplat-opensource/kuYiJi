/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.factory;

import com.cosmo.hhim.thirdplat.api.wechatmp.RemoteWxMenuService;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Slf4j
public class RemoteWxMenuFallbackFactory implements FallbackFactory<RemoteWxMenuService> {
    @Override
    public RemoteWxMenuService create(Throwable throwable) {
        return null;
    }
}

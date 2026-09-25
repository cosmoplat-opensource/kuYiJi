/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.factory;

import com.cosmo.hhim.thirdplat.api.wechatmp.RemoteWxUserService;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxMpUserInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Slf4j
public class RemoteWxUserFallbackFactory implements FallbackFactory<RemoteWxUserService> {
    @Override
    public RemoteWxUserService create(Throwable throwable) {
        return new RemoteWxUserService() {
            @Override
            public APIResponse<WxMpUserInfoResult> getMpUserInfo(String openid) {
                return APIResponse.fail("调用远程服务，通过openID查询微信粉丝基本用户信息失败：" + throwable, 500);
            }
        };
    }
}

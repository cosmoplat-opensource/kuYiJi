/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.factory;

import com.cosmo.hhim.thirdplat.api.wechatmp.RemoteWxWebPageService;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.TmsWebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParamV2;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageUserInfoParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageAccessTokenResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageUserInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxJsSdkConfigResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Slf4j
public class RemoteWxWebPageFallbackFactory implements FallbackFactory<RemoteWxWebPageService> {
    @Override
    public RemoteWxWebPageService create(Throwable throwable) {
        return new RemoteWxWebPageService() {
            @Override
            public APIResponse<String> getWebPageAuthorizeUrl(WebPageAuthorizeUrlParam param) {
                return APIResponse.fail("调用远程服务，获取网页授权微信跳转的URL（通用tenantCode）失败：" + throwable, 500);
            }

            @Override
            public APIResponse<String> getWebPageAuthorizeUrlV2(WebPageAuthorizeUrlParamV2 param) {
                return APIResponse.fail("调用远程服务，获取网页授权微信跳转的URL（通用AppId）失败：" + throwable, 500);
            }

            @Override
            public APIResponse<String> getTmsWebPageAuthorizeUrl(TmsWebPageAuthorizeUrlParam param) {
                return APIResponse.fail("调用远程服务，获取网页授权微信跳转的URL（TMS专用）失败：" + throwable, 500);
            }

            @Override
            public APIResponse<WebPageAccessTokenResult> getAccessToken(String code) {
                return APIResponse.fail("调用远程服务，获取网页授权token失败：" + throwable, 500);
            }

            @Override
            public APIResponse<WebPageUserInfoResult> getUserInfo(WebPageUserInfoParam param) {
                return APIResponse.fail("调用远程服务，获取用户基本信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<WxJsSdkConfigResult> getWxJsSdkConfig(String url) {
                return APIResponse.fail("调用远程服务，获取微信jsSdk的配置信息失败：" + throwable, 500);
            }
        };
    }
}

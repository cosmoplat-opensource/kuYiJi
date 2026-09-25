/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.TmsWebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParamV2;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageUserInfoParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageAccessTokenResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageUserInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxJsSdkConfigResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.factory.RemoteWxWebPageFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@FeignClient(contextId = "RemoteWxWebPageService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxWebPageFallbackFactory.class)
public interface RemoteWxWebPageService {
    /**
     * 获取网页授权微信跳转的URL（通用tenantCode）
     *
     * @return
     */
    @GetMapping("/wechatmp/web/authorizeUrl")
    APIResponse<String> getWebPageAuthorizeUrl(@SpringQueryMap WebPageAuthorizeUrlParam param);

    /**
     * 获取网页授权微信跳转的URL（通用AppId）
     *
     * @return
     */
    @GetMapping("/wechatmp/web/authorizeUrlV2")
    APIResponse<String> getWebPageAuthorizeUrlV2(@SpringQueryMap WebPageAuthorizeUrlParamV2 param);

    /**
     * 获取网页授权微信跳转的URL(TMS专用)
     *
     * @return
     */
    @GetMapping("/wechatmp/web/tmsauthorizeUrl")
    APIResponse<String> getTmsWebPageAuthorizeUrl(@SpringQueryMap TmsWebPageAuthorizeUrlParam param);

    /**
     * 获取网页授权token
     *
     * @param code
     * @return
     */
    @GetMapping("/wechatmp/web/token")
    APIResponse<WebPageAccessTokenResult> getAccessToken(@RequestParam("code") String code);

    /**
     * 获取用户基本信息
     *
     * @param param
     * @return
     */
    @GetMapping("/wechatmp/web/userinfo")
    APIResponse<WebPageUserInfoResult> getUserInfo(@SpringQueryMap WebPageUserInfoParam param);

    /**
     * 获取微信jsSdk的配置信息
     * @return的
     */
    @GetMapping("/wechatmp/web/getWxJsSdkConfig")
    APIResponse<WxJsSdkConfigResult> getWxJsSdkConfig(@RequestParam("url") String url);
}

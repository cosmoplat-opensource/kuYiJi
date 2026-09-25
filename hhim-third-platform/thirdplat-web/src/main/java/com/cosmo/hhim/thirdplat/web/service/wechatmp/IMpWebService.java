/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.TmsWebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParamV2;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageUserInfoParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageAccessTokenResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageUserInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxJsSdkConfigResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

import javax.validation.Valid;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
public interface IMpWebService {

    /**
     * 获取微信网页授权URL(通用tenantCode)
     * @param param
     * @return
     */
    APIResponse<String> getWebPageAuthorizeUrl(WebPageAuthorizeUrlParam param);

    /**
     * 获取微信网页授权URL(通用AppId)
     * @param param
     * @return
     */
    APIResponse<String> getWebPageAuthorizeUrlV2(WebPageAuthorizeUrlParamV2 param);

    /**
     * 获取微信网页授权URL(TMS专用)
     * @param param
     * @return
     */
    APIResponse<String> getTmsWebPageAuthorizeUrl(TmsWebPageAuthorizeUrlParam param);

    /**
     * 获取网页授权token
     * @param code
     * @return
     */
    APIResponse<WebPageAccessTokenResult> getAccessToken(String code);

    /**
     * 获取用户基本信息
     * @param param
     * @return
     */
    APIResponse<WebPageUserInfoResult> getUserInfo(WebPageUserInfoParam param);

    /**
     * 获取微信jsSdk配置信息
     * @return
     */
    APIResponse<WxJsSdkConfigResult> getWxJsSdkConfig(String url);

}

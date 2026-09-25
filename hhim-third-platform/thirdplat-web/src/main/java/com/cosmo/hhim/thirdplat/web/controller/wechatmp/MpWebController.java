/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.TmsWebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParamV2;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageUserInfoParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageAccessTokenResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageUserInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxJsSdkConfigResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@RestController
@RequestMapping("/wechatmp/web")
public class MpWebController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMpWebService mpWebService;

    /**
     * 获取网页授权微信跳转的URL(通用tenantCode)
     *
     * @return
     */
    @GetMapping("/authorizeUrl")
    public APIResponse<String> getWebPageAuthorizeUrl(@Valid WebPageAuthorizeUrlParam param) {
        return mpWebService.getWebPageAuthorizeUrl(param);
    }

    /**
     * 获取网页授权微信跳转的URL(通用AppId)
     *
     * @return
     */
    @GetMapping("/authorizeUrlV2")
    public APIResponse<String> getWebPageAuthorizeUrlV2(@Valid WebPageAuthorizeUrlParamV2 param) {
        return mpWebService.getWebPageAuthorizeUrlV2(param);
    }

    /**
     * 获取网页授权微信跳转的URL(TMS专用)
     *
     * @return
     */
    @GetMapping("/tmsauthorizeUrl")
    public APIResponse<String> getTmsWebPageAuthorizeUrl(@Valid TmsWebPageAuthorizeUrlParam param) {
        return mpWebService.getTmsWebPageAuthorizeUrl(param);
    }

    /**
     * 获取网页授权token
     *
     * @param code
     * @return
     */
    @GetMapping("/token")
    public APIResponse<WebPageAccessTokenResult> getAccessToken(String code) {
        return mpWebService.getAccessToken(code);
    }

    /**
     * 获取用户基本信息
     * @param param
     * @return
     */
    @GetMapping("/userinfo")
    public APIResponse<WebPageUserInfoResult> getUserInfo(@Valid WebPageUserInfoParam param){
        return mpWebService.getUserInfo(param);
    }

    /**
     * 获取微信jsSdk的配置信息
     * @return的
     */
    @GetMapping("/getWxJsSdkConfig")
    public APIResponse<WxJsSdkConfigResult> getWxJsSdkConfig(String url){
        return mpWebService.getWxJsSdkConfig(url);
    }


}

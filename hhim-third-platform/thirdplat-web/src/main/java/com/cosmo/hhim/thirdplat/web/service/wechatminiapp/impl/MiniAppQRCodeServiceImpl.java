/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.api.WxAppQRCodeApi;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppQRCodeService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.QRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SchemeCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UrlLinkParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.SchemeCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UrlLinkInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxMiniAppResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@Slf4j
@Service
public class MiniAppQRCodeServiceImpl implements IMiniAppQRCodeService {

    @Autowired
    private WxAppQRCodeApi wxAppQRCodeApi;


    /**
     * 获取小程序码
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<QRCodeInfo> getMiniAppQrCode(QRCodeParam param) {
        // 发起请求
        ForestResponse<WxAppResponseResult> response = wxAppQRCodeApi.getQRCode(param);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        // access_token 失效时自动重试一次（拦截器已清除 Redis 缓存并刷新 token）
        if (errorResponse != null && errorResponse.getCode() == 40001) {
            log.warn("access_token 失效(40001)，自动重试...");
            response = wxAppQRCodeApi.getQRCode(param);
            errorResponse = wxMiniAppResponseExceptionHandler(response);
        }
        if (null != errorResponse) {
            return errorResponse;
        }

        QRCodeInfo result = new QRCodeInfo();
        // 以字节数组的形式获取请求响应内容
        try {
            byte[] buffer = response.getByteArray();
            result.setBuffer(buffer);
        } catch (Exception e) {
            log.error("Failed to get buffer from the response:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }

        return APIResponse.success(result);
    }

    /**
     * 获取小程序码（无限制）
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<QRCodeInfo> getMiniAppUnLimitQrCode(UnlimitedQRCodeParam param) {
        // 发起请求
        ForestResponse<WxAppResponseResult> response = wxAppQRCodeApi.getUnlimitedQRCode(param);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        // access_token 失效时自动重试一次（拦截器已清除 Redis 缓存并刷新 token）
        if (errorResponse != null && errorResponse.getCode() == 40001) {
            log.warn("access_token 失效(40001)，自动重试...");
            response = wxAppQRCodeApi.getUnlimitedQRCode(param);
            errorResponse = wxMiniAppResponseExceptionHandler(response);
        }
        if (null != errorResponse) {
            return errorResponse;
        }

        QRCodeInfo result = new QRCodeInfo();
        // 以字节数组的形式获取请求响应内容
        try {
            byte[] buffer = response.getByteArray();
            result.setBuffer(buffer);
        } catch (Exception e) {
            log.error("Failed to get buffer from the response:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }

        return APIResponse.success(result);
    }

    /**
     * 获取微信小程序Url Link
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<UrlLinkInfo> getMiniAppUrlLink(UrlLinkParam param) {
        // 发起请求
        ForestResponse<UrlLinkInfo> response = wxAppQRCodeApi.generateUrlLink(param);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        // access_token 失效时自动重试一次（拦截器已清除 Redis 缓存并刷新 token）
        if (errorResponse != null && errorResponse.getCode() == 40001) {
            log.warn("access_token 失效(40001)，自动重试...");
            response = wxAppQRCodeApi.generateUrlLink(param);
            errorResponse = wxMiniAppResponseExceptionHandler(response);
        }
        if (null != errorResponse) {
            return errorResponse;
        }

        return APIResponse.success(response.getResult());
    }

    /**
     * 获取微信小程序scheme
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<SchemeCodeInfo> getMiniAppScheme(SchemeCodeParam param) {
        // 发起请求
        ForestResponse<SchemeCodeInfo> response = wxAppQRCodeApi.generateScheme(param);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        // access_token 失效时自动重试一次（拦截器已清除 Redis 缓存并刷新 token）
        if (errorResponse != null && errorResponse.getCode() == 40001) {
            log.warn("access_token 失效(40001)，自动重试...");
            response = wxAppQRCodeApi.generateScheme(param);
            errorResponse = wxMiniAppResponseExceptionHandler(response);
        }
        if (null != errorResponse) {
            return errorResponse;
        }

        return APIResponse.success(response.getResult());
    }
}

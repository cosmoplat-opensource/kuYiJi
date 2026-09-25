/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.utils;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.wechatmp.response.WxResponseResult;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.http.ForestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-07
 */
public class ResponseParserUtil {

    private static final Logger log = LoggerFactory.getLogger(ResponseParserUtil.class);

    /**
     * wx接口响应失败，响应结果封装处理
     */
    public static APIResponse wxResponseExceptionHandler(ForestResponse response) {
        if (response.isError()) {
            String content = response.getContent();
            log.error("[ResponseParser] 微信接口响应异常, isError=true, content={}", content);
            if (StringUtils.hasText(content)) {
                WxResponseResult responseResult = JSON.parseObject(content, WxResponseResult.class);
                log.error("[ResponseParser] 解析微信错误响应, errcode={}, errmsg={}", responseResult.getErrcode(), responseResult.getErrmsg());
                return APIResponse.fail(responseResult.getErrmsg(), responseResult.getErrcode());
            }
            log.error("[ResponseParser] 微信接口响应异常但content为空, 走兜底返回");
            return APIResponse.fail("微信接口请求失败", 500);
        }
        WxResponseResult responseResult = (WxResponseResult) response.getResult();
        if (null != responseResult && !responseResult.isSuccess()) {
            log.error("[ResponseParser] 微信接口返回业务失败, errcode={}, errmsg={}", responseResult.getErrcode(), responseResult.getErrmsg());
            return APIResponse.fail(responseResult.getErrmsg(), responseResult.getErrcode());
        }
        return null;
    }

    /**
     * wxMiniApp接口响应失败，响应结果封装处理
     */
    public static APIResponse wxMiniAppResponseExceptionHandler(ForestResponse response) {
        if (response.isError()) {
            String content = response.getContent();
            log.error("[ResponseParser] 微信小程序接口响应异常, isError=true, content={}", content);
            if (StringUtils.hasText(content)) {
                WxAppResponseResult responseResult = JSON.parseObject(content, WxAppResponseResult.class);
                return APIResponse.fail(responseResult.getErrmsg(), responseResult.getErrcode());
            }
            log.error("[ResponseParser] 微信小程序接口响应异常但content为空, 走兜底返回");
            return APIResponse.fail("微信小程序接口请求失败", 500);
        }
        // 成功响应：getResult() 可能是 WxAppResponseResult（JSON接口）或 String（二进制接口如 getUnlimitQrCode）
        // 只有确实是 WxAppResponseResult 时才做业务错误检查，否则直接返回 null（无错误）
        Object result = response.getResult();
        if (result instanceof WxAppResponseResult) {
            WxAppResponseResult responseResult = (WxAppResponseResult) result;
            if (!responseResult.isSuccess()) {
                log.error("[ResponseParser] 微信小程序接口返回业务失败, errcode={}, errmsg={}", responseResult.getErrcode(), responseResult.getErrmsg());
                return APIResponse.fail(responseResult.getErrmsg(), responseResult.getErrcode());
            }
        }
        return null;
    }

    /**
     * unipush接口响应失败，响应结果封装处理
     */
    public static APIResponse unipushResponseExceptionHandler(ForestResponse response) {
        if (response.isError()) {
            String content = response.getContent();
            log.error("[ResponseParser] unipush接口响应异常, isError=true, content={}", content);
            if (StringUtils.hasText(content)) {
                UniPushResponseResult responseResult = JSON.parseObject(content, UniPushResponseResult.class);
                return APIResponse.fail(responseResult.getMsg(), responseResult.getCode());
            }
        }
        UniPushResponseResult responseResult = (UniPushResponseResult) response.getResult();
        if (null != responseResult && !responseResult.isSuccess()) {
            return APIResponse.fail(responseResult.getMsg(), responseResult.getCode());
        }
        return null;
    }
}

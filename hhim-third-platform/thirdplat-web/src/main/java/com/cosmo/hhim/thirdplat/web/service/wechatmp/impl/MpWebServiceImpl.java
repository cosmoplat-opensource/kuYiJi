/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.TmsWebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageAuthorizeUrlParamV2;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.WebPageUserInfoParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageAccessTokenResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WebPageUserInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxJsSdkConfigResult;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.core.config.ForestGlobalConfig;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxJsSDKApi;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxWebPageApi;
import com.cosmo.hhim.thirdplat.modules.wechatmp.constants.WebUrlConfigConstant;
import com.cosmo.hhim.thirdplat.modules.wechatmp.constants.WechatMpConstant;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.GetUserInfoInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetJsSdkTicketOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetWebUserInfoOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.WebAccessTokenInfo;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpWebService;
import com.dtflys.forest.http.ForestResponse;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@Service
public class MpWebServiceImpl implements IMpWebService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ForestGlobalConfig forestGlobalConfig;

    @Autowired
    private WxWebPageApi wxWebPageApi;

    @Autowired
    private WxJsSDKApi wxJsSDKApi;

    // URL分隔符
    private static final String URL_SEGMENT = "/";

    /**
     * 清洗授权 state 参数：微信文档要求 state 为 a-zA-Z0-9 且最长128字节，
     * 仅保留安全字符集，防止拼接进授权 URL 时产生注入/越界内容
     */
    private static String sanitizeState(String state) {
        if (state == null || state.length() == 0) {
            return state;
        }
        String cleaned = state.replaceAll("[^a-zA-Z0-9_-]", "");
        if (cleaned.length() > 128) {
            cleaned = cleaned.substring(0, 128);
        }
        return cleaned;
    }

    // 生成随机字符串用的
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 获取微信网页授权URL（通用tenantCode）
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<String> getWebPageAuthorizeUrl(WebPageAuthorizeUrlParam param) {
        try {
            String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);

            // 通过appId查询redis缓存中的公众号配置
            String wechatMpConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY));
            if (StringUtils.hasText(wechatMpConfigStr)) {
                HyzzWechatConfig wechatConfig = JSON.parseObject(wechatMpConfigStr, HyzzWechatConfig.class);
                String webPageUrl = forestGlobalConfig.getVariables().get(WechatMpConstant.WXMP_WEBPAGE_URL);
                String encodeRedirectUri = URLEncoder.encode(param.getRedirectUri(), "UTF-8");
                String scope = param.getWebPageScopeEnum().getKey();
                String resultUrl = String.format(WebUrlConfigConstant.WX_MP_WEBPAGE_AUTH_URL, webPageUrl, appId, encodeRedirectUri, scope, sanitizeState(param.getState()));
                log.info("微信公众号授权URL:{}", resultUrl);
                return APIResponse.success(resultUrl);
            }
        } catch (Exception e) {
            log.error("获取微信公众号网页授权URL发生异常：{}", Throwables.getStackTraceAsString(e));
        }
        return APIResponse.success(null);
    }

    /**
     * 获取微信网页授权URL(通用AppId)
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<String> getWebPageAuthorizeUrlV2(WebPageAuthorizeUrlParamV2 param) {
        try {
            String webPageUrl = forestGlobalConfig.getVariables().get(WechatMpConstant.WXMP_WEBPAGE_URL);
            String appId = param.getAppId();
            String encodeRedirectUri = URLEncoder.encode(param.getRedirectUri(), "UTF-8");
            String scope = param.getWebPageScopeEnum().getKey();
            String resultUrl = String.format(WebUrlConfigConstant.WX_MP_WEBPAGE_AUTH_URL, webPageUrl, appId, encodeRedirectUri, scope, sanitizeState(param.getState()));
            log.info("微信公众号授权URL:{}", resultUrl);
            return APIResponse.success(resultUrl);
        } catch (Exception e) {
            log.error("获取微信公众号网页授权URL发生异常：{}", Throwables.getStackTraceAsString(e));
        }
        return APIResponse.success(null);
    }


    /**
     * 获取微信网页授权URL（TMS专用）
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<String> getTmsWebPageAuthorizeUrl(TmsWebPageAuthorizeUrlParam param) {
        try {
            String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);

            // 通过appId查询redis缓存中的公众号配置
            String wechatMpConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY));
            if (StringUtils.hasText(wechatMpConfigStr)) {
                HyzzWechatConfig wechatConfig = JSON.parseObject(wechatMpConfigStr, HyzzWechatConfig.class);
                String webPageUrl = forestGlobalConfig.getVariables().get(WechatMpConstant.WXMP_WEBPAGE_URL);

                // URL拼接识别信息
                String redirectUri = param.getRedirectUri() +
                        URL_SEGMENT + appId +
                        URL_SEGMENT + param.getUsername() +
                        URL_SEGMENT + param.getUserIdentity() +
                        URL_SEGMENT;
                String encodeRedirectUri = URLEncoder.encode(redirectUri, "UTF-8");
                String scope = param.getWebPageScopeEnum().getKey();
                String resultUrl = String.format(WebUrlConfigConstant.WX_MP_WEBPAGE_AUTH_URL, webPageUrl, appId, encodeRedirectUri, scope, sanitizeState(param.getState()));
                log.info("微信公众号授权URL:{}", resultUrl);
                return APIResponse.success(resultUrl);
            }
        } catch (Exception e) {
            log.error("获取微信公众号网页授权URL发生异常：{}", Throwables.getStackTraceAsString(e));
        }
        return APIResponse.success(null);
    }

    /**
     * 网页授权token
     *
     * @param code
     * @return
     */
    @Override
    public APIResponse<WebPageAccessTokenResult> getAccessToken(String code) {
        // 发起请求
        ForestResponse<WebAccessTokenInfo> response = wxWebPageApi.getWebAccessToken(code);
        log.info("getWebAccessToken---result:{}", JSON.toJSONString(response.getResult()));
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        WebPageAccessTokenResult result = new WebPageAccessTokenResult();
        WebAccessTokenInfo webAccessTokenInfo = response.getResult();
        result.setAccessToken(webAccessTokenInfo.getAccess_token());
        result.setRefreshToken(webAccessTokenInfo.getRefresh_token());
        result.setExpireTime(webAccessTokenInfo.getExpires_in());
        result.setOpenId(webAccessTokenInfo.getOpenid());
        result.setScope(webAccessTokenInfo.getScope());
        return APIResponse.success(result);
    }

    /**
     * 获取用户基本信息
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<WebPageUserInfoResult> getUserInfo(WebPageUserInfoParam param) {
        // 组装请求参数
        GetUserInfoInDto getUserInfoInDto = new GetUserInfoInDto();
        getUserInfoInDto.setOpenid(param.getOpenId());
        getUserInfoInDto.setLang(WechatMpConstant.Lang.ZH_CN.getKey());

        // 发起请求
        ForestResponse<GetWebUserInfoOutDto> response = wxWebPageApi.getUserInfo(param.getAccessToken(), getUserInfoInDto);
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        WebPageUserInfoResult result = new WebPageUserInfoResult();
        BeanUtils.copyProperties(response.getResult(), result);
        return APIResponse.success(result);
    }

    /**
     * 获取微信jsSdk配置信息
     *
     * @return
     */
    @Override
    public APIResponse<WxJsSdkConfigResult> getWxJsSdkConfig(String url) {
        log.info("[微信JS-SDK] 开始获取JS-SDK配置, url={}", url);

        // 1.公众号appid
        String appId = (String) ThreadLocalCache.getCache(Constant.WXMP_APPID);
        log.info("[微信JS-SDK] ThreadLocalCache中获取appId, appId={}", appId);
//        String wechatMpConfigStr = redisTemplate.opsForValue().get(appId + ":" + Constant.RedisKeys.WECHATMP.value(Constant.WECHAT_MP_CONFIG_REDIS_KEY));
//        if (StringUtils.hasText(wechatMpConfigStr)) {
//            HyzzWechatConfig wechatConfig = JSON.parseObject(wechatMpConfigStr, HyzzWechatConfig.class);
//            appId = wechatConfig.getAppId();
//        }

        // 2.生成签名的随机串（SecureRandom 防预测）
        String nonceStr = "";
        final int nonceStrLength = 16;
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < nonceStrLength; i++) {
            int beginIndex = secureRandom.nextInt(CHARS.length());
            nonceStr += CHARS.substring(beginIndex, beginIndex + 1);
        }

        // 3.生成签名的时间戳
        String timestamp = String.format("%010d", System.currentTimeMillis() / 1000);

        // 4. 生成签名
        String signature = "";

        // 4.1 获取微信js-sdk票据
        log.info("[微信JS-SDK] 开始调用微信接口获取jsapi_ticket");
        ForestResponse<GetJsSdkTicketOutDto> response = wxJsSDKApi.getJsSdkTicket();
        log.info("getJsSdkTicket----result:{}",JSON.toJSONString(response.getResult()));
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            log.error("[微信JS-SDK] 获取jsapi_ticket失败, errorResponse={}", errorResponse);
            return errorResponse;
        }
        GetJsSdkTicketOutDto ticketInfo = response.getResult();
        String ticket = ticketInfo.getTicket();
        log.info("[微信JS-SDK] 获取jsapi_ticket成功, ticket前缀={}",
                StringUtils.hasText(ticket) ? ticket.substring(0, Math.min(10, ticket.length())) + "..." : "null");

        // 4.2 将参数按照 key 值 ASCII 码升序排序
        String decript = "jsapi_ticket=" + ticket + "&noncestr=" + nonceStr + "&timestamp=" + timestamp + "&url=" + url;

        // 4.3 进行SHA1加密
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            signature = hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException :{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }

        WxJsSdkConfigResult result = new WxJsSdkConfigResult();
        result.setAppId(appId);
        result.setNonceStr(nonceStr);
        result.setTimestamp(timestamp);
        result.setSignature(signature);

        log.info("[微信JS-SDK] JS-SDK配置生成成功, appId={}, nonceStr={}, timestamp={}, signature前缀={}",
                appId, nonceStr, timestamp,
                StringUtils.hasText(signature) ? signature.substring(0, Math.min(10, signature.length())) + "..." : "null");
        return APIResponse.success(result);
    }


}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.web;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzMicroMiniappConfigMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzWechatConfigMapper;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.HyzzMicroMiniappConfig;
import com.plumelog.core.TraceId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
public class TenantWebFilter extends HandlerInterceptorAdapter {
    private String traceId;

    private final HyzzMicroMiniappConfigMapper hyzzMicroMiniappConfigMapper;
    private final HyzzWechatConfigMapper hyzzWechatConfigMapper;
    private final String defaultAppId;

    public TenantWebFilter(HyzzMicroMiniappConfigMapper hyzzMicroMiniappConfigMapper, HyzzWechatConfigMapper hyzzWechatConfigMapper, String defaultAppId) {
        this.hyzzMicroMiniappConfigMapper = hyzzMicroMiniappConfigMapper;
        this.hyzzWechatConfigMapper = hyzzWechatConfigMapper;
        this.defaultAppId = defaultAppId;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        traceId = request.getHeader(Constants.TRACEID);
        String tenantCode = request.getHeader(Constants.TARGET_CUSTOMER);
        String wxAppId = request.getHeader(Constants.WXMP_APPID);

        ThreadLocalCache.saveCache(Constant.TENANT_CODE, tenantCode);
        TraceId.logTraceID.set(traceId);
        ThreadContext.put(Constants.TRACEID, traceId);

        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>微信公众号缓存设置<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        log.debug("TenantWebFilter缓存设置：微信公众号AppId--------------------->beginning...，traceId:{},tenantCode:{},wxAppId:{}", traceId, tenantCode, wxAppId);

        // 设置微信公众号appId
        if (!StringUtils.hasText(wxAppId) && StringUtils.hasText(tenantCode)) {
            HyzzWechatConfig hyzzWechatConfig = hyzzWechatConfigMapper.selectHyzzWechatConfigByCustomerCode(tenantCode);
            if (null != hyzzWechatConfig) {
                wxAppId = hyzzWechatConfig.getAppId();
            } else {
                wxAppId = defaultAppId;
            }
        }

        if (!StringUtils.hasText(wxAppId) && !StringUtils.hasText(tenantCode)) {
            wxAppId = defaultAppId;
        }

        log.debug("TenantWebFilter缓存设置：微信公众号AppId--------------------->finished！！！traceId:{},tenantCode:{},wxAppId:{}", traceId, tenantCode, wxAppId);
        ThreadLocalCache.saveCache(Constant.WXMP_APPID, wxAppId);
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>微信公众号缓存设置<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>微信小程序缓存设置<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        String deviceType = request.getHeader(CacheConstants.DETAILS_TYPE);
        String applicationSign = request.getHeader(Constants.APPLICATION_SIGN);

        String wechatMiniAppId = null;
        if (CommonConstant.DeviceType.WXMINIAPP.getKey().equals(deviceType)) { // deviceType为wechatMiniApp时设置此缓存
            log.debug("TenantWebFilter缓存设置：微信小程序AppId-------->beginning..., traceId:{},tenantCode:{},deviceType:{},applicationSign:{}", traceId, tenantCode, deviceType, applicationSign);
            HyzzMicroMiniappConfig microMiniappConfig = hyzzMicroMiniappConfigMapper.selectMicroMiniAppConfigByCondition(applicationSign, deviceType);
            if (null != microMiniappConfig && StringUtils.hasText(microMiniappConfig.getAppId())) {
                wechatMiniAppId = microMiniappConfig.getAppId();
            }
            log.debug("TenantWebFilter缓存设置：微信小程序AppId------->finished！！！traceId:{},tenantCode:{},deviceType:{},applicationSign:{},miniAppId:{}", traceId, tenantCode, deviceType, applicationSign, wechatMiniAppId);
            ThreadLocalCache.saveCache(Constant.WXMINIAPP_APPID, wechatMiniAppId);
        }
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>微信小程序缓存设置<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalCache.clearCache();
    }
}

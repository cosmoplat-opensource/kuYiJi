/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.aop;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.github.pagehelper.PageHelper;
import com.plumelog.core.TraceId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

public class WebFilter implements HandlerInterceptor {


    private static final Logger log = LoggerFactory.getLogger(WebFilter.class);
    @Value("${hhim.envgroup}")
    private String envgroup;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o) throws Exception {
        long start = System.currentTimeMillis();

        request.setAttribute("startTime", start);
        String ds = request.getHeader(Constants.TARGET_DS);
        String schema = request.getHeader(Constants.TARGET_SCHEMA);
        String customer = request.getHeader(Constants.TARGET_CUSTOMER);
        String customerName = request.getHeader(Constants.TARGET_CUSTOMER_NAME);
        String userName = request.getHeader(CacheConstants.DETAILS_USERNAME);
        // 优先从请求头读（网关注入），decouple 后网关不再注入，fallback 到 request attribute（MicroAccessAuthFilterCondition 注入）
        String userId = request.getHeader(CacheConstants.DETAILS_USER_ID);
        if (StringUtils.isEmpty(userId)) {
            Object attrUserId = request.getAttribute(CacheConstants.DETAILS_USER_ID);
            if (attrUserId != null) {
                userId = attrUserId.toString();
            }
        }
        if (StringUtils.isNotEmpty(userId)) {
            ThreadContext.put(CacheConstants.DETAILS_USER_ID, userId);
        }
        String nickname = request.getHeader(CacheConstants.NICK_NAME);
        String authorization = request.getHeader(CacheConstants.AUTHORIZATION_HEADER);
        String type = request.getHeader(CacheConstants.TYPE);
        String traceId = request.getHeader(Constants.TRACEID);
        String trialPhone = request.getHeader(CacheConstants.TRIAL_PHONE);
        if (StringUtils.isEmpty(traceId) || !traceId.matches("^[a-zA-Z0-9_\\-]{1,64}$")) {
            // traceId 来自请求头不可信：仅允许字母/数字/下划线/中划线，防止响应头注入（CRLF）与异常字符
            traceId = UUID.randomUUID().toString();
        }
        TraceId.logTraceID.set(traceId);
        ThreadContext.put(Constants.TRACEID, traceId);
        if(StringUtils.isNotEmpty(customerName)){
            ThreadContext.put(Constants.TARGET_CUSTOMER_NAME, URLDecoder.decode(customerName,"UTF-8"));
        }
        if(StringUtils.isNotEmpty(trialPhone)){
            ThreadContext.put(CacheConstants.TRIAL_PHONE, trialPhone);
        }
        String mainAccountFlag = request.getHeader(Constants.MAIN_ACCOUNT_FLAG);
        if (StringUtils.isNotEmpty(mainAccountFlag)) {
            ThreadContext.put(Constants.MAIN_ACCOUNT_FLAG, mainAccountFlag);
        }
        //ThreadContext.put(Constants.SKYWALKING_TRACEID, TraceContext.traceId());
        if (CheckObjectUtils.isNotEmpty(request.getHeader(Constants.PAGE_NUM_FEIGN))) {
            ThreadContext.put(Constants.PAGE_NUM_FEIGN, request.getHeader(Constants.PAGE_NUM_FEIGN));
        }
        if (CheckObjectUtils.isNotEmpty(type)) {
            ThreadContext.put(CacheConstants.TYPE, request.getHeader(CacheConstants.TYPE));
        } else {
            ThreadContext.put(CacheConstants.TYPE, "NA");
        }
        if (CheckObjectUtils.isNotEmpty(request.getHeader(Constants.PAGE_SIZE_FEIGN))) {
            ThreadContext.put(Constants.PAGE_SIZE_FEIGN, request.getHeader(Constants.PAGE_SIZE_FEIGN));
        }

        // 全局安全响应头：禁止浏览器对响应内容做 MIME 嗅探，缓解 XSS/类型混淆攻击
        httpServletResponse.addHeader("X-Content-Type-Options", "nosniff");
        // traceId 已通过上方白名单校验（仅字母/数字/下划线/中划线），URLEncoder 对这些字符编码结果不变；
        // 使用 URLEncoder 显式编码，从代码层面消除响应头注入（CRLF）风险
        try {
            httpServletResponse.addHeader("traceId", URLEncoder.encode(traceId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // UTF-8 为 JVM 必备字符集，理论不可达；不在此回退设置未编码的原始值，避免响应头注入（CRLF）风险
            throw new IllegalStateException("UTF-8 encoding is not supported", e);
        }
        DBControlUtil.setDbAndSchema(ds, schema, customer, userName, StringUtils.isNotEmpty(userId) ? Long.parseLong(userId) : 0L, authorization, nickname);
        String idempotent = request.getHeader("idempotent");
        if (StringUtils.isNotEmpty(idempotent)&&!idempotent.equals(""+schema + customer)) {
           // log.error("mvc idempotent 不匹配");
        }
       // log.info(" customer:" + customer + " schema:" + schema + " username:" + userName + " token:" + authorization);

        return true;
    }




    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long start = (long) request.getAttribute("startTime");
        long end = System.currentTimeMillis();
        request.setAttribute("handleTime", end - start);
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        try {
            if (!"/healthCheck".equals(request.getRequestURI())) {
            }
        } catch (Exception exception) {
           // log.warn("mvc拦截器after异常", exception);
        } finally {
            TraceId.logTraceID.remove();
            ThreadContext.clear();
            PageHelper.clearPage();
        }


    }


}

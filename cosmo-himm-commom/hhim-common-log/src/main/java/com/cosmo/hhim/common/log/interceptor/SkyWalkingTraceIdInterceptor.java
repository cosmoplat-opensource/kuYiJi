/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.common.log.interceptor;
//
//import com.alibaba.fastjson.JSONObject;
//import com.plumelog.core.TraceId;
//import org.apache.logging.log4j.ThreadContext;
//import org.apache.skywalking.apm.toolkit.trace.TraceContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.lang.Nullable;
//import org.springframework.util.StreamUtils;
//import org.springframework.util.StringUtils;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * @author cosmo-hhim-open Team
// * @createTime 2021-08-13
// */
//public class SkyWalkingTraceIdInterceptor extends HandlerInterceptorAdapter {
//    protected final Logger logger = LoggerFactory.getLogger(SkyWalkingTraceIdInterceptor.class);
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        String traceId = TraceContext.traceId();
//        if (StringUtils.hasText(traceId) || "N/A".equals(traceId)) {
//            TraceId.logTraceID.set(traceId);
//        } else {
//            traceId = UUID.randomUUID().toString().replaceAll("-", "");
////            traceId= uuid.substring(uuid.length() - 7);
//            TraceId.logTraceID.set(traceId);
//        }
//        ThreadContext.put("traceId", traceId);
//        response.addHeader("traceId", traceId);
//        if (!"/healthCheck".equals(request.getRequestURI())) {
//            logger.info("请求路径:" + request.getRequestURI() + " 请求参数:" + JSONObject.toJSONString(request.getParameterMap()));
//        }
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
//                                @Nullable Exception ex) throws Exception {
//        TraceId.logTraceID.remove();
//    }
//
//}

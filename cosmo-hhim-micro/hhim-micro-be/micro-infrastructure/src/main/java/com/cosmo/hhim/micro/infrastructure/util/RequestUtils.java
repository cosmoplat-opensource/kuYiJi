/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.security.utils.HttpServletRequestUtil;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-03
 */
@Slf4j
public class RequestUtils {

    private static final int BUFFER_SIZE = 1024 * 8;

    /**
     * 提取请求头
     *
     * @param request
     * @return
     */
    public static Map<String, String> extractRequestHeader(HttpServletRequest request) {
        Map<String, String> requestHeaderMap = Maps.newHashMap();
        String deviceType = request.getHeader(CacheConstants.DETAILS_TYPE);
        String applicationSign = request.getHeader(Constants.APPLICATION_SIGN);
        String trialPhone = request.getHeader(CacheConstants.TRIAL_PHONE);
        requestHeaderMap.put(CacheConstants.DETAILS_TYPE, deviceType);
        requestHeaderMap.put(Constants.APPLICATION_SIGN, applicationSign);
        requestHeaderMap.put(CacheConstants.TRIAL_PHONE, trialPhone);

        log.info("请求头提取信息---> deviceType:{}, applicationSign:{}", deviceType, applicationSign);
        return requestHeaderMap;
    }

    /**
     * 添加Tomcat请求头信息
     *
     * @param headerMap
     */
    public static void addTomcatRequestHeaders(Map<String, String> headerMap) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            RequestFacade requestFacade = (RequestFacade) request;
            // 反射获取request属性（Spring ReflectionUtils 内部处理访问权限，无需显式 setAccessible）
            Field requestField = requestFacade.getClass().getDeclaredField("request");
            Object requestObj = ReflectionUtils.getField(requestField, requestFacade);
            // 获取coyoteRequest
            Field coyoteRequest = requestObj.getClass().getDeclaredField("coyoteRequest");
            Object obj = ReflectionUtils.getField(coyoteRequest, requestObj);
            Field headers = obj.getClass().getDeclaredField("headers");
            // 获取Header
            MimeHeaders mimeHeaders = (MimeHeaders) ReflectionUtils.getField(headers, obj);

            for (Iterator<Map.Entry<String, String>> iterator = headerMap.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> entry = iterator.next();
                mimeHeaders.addValue(entry.getKey()).setString(entry.getValue());
            }

        } catch (Exception e) {
            log.error("添加请求头信息失败! errorMsg:{}", Throwables.getStackTraceAsString(e));
            throw new CustomException("添加请求头信息失败！");
        }
    }


    /**
     * 添加请求头信息
     *
     * @param headerMap
     */
    public static void addRequestHeaders(Map<String, String> headerMap) {
        if (!CollectionUtils.isEmpty(headerMap)) {
            try {
                HttpServletRequestUtil.addCustomizedRequestHeaders(headerMap);
            } catch (ClassCastException e) {
                log.warn("未启用访问权限模块，采用Tomcat请求头添加方式处理...");
                addTomcatRequestHeaders(headerMap);
            }
        }
    }

    /**
     * 获取requestBody
     */
    public static String getRequestBody() throws IOException {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) attributes;
        assert servletRequestAttributes != null;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        return read(reader);
    }

    /**
     * read string.
     *
     * @param reader Reader instance.
     * @return String.
     * @throws IOException
     */
    public static String read(Reader reader) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            write(reader, writer);
            return writer.getBuffer().toString();
        }
    }

    /**
     * write.
     *
     * @param reader Reader.
     * @param writer Writer.
     * @return count.
     * @throws IOException
     */
    public static long write(Reader reader, Writer writer) throws IOException {

        return write(reader, writer, BUFFER_SIZE);
    }

    /**
     * write.
     *
     * @param reader     Reader.
     * @param writer     Writer.
     * @param bufferSize buffer size.
     * @return count.
     * @throws IOException
     */
    public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {

        int read;
        long total = 0;
        char[] buf = new char[bufferSize];
        while ((read = reader.read(buf)) != -1) {

            writer.write(buf, 0, read);
            total += read;
        }
        return total;
    }
}

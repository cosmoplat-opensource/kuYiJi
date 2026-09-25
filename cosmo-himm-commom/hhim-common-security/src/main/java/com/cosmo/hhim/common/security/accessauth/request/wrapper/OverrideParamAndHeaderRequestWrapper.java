/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.request.wrapper;

import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 可以重写request参数的HttpServletRequest的Wrapper类
 * @createTime 2022-10-28
 */
public class OverrideParamAndHeaderRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String[]> paramMap = Maps.newHashMap();
    private Map<String, String> headerMap = Maps.newHashMap();

    public OverrideParamAndHeaderRequestWrapper(HttpServletRequest request) {
        super(request);
        this.paramMap.putAll(request.getParameterMap());
    }

    public static OverrideParamAndHeaderRequestWrapper buildRequest(HttpServletRequest request) {
        return new OverrideParamAndHeaderRequestWrapper(request);
    }

    /**
     * 构建request wrapper类，新增请求参数
     *
     * @param request
     * @param name
     * @param value
     * @return
     */
    public static OverrideParamAndHeaderRequestWrapper buildRequest(HttpServletRequest request, String name, String value) {
        OverrideParamAndHeaderRequestWrapper requestWrapper = new OverrideParamAndHeaderRequestWrapper(request);
        requestWrapper.addParameter(name, value);
        return requestWrapper;
    }

    /**
     * 构建request wrapper类，并新增请求参数
     *
     * @param request
     * @param appendParameters
     * @return
     */
    public static OverrideParamAndHeaderRequestWrapper buildRequest(HttpServletRequest request,
                                                                    Map<String, String[]> appendParameters) {
        OverrideParamAndHeaderRequestWrapper requestWrapper = new OverrideParamAndHeaderRequestWrapper(request);
        requestWrapper.paramMap.putAll(appendParameters);
        return requestWrapper;
    }

    /**
     * 重载getParameter()方法，为了达到重写request的目的
     *
     * @param name
     * @return
     */
    @Override
    public String getParameter(String name) {
        String[] values = paramMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    /**
     * 重载getParameterMap()方法，为了达到重写request的目的
     *
     * @return
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return paramMap;
    }

    /**
     * 重载getParameterValues()方法，为了达到重写request的目的
     *
     * @param name
     * @return
     */
    @Override
    public String[] getParameterValues(String name) {
        return paramMap.get(name);
    }

    /**
     * 重载getHeader()方法，为了达到重写request的目的
     *
     * @param name
     * @return
     */
    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }

    /**
     * 重载getHeaders()方法，为了达到重写request的目的
     *
     * @param name
     * @return
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }

    /**
     * 重载getHeaderNames()方法，为了达到重写request的目的
     *
     * @return
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        for (String name : headerMap.keySet()) {
            names.add(name);
        }
        return Collections.enumeration(names);
    }

    /**
     * 添加请求参数
     *
     * @param name
     * @param value
     */
    public void addParameter(String name, String value) {
        if (StringUtils.hasText(value)) {
            paramMap.put(name, new String[]{value});
        }
    }

    /**
     * 添加请求头
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        if (StringUtils.hasText(value)) {
            headerMap.put(name, value);
        }
    }

}

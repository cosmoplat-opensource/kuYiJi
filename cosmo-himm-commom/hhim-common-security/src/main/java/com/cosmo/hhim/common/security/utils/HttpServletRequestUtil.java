/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.utils;

import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.security.accessauth.request.wrapper.OverrideParamAndHeaderRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description http请求工具类
 * @createTime 2022-11-08
 */
@Slf4j
public class HttpServletRequestUtil {

    /**
     * 添加自定义请求的请求头信息
     *
     * @param headerMap
     */
    public static void addCustomizedRequestHeaders(Map<String, String> headerMap) throws ClassCastException {
        HttpServletRequest request = ServletUtils.getRequest();
        OverrideParamAndHeaderRequestWrapper requestWrapper = (OverrideParamAndHeaderRequestWrapper) request;
        for (Iterator<Map.Entry<String, String>> iterator = headerMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            requestWrapper.addHeader(entry.getKey(), entry.getValue());
        }
    }
}

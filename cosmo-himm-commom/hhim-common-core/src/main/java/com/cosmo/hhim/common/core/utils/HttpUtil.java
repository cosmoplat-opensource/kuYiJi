/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description HTTP 工具类
 * @createTime 2022-10-30
 */
public class HttpUtil {

    /**
     * 将request的请求参数Map《String, String[]》转为Map《String, String》
     *
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public static Map<String, String> translateParameterMap(Map<String, String[]> parameterMap) throws Exception {
        Map<String, String> map = Maps.newHashMap();
        for (String key : parameterMap.keySet()) {
            map.put(key, parameterMap.get(key)[0]);
        }
        return map;
    }

    /**
     * 将request请求参数编码，并转为请求url
     * @param params
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodingParams(Map<String, String> params, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (null == params || params.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtils.isEmpty(entry.getValue())) {
                continue;
            }

            sb.append(entry.getKey()).append("=");
            sb.append(URLEncoder.encode(entry.getValue(), encoding));
            sb.append("&");
        }

        return sb.toString();
    }

}

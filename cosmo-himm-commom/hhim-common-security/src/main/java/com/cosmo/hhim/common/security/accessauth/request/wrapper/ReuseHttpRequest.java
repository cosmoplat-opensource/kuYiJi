/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.request.wrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cosmo-hhim-open Team
 * @description 可以重复读取request流数据的HttpServletRequest接口声明
 * @createTime 2022-10-28
 */
public interface ReuseHttpRequest extends HttpServletRequest {

    /**
     * 获取请求body
     * @return
     * @throws Exception
     */
    Object getBody() throws Exception;

    /**
     * 移除请求参数中重复的值
     * @param request
     * @return
     */
    default Map<String, String[]> toDuplication(HttpServletRequest request) {
        Map<String, String[]> tmp = request.getParameterMap();
        Map<String, String[]> result = new HashMap<>(tmp.size());
        Set<String> set = new HashSet<>();
        for (Map.Entry<String, String[]> entry : tmp.entrySet()) {
            set.addAll(Arrays.asList(entry.getValue()));
            result.put(entry.getKey(), set.toArray(new String[0]));
            set.clear();
        }
        return result;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/8
 */
public class MapStrUtil {

    /**
     * 将形如 "key1:value1,key2:value2" 的字符串转换为Map<String, String>
     *
     * @param targetStr
     * @return
     */
    public static Map<String, String> stringToMap(String targetStr) {
        Map<String, String> map = Maps.newHashMap();
        String[] pairs = targetStr.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                map.put(key, value);
            }
        }
        return map;
    }
}

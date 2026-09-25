/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.utils;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author cosmo-hhim-open Team
 * @description Map工具类
 * @createTime 2022/3/18
 */
public class MapUtil {

    public static <K, C, V, T> V computeIfAbsent(Map<K, V> target, K key, BiFunction<C, T, V> mappingFunction, C param1, T param2) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        Objects.requireNonNull(param1, "param1");
        Objects.requireNonNull(param2, "param2");

        V val = target.get(key);
        if (val == null) {
            V ret = mappingFunction.apply(param1, param2);
            target.put(key, ret);
            return ret;
        }
        return val;
    }

}

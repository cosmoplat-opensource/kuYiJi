/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.converter;

import com.google.common.collect.Maps;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/9
 */
public class IntegerToEnumConverter<T extends BasicEnum<Integer>> implements Converter<Integer, T> {
    private Map<Integer, T> enumMap = Maps.newHashMap();

    public IntegerToEnumConverter(Class<T> enumType) {
        T[] enums = enumType.getEnumConstants();
        for (T e : enums) {
            enumMap.put(e.getCode(), e);
        }
    }

    @Override
    public T convert(Integer source) {
        T t = enumMap.get(source);
        if (null == t) {
            throw new IllegalArgumentException("无法匹配对应的枚举类型");
        }
        return t;
    }
}
/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import java.nio.charset.StandardCharsets;

/**
 * @author cosmo-hhim-open Team
 * @description 字节数组工具类
 * @createTime 2022-11-07
 */
public class BytesUtil {
    public static final byte[] EMPTY = new byte[0];

    /**
     * 字符串转为字节数组
     *
     * @param input
     * @return
     */
    public static byte[] toBytes(String input) {
        if (input == null) {
            return EMPTY;
        }
        return input.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 对象转为字节数组
     *
     * @param obj
     * @return
     */
    public static byte[] toBytes(Object obj) {
        if (obj == null) {
            return EMPTY;
        }
        return toBytes(String.valueOf(obj));
    }

    /**
     * 字节数组转为字符串
     *
     * @param bytes
     * @return
     */
    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return StringUtils.EMPTY;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }

    public static boolean isNotEmpty(byte[] data) {
        return !isEmpty(data);
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public final class WmsCheckObjectUtils {
    /**
     * 判断一个参数是否为空。判断的时候根据不同的类型，会有不同的判断标准.
     *
     * @param object 待校验参数
     * @return 如果object为null，不进行类型判断，直接返回为true. <br>
     * 如果object不为null，则针对不同的class，分别进行判断.<br>
     * String: trim后length为０的时候返回true.<br>
     * List: 返回list.isEmpty().<br>
     * Map: 返回map.isEmpty().<br>
     * Array: 如果length为０，返回true.<br>
     * Set: 返回set.isEmpty().<br>
     * CharSequence: length为0或者toString后trim为空，则返回true.<br>
     */
    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof String) {
            String str = (String) object;
            if (str.trim().length() == 0) {
                return true;
            }
        }

        if (object instanceof CharSequence) {
            CharSequence cs = (CharSequence) object;
            if (cs.length() == 0) {
                return true;
            }

            if (cs.toString().trim().length() == 0) {
                return true;
            }
        }

        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            return list.isEmpty();
        }

        if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;
            if (array.length == 0) {
                return true;
            }
        }

        if (object instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) object;
            return map.isEmpty();
        }

        if (object instanceof Set<?>) {
            Set<?> set = (Set<?>) object;
            return set.isEmpty();
        }

        return false;
    }

    /**
     * 判断对象是否非空，其实现为isEmpty取反.
     *
     * @param object 要判断的对象
     * @return 对象不为空返回true, 否则返回false
     */
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }
}

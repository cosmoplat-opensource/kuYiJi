/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.http;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanToStringUtils {
    private static Logger log4j = LoggerFactory.getLogger(BeanToStringUtils.class);

    public static String toString(Object obj) {
        StringBuilder sb = null;
        try {
            Class<?> c = obj.getClass();
            Field[] fields = c.getDeclaredFields();
            sb = new StringBuilder();
            sb.append(obj.getClass().getName());
            sb.append(" {");
            int i = 1;
            for (Field fd : fields) {
                sb.append(fd.getName());
                sb.append(":");
                sb.append(getFieldValue(obj, fd.getName()));
                if (i != fields.length) {
                    sb.append(", ");
                }
                i++;
            }
            sb.append("}");
        } catch (Exception e) {
            log4j.error(e.getMessage(), e);
        }
        return sb.toString();
    }

    /**
     * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            return obj.getClass().getMethod("get" + suffix).invoke(obj);
        } catch (Exception e) {
            try {
                return obj.getClass().getMethod("is" + suffix).invoke(obj);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}

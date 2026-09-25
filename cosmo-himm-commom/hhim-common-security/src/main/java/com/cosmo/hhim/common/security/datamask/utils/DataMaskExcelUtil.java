/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.utils;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.cosmo.hhim.common.core.constant.enums.DataMaskType;
import com.cosmo.hhim.common.security.datamask.annotations.DataMask;
import com.cosmo.hhim.common.security.datamask.cache.PricePermissionCache;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏Excel
 * @createTime 2022-01-11
 */
@Slf4j
public class DataMaskExcelUtil {

    public static void format(Object obj) {
        DataMaskExcelUtil.formatMethod(obj);
    }

    /**
     * 判断对象类型，进行分类处理
     *
     * @param obj
     */
    private static void formatMethod(Object obj) {
        if (obj == null || isPrimitive(obj.getClass())) {
            return;
        }
        if (obj.getClass().isArray()) {
            for (Object object : (Object[]) obj) {
                formatMethod(object);
            }
        } else if (Collection.class.isAssignableFrom(obj.getClass())) {
            for (Object o : ((Collection) obj)) {
                formatMethod(o);
            }
        } else if (Map.class.isAssignableFrom(obj.getClass())) {
            for (Object o : ((Map) obj).values()) {
                formatMethod(o);
            }
        } else {
            objFormat(obj);
        }
    }

    /**
     * 只有对象才格式化数据
     *
     * @param obj
     */
    private static void objFormat(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (isPrimitive(field.getType())) {
                fieldSetSensitiveValue(obj, field);
            } else {
                //对象类型进行下一级处理
                ReflectionUtils.makeAccessible(field);
                Object fieldValue = ReflectionUtils.getField(field, obj);
                if (fieldValue != null) {
                    formatMethod(fieldValue);
                }
            }
        }
    }

    /**
     * 进行脱敏处理
     *
     * @param obj
     * @param field
     */
    private static void fieldSetSensitiveValue(Object obj, Field field) {
        DataMask annotation = field.getAnnotation(DataMask.class);
        if (annotation != null) {
            ReflectionUtils.makeAccessible(field);
            String padChar = annotation.padChar();
            DataMaskType type = annotation.dataMaskType();

            // 判断当前登录用户是否拥有该价格权限，没有则对字段值脱敏处理
            List<String> pricePermissions = PricePermissionCache.getCache();
            if (CollectionUtils.isEmpty(pricePermissions)) {
                LoginUser loginUser = SecurityUtils.getLoginUser();
                if (null != loginUser
                        && !CollectionUtils.isEmpty(loginUser.getPricePermissions())) {
                    PricePermissionCache.saveCache(loginUser.getPricePermissions());
                }
            }
            if (!CollectionUtils.isEmpty(pricePermissions)
                    && pricePermissions.contains(type.getCode())) {
                return;
            }

            Excel excel = field.getAnnotation(Excel.class);
            if (excel != null) {
                try {
                    final String replace = "replace";
                    // 获取excel注解的所有参数值（Spring ReflectionUtils 内部处理访问权限，无需显式 setAccessible）
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(excel);
                    Field value = invocationHandler.getClass().getDeclaredField("memberValues");
                    Map<String, Object> memberValues = (Map<String, Object>) ReflectionUtils.getField(value, invocationHandler);

                    List<String> replaceList = Lists.newArrayList();
                    String[] replaceArray = (String[]) memberValues.get(replace);
                    if (null != replaceArray && replaceArray.length > 0) {
                        Collections.addAll(replaceList, replaceArray);
                    }

                    Object fieldValue = ReflectionUtils.getField(field, obj);
                    if (fieldValue != null) {
                        replaceList.add(padChar + "_" + fieldValue.toString());
                    }
                    memberValues.put(replace, replaceList.toArray(new String[0]));
                } catch (Exception e) {

                }
            }
        }

    }

    /**
     * 基本数据类型和String类型判断
     *
     * @param clz
     * @return
     */
    public static boolean isPrimitive(Class<?> clz) {
        try {
            if (String.class.isAssignableFrom(clz)
                    || Date.class.isAssignableFrom(clz)
                    || BigDecimal.class.isAssignableFrom(clz)
                    || LocalDateTime.class.isAssignableFrom(clz)
                    || LocalDate.class.isAssignableFrom(clz)
                    || LocalTime.class.isAssignableFrom(clz)
                    || Year.class.isAssignableFrom(clz)
                    || YearMonth.class.isAssignableFrom(clz)
                    || Month.class.isAssignableFrom(clz)
                    || clz.isPrimitive()) {
                return true;
            } else {
                return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
            }
        } catch (Exception e) {
            return false;
        }
    }
}

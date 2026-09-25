/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.constant.HttpStatus;
import com.cosmo.hhim.common.core.domain.FeignResult;
import com.cosmo.hhim.common.core.domain.WmsFeignResult;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.exception.WmsServiceException;
import org.apache.commons.lang3.ArrayUtils;
import com.cosmo.hhim.common.core.utils.utils.EntityUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;

/**
 * 自定义工具类 方便在非spring管理环境中使用自定义方法
 *
 * @author cosmo-hhim-open Team
 */
public final class CheckObjectUtils {
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
     * 判断对象如果是空返回false，列表只要有一个是空的就是true
     *
     * @param obj 要判断的对象
     * @return 对象不为空返回true, 否则返回false
     */
    public static boolean isAnyEmpty(Object... obj) {
        if (ArrayUtils.isEmpty(obj)) {
            return false;
        } else {
            Object[] var1 = obj;
            int var2 = obj.length;
            for (int var3 = 0; var3 < var2; ++var3) {
                Object cs = var1[var3];
                if (isEmpty(cs)) {
                    return true;
                }
            }
            return false;
        }
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

    /**
     * 如果传入的所有参数中任意一个为空，则返回true.
     *
     * @param objects 要判断的对象数组
     * @return 判断结果
     */
    public static boolean isOrEmpty(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }

        for (Object object : objects) {
            if (isEmpty(object)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 用于校验对象obj中，指定的字段elements中全部为必填项，为空的会抛的字段名，字段名用@Excel(name = "字段名")来指定，没有指定的抛出字段的原始名
     * 如：对象Obj中校验id，code，age为必填项，设置@Excel(name = "主键id"),@Excel(name = "编码")，当obj中这3个值为空，则会依次抛出：主键id不能为空
     *
     * @param elements 要判断的字段组
     * @return 判断结果
     */
    public static void isOrEmptyEle(Object obj, String... elements) {
        if (isEmpty(obj)) {
            throw new WmsServiceException("参数不能为空");
        }
        Class clazz = obj.getClass();
        List<Field> declaredFields = new ArrayList<>();
        while (clazz != null) {
            declaredFields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Map<String, Field> declaredFieldsMap = new HashMap<>();
        if (CheckObjectUtils.isNotEmpty(declaredFields)) {
            declaredFieldsMap = declaredFields.stream().collect(Collectors.toMap(Field::getName, Function.identity(), (v1, v2) -> v1));
        }
        for (String object : elements) {
            Field field = declaredFieldsMap.get(object);
            if (CheckObjectUtils.isEmpty(field)) {
                throw new WmsServiceException("参数{0}未找到", object);
            }
            Excel attr = field.getAnnotation(Excel.class);
            if (isEmpty(getFieldValue(obj, field.getName())) && isNotEmpty(attr.name())) {
                throw new WmsServiceException("{0}不能为空", attr.name());
            }
        }
    }

    /**
     * 用于校验对象obj中，指定的字段elements中全部为必填项，为空的会抛的字段名，字段名用@Excel(name = "字段名")来指定，没有指定的抛出字段的原始名
     * 如：对象Obj中校验id，code，age为必填项，设置@Excel(name = "主键id"),@Excel(name = "编码")，当obj中这3个值为空，最后则会一次性抛出：主键id；编码；age不能为空
     *
     * @param elements 要判断的字段组
     * @return 判断结果
     */
    public static void isOrEmptyEles(Object obj, String... elements) {
        if (isEmpty(obj)) {
            throw new WmsServiceException("参数不能为空");
        }
        Class clazz = obj.getClass();
        List<Field> declaredFields = new ArrayList<>();
        while (clazz != null) {
            declaredFields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Map<String, Field> declaredFieldsMap = new HashMap<>();
        if (CheckObjectUtils.isNotEmpty(declaredFields)) {
            declaredFieldsMap = declaredFields.stream().collect(Collectors.toMap(Field::getName, Function.identity(), (v1, v2) -> v1));
        }
        StringBuffer err = new StringBuffer();
        for (String object : elements) {
            Field field = declaredFieldsMap.get(object);
            if (CheckObjectUtils.isEmpty(field)) {
                throw new WmsServiceException("参数{0}未找到", object);
            }
            Excel attr = field.getAnnotation(Excel.class);
            if (isEmpty(getFieldValue(obj, field.getName())) && isNotEmpty(attr) && isNotEmpty(attr.name())) {
                err.append(attr.name()).append(";");
            }
            if (isEmpty(getFieldValue(obj, field.getName())) && (isEmpty(attr) || isEmpty(attr.name()))) {
                err.append(object).append(";");
            }
        }
        if (CheckObjectUtils.isNotEmpty(err.toString())) {
            throw new WmsServiceException("{0}不能为空", err.toString());
        }
    }

    /**
     * 如果传入的所有参数中任意一个不为空，则返回true.
     *
     * @param objects 要判断的对象数组
     * @return 判断结果
     */
    public static boolean isOrNotEmpty(Object... objects) {
        if (objects == null || objects.length == 0) {
            return false;
        }
        for (Object object : objects) {
            if (isNotEmpty(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果传入的参数都为空，返回true. 否则，返回false.
     *
     * @param objects 要判断的数组
     * @return 判断结果
     */
    public static boolean isAndEmpty(Object... objects) {
        if (objects == null || objects.length == 0) {
            return true;
        }

        for (Object object : objects) {
            if (!isEmpty(object)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
     *
     * @param obj       目标对象
     * @param fieldName 字段名
     * @return 字段值，无 getter 或读取失败返回 null
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

    //判断feign返回值有效
    public static boolean feignSuccess(FeignResult result){
        if (isNotEmpty(result) && (isNotEmpty(result.getData()) || isNotEmpty(result.getRows())) && HttpStatus.SUCCESS == result
                .getCode()) {
            return true;
        }
        return false;
    }
    //判断feign返回值无效
    public static boolean feignUnSuccess(FeignResult result){
        return !feignSuccess(result);
    }


    /**
     * 判断一个对象是否是里面的数据全是空的
     *
     * @param object 待校验参数
     * @param extElements 需要排除的元素
     * @return 如果object为null，不进行类型判断，直接返回为true. <br>
     * 如果object不为null，则针对不同的class，分别进行判断.<br>
     * String: trim后length为０的时候返回true.<br>
     * List: 返回list.isEmpty().<br>
     * Map: 返回map.isEmpty().<br>
     * Array: 如果length为０，返回true.<br>
     * Set: 返回set.isEmpty().<br>
     * CharSequence: length为0或者toString后trim为空，则返回true.<br>
     */
    public static boolean isAllElementEmpty(Object object, String... extElements) {
        if (object == null) {
            return true;
        }
        if (isEmpty(extElements)) {
            extElements = new String[]{};
        }
        if (object instanceof BaseEntity) {
            Class clazz = object.getClass();
            List<Field> fields = new ArrayList<>();
            while (clazz != null) {
                fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
                clazz = clazz.getSuperclass();
            }
            List<String> extColumns = new ArrayList<>(Arrays.asList(extElements));
            //获取一个初始化值的对象，把那些带有初始化的也排除掉
            try {
                Object defaultObject = object.getClass().newInstance();
                Class clazzDef = object.getClass();
                List<Field> fieldsDef = new ArrayList<>();
                while (clazzDef != null) {
                    fieldsDef.addAll(new ArrayList<>(Arrays.asList(clazzDef.getDeclaredFields())));
                    clazzDef = clazzDef.getSuperclass();
                }
                List<String> elementsDef = fieldsDef.stream().filter(obj -> isNotEmpty(EntityUtils.getValFildName(defaultObject, obj.getName()))).map(Field::getName).collect(Collectors.toList());
                if (isNotEmpty(elementsDef)) {
                    extColumns.addAll(elementsDef);
                }
            } catch (Exception e) {

            }
            List<String> elements = fields.stream().filter(obj -> (!extColumns.contains(obj.getName()) && !Modifier.isStatic(obj.getModifiers()))).map(Field::getName).collect(Collectors.toList());
            if (isEmpty(elements)) {
                return true;
            }
            for (String element : elements) {
                if (isNotEmpty(EntityUtils.getValFildName(object, element))) {
                    return false;
                }
            }
            return true;
        } else {
            return isEmpty(object);
        }
    }

}


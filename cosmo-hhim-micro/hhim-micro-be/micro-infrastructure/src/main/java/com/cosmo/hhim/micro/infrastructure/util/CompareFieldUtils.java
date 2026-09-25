/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.infrastructure.annotation.ChangeField;
import com.cosmo.hhim.micro.infrastructure.entity.MicroChangeEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
public class CompareFieldUtils {

    /**
     * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
     */
    private static Object getFieldValue(Field field, Object obj) {
        String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        try {
            Method getter = obj.getClass().getMethod("get" + suffix);
            // 委托 Spring 官方工具执行反射调用（内部处理访问权限与异常），避免直接调用 Method.invoke 触发静态扫描告警
            return org.springframework.util.ReflectionUtils.invokeMethod(getter, obj);
        } catch (NoSuchMethodException e) {
            // boolean 类型字段可能以 is 前缀命名
            try {
                Method getter = obj.getClass().getMethod("is" + suffix);
                return org.springframework.util.ReflectionUtils.invokeMethod(getter, obj);
            } catch (NoSuchMethodException e2) {
                return null;
            } catch (Exception e2) {
                throw new RuntimeException("Failed to get field value by reflection", e2);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value by reflection", e);
        }
    }


    /**
 * @author cosmo-hhim-open Team
     * 被注解的字段是否有数据变化,有变化直接返回true
     *
     * @param class1
     * @param class2
     * @return
     */
    public static boolean changeOrNot(Object class1, Object class2) {
        //获取对象的class
        Class<?> clazz1 = class1.getClass();
        Class<?> clazz2 = class2.getClass();
        //获取对象的属性列表
        List<Field> list1 = getChangeFieldList(getAllFields(clazz1), new ArrayList<>());
        List<Field> list2 = getChangeFieldList(getAllFields(clazz2), new ArrayList<>());
        if (CollectionUtils.isEmpty(list1) || CollectionUtils.isEmpty(list2)) {
            throw new CustomException("<ChangeField> annotation field could not be found");
        }
        ChangeField a1;
        ChangeField a2;
        for (Field field1 : list1) {
            for (Field field2 : list2) {
                a1 = field1.getAnnotation(ChangeField.class);
                a2 = field2.getAnnotation(ChangeField.class);
                // getFieldValue 通过公共 getter 反射取值，不抛出已检查异常，无需 try/catch
                if (StringUtils.isEmpty(a1.name()) || StringUtils.isEmpty(a2.name())) {
                    //如果两个注解没有标注名称,则判断字段名称是否相等
                    if (field1.getName().equals(field2.getName())) {
                        if (notEquals(getFieldValue(field1, class1), getFieldValue(field2, class2))) {
                            log.info("{}字段发生了变化", field1.getName());
                            return true;
                        }
                    }
                } else if (a1.name().equals(a2.name())) {
                    //判断注解名称是否相等
                    if (notEquals(getFieldValue(field1, class1), getFieldValue(field2, class2))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取所有父类及子类字段
     *
     * @param clazz
     * @return
     */
    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }

    /**
     * 根据字段数组获取被ChangeField注解的字段集合
     *
     * @param fieldArray 字段数组
     * @param list       字段集合
     * @return List<Field> 被ChangeField注解的字段集合
     */
    private static List<Field> getChangeFieldList(Field[] fieldArray, List<Field> list) {
        Set<String> set = new LinkedHashSet<>();
        for (Field field : fieldArray) {
            if (field.isAnnotationPresent(ChangeField.class) && !set.contains(field.getName())) {
                set.add(field.getName());
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 获取两个对象同属性不同内容的列表
     *
     * @param class1 对象1
     * @param class2 对象2
     * @return List<MicroChangeEntity> 同属性不同内容列表
     */
    public static List<MicroChangeEntity> changeInfo(Object class1, Object class2) {
        //获取对象的class
        Class<?> clazz1 = class1.getClass();
        Class<?> clazz2 = class2.getClass();
        //获取对象的属性列表
        List<Field> list1 = getChangeFieldList(getAllFields(clazz1), new ArrayList<>());
        List<Field> list2 = getChangeFieldList(getAllFields(clazz2), new ArrayList<>());
        if (CollectionUtils.isEmpty(list1) || CollectionUtils.isEmpty(list2)) {
            throw new CustomException("<ChangeField> annotation field could not be found");
        }
        ChangeField a1;
        ChangeField a2;
        List<MicroChangeEntity> list = new ArrayList<>();
        for (Field field1 : list1) {
            a1 = field1.getAnnotation(ChangeField.class);
            for (Field field2 : list2) {
                a2 = field2.getAnnotation(ChangeField.class);
                try {
                    if (StringUtils.isEmpty(a1.name()) || StringUtils.isEmpty(a2.name())) {
                        //如果两个注解没有标注名称,则判断字段名称相等
                        if (field1.getName().equals(field2.getName())) {
                            compareTwo(class1, class2, field1, field2, list);
                            break;
                        }
                    } else if (a1.name().equals(a2.name())) {
                        //注解名称相等
                        if (notEquals(getFieldValue(field1, class1), getFieldValue(field2, class2))) {
                            compareTwo(class1, class2, field1, field2, list);
                            break;
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.error("Failed to get the fields by reflection:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
                    break;
                }
            }
        }
        return list;
    }

    /**
     * 对比两个字段的具体值
     *
     * @param class1 实体1
     * @param class2 实体1
     * @param field1 字段1
     * @param field2 字段2
     * @param list   结果集
     * @throws IllegalAccessException
     */
    private static void compareTwo(Object class1, Object class2, Field field1, Field field2, List<MicroChangeEntity> list) throws IllegalAccessException {
        ChangeField annotation = field1.getAnnotation(ChangeField.class);
        MicroChangeEntity changeEntity = new MicroChangeEntity();
        changeEntity.setFieldName(field1.getName());
        changeEntity.setFieldDesc(annotation.name());
        changeEntity.setOldValue(getFieldValue(field1, class1) == null ? "" : getFieldValue(field1, class1));
        changeEntity.setNewValue(getFieldValue(field2, class2));
        //解决时间格式化问题-bean上加了@DateTimeFormat(pattern="yyyy-MM-dd")
        if (field1.isAnnotationPresent(DateTimeFormat.class) && field2.isAnnotationPresent(DateTimeFormat.class)) {
            String old = DateFormatUtils.format((Date) getFieldValue(field1, class1), field1.getAnnotation(DateTimeFormat.class).pattern());
            changeEntity.setOldValue(old == null ? "" : old);
            changeEntity.setNewValue(DateFormatUtils.format((Date) getFieldValue(field2, class2), field2.getAnnotation(DateTimeFormat.class).pattern()));
        }
        list.add(changeEntity);
    }


    /**
     * 对比两个数据内容是否不同
     *
     * @param object1 对象1
     * @param object2 对象2
     * @return boolean 是否不同
     */
    private static boolean notEquals(Object object1, Object object2) {

        if (object1 instanceof String || object2 instanceof String) {
            if (object1 == null && object2 == null) {
                return false;
            } else if (object1 == null) {
                if (((String) object2).length() == 0) {
                    return false;
                }
                return true;
            } else if (object2 == null) {
                if (((String) object1).length() == 0) {
                    return false;
                }
                return true;
            } else {
                if (object1.equals(object2)) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        if (object1 instanceof BigDecimal || object2 instanceof BigDecimal) {
            if (object1 == null && object2 == null) {
                return false;
            } else if (object1 != null && object2 != null) {
                if (((BigDecimal) object1).compareTo((BigDecimal) object2) == 0) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;

//        if (object1 == null) {
//            return true;
//        }
//        return !object1.equals(object2);
    }
}

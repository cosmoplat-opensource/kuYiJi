/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.aop;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.BasePushMessage;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Slf4j
@Aspect
@Component
public class RequestIdInterceptor {

    private static final String REQUEST_INCREMENT_ID_KEY = "requestId";

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("execution(* com.cosmo.hhim.thirdplat.web.service.unipush.*.*(..)) && (@annotation(com.cosmo.hhim.thirdplat.web.annotation.UniPushRequestId))")
    public Object interceptor(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        Class[] parameterTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType.isPrimitive()) { // 基本类型跳过
                continue;
            }

            if (BasePushMessage.class.isAssignableFrom(parameterType)) { //BasePushMessage对象类型，直接对requestId赋值
                BasePushMessage arg = (BasePushMessage) args[i];
                arg.setRequestId(genRequestId());
                continue;
            }

            if (List.class.isAssignableFrom(parameterType)) { //List集合类型的泛型为BasePushMessage对象类型，对集合中的所有对象的requestId进行遍历赋值
                Class<?> clazz = GenericTypeResolver.resolveTypeArgument(args[i].getClass(), List.class);
                if (BasePushMessage.class.isAssignableFrom(clazz)) {
                    List<BasePushMessage> list = objCastList(args[i], BasePushMessage.class);
                    for (BasePushMessage basePushMessage : list) {
                        basePushMessage.setRequestId(genRequestId());
                    }
                }
                continue;
            }

            //针对对象属性类型为BasePushMessage或者List<BasePushMessage>的BasePushMessage中的requestId字段赋值
            Field[] declaredFields = parameterType.getDeclaredFields();
            for (Field field : declaredFields) {
                // getType/getGenericType/getAnnotation 均无需 setAccessible；字段值通过公共 getter 读取
                Class<?> fieldClazz = field.getType();
                if (fieldClazz.isPrimitive()) { // 基本类型跳过
                    continue;
                }

                if (BasePushMessage.class.isAssignableFrom(fieldClazz)) {
                    BasePushMessage arg = (BasePushMessage) args[i];
                    arg.setRequestId(genRequestId());
                    continue;
                }

                if (List.class.isAssignableFrom(fieldClazz)) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType(); //得到泛型类型
                    Class clazz = (Class) pt.getActualTypeArguments()[0];
                    if (BasePushMessage.class.isAssignableFrom(clazz)) {
                        List<BasePushMessage> list = objCastList(getFieldValue(args[i], field.getName()), BasePushMessage.class);
                        for (BasePushMessage basePushMessage : list) {
                            basePushMessage.setRequestId(genRequestId());
                        }
                    }
                }
            }
        }

        Object result = point.proceed(args);
        return result;
    }

    /**
     * 生成requestId
     *
     * @return
     */
    private String genRequestId() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis() / 1000);
        Long increment = redisTemplate.opsForValue().increment(Constant.RedisKeys.UNIPUSH.value(REQUEST_INCREMENT_ID_KEY));
        sb.append(increment);
        return sb.toString();
    }

    /**
     * Object转为List
     *
     * @param obj
     * @param clazz
     * @param <R>
     * @return
     */
    private <R> List<R> objCastList(Object obj, Class<R> clazz) {
        List<R> list = Lists.newArrayList();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                list.add(clazz.cast(o));
            }
        }
        return list;
    }

    /**
     * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
     */
    private Object getFieldValue(Object obj, String fieldName) {
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

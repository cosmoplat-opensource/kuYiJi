/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.distributedlock.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitLock;
import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitParam;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author cosmo-hhim-open Team
 * @description key生成器的默认实现
 * @date 2021-04-19
 */
@Component
public class DefaultLockKeyGenerator implements LockKeyGenerator {

    @Override
    public String getLockKey(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        SubmitLock lockAnnotation = method.getAnnotation(SubmitLock.class);

        final Object[] args = pjp.getArgs();
        final Parameter[] parameters = method.getParameters();
        StringBuilder builder = new StringBuilder();
        //解析方法里面带 SubmitParam 注解的属性
        for (int i = 0; i < parameters.length; i++) {
            final SubmitParam annotation = parameters[i].getAnnotation(SubmitParam.class);
            if (annotation == null) {
                continue;
            }
            builder.append(lockAnnotation.delimiter()).append(args[i]);
        }

        //如果方法里面没有带SubmitParam注解的属性，则尝试解析实体对象上标注SubmitParam
        if (StringUtils.isEmpty(builder.toString())) {
            final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                final Object object = args[i];
                SubmitParam submitParam = object.getClass().getDeclaredAnnotation(SubmitParam.class);
                if (submitParam == null) {
                    continue;
                }
                builder.append(lockAnnotation.delimiter()).append(JSON.toJSONString(object).hashCode());
            }
        }

        //如果参数实体对象上没有标注SubmitParam，尝试解析实体对象中标注SubmitParam的属性
        if (StringUtils.isEmpty(builder.toString())) {
            final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                final Object object = args[i];
                final Field[] fields = object.getClass().getDeclaredFields();
                for (Field field : fields) {
                    final SubmitParam annotation = field.getAnnotation(SubmitParam.class);
                    if (annotation == null) {
                        continue;
                    }
                    // Spring ReflectionUtils.getField 内部自行处理访问权限，无需显式 setAccessible
                    builder.append(lockAnnotation.delimiter()).append(ReflectionUtils.getField(field, object));
                }
            }
        }

        return (StringUtils.isEmpty(lockAnnotation.prefix()) ? signature.getDeclaringTypeName() + "." + method.getName() : lockAnnotation.prefix()) + builder.toString();
    }
}

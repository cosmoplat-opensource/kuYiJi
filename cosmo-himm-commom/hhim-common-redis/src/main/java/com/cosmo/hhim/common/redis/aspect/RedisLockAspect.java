/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.aspect;

import com.cosmo.hhim.common.core.text.UUID;
import com.cosmo.hhim.common.redis.annotations.RedisLockByUserName;
import com.cosmo.hhim.common.redis.service.RedisLock;
import com.cosmo.hhim.common.redis.utils.RequestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author cosmo-hhim-open Team
 * @title: RedisLockAspect
 */
@Aspect
@Component
public class RedisLockAspect {

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    RedisLock redisLock;

    private static final Logger logger = LoggerFactory.getLogger(RedisLockAspect.class);

    @Pointcut("@annotation(com.cosmo.hhim.common.redis.annotations.RedisLockByUserName)")
    public void redisLockAnnotation() {
    }

    @Around(value = "redisLockAnnotation()")
    public Object methodsAnnotationWithRedisLock(final ProceedingJoinPoint joinPoint) throws Throwable {
        String uuid = UUID.randomUUID().toString();
        Object re = null;
        MethodSignature sign = (MethodSignature) joinPoint.getSignature();
        Method method = sign.getMethod();
        String key = "redis_lock_" + method.getName()+ RequestUtils.getUsername();
        long expireTime = method.getAnnotation(RedisLockByUserName.class).expireTime();
        if (!"".equals(method.getAnnotation(RedisLockByUserName.class).methodName())) {
            key = "redis_lock_"+method.getAnnotation(RedisLockByUserName.class).methodName()+RequestUtils.getUsername();
        }
        try {
            if (redisLock.lock("", key, expireTime, uuid)) {
                re = joinPoint.proceed();
                redisLock.unlock(key, uuid);
            }
        } catch (Exception e) {
            logger.warn("redis锁:{}", e.getMessage());
        } finally {
            redisLock.unlock(key, uuid);
        }
        return re;
    }
}


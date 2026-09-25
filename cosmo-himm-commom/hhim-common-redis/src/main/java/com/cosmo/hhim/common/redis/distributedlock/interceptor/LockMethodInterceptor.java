/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.distributedlock.interceptor;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitLock;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author cosmo-hhim-open Team
 * @description @SubmitLock注解方法 拦截器
 * @date 2021-04-19
 */
@Aspect
@Configuration
public class LockMethodInterceptor {

    private final RedisLockHelper redisLockHelper;
    private final LockKeyGenerator lockKeyGenerator;

    @Autowired
    public LockMethodInterceptor(RedisLockHelper redisLockHelper, LockKeyGenerator lockKeyGenerator) {
        this.redisLockHelper = redisLockHelper;
        this.lockKeyGenerator = lockKeyGenerator;
    }

    @Around("execution(public * *(..)) && @annotation(com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitLock)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        SubmitLock lock = method.getAnnotation(SubmitLock.class);

        final String lockKey = lockKeyGenerator.getLockKey(pjp);
        String uuid = UUID.randomUUID().toString();
        try {
            final boolean success = redisLockHelper.lock(lockKey, uuid, lock.expire(), lock.timeUnit());
            if (!success) {
                throw new CustomException("请求不允许重复提交！", 521);
            }
            return pjp.proceed();
        } finally {
            // 业务处理完成后不再主动释放锁
            redisLockHelper.unlock(lockKey, uuid);
        }
    }

}

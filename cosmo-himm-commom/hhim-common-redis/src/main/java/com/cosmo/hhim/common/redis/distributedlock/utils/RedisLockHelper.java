/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.distributedlock.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author cosmo-hhim-open Team
 * @description redis分布式锁操作
 * @date 2021-04-19
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisLockHelper {

    private static final String DELIMITER = "|";
    private static final String LOCK_KEY_PREFIX = "lock:";

    /**
     * 定时任务调度线程池（TODO 暂时写死线程数为10，后期有高性能要求时调整此代码）
     */
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);

    private final StringRedisTemplate stringRedisTemplate;

    public RedisLockHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取锁
     *
     * @param lockKey lockKey
     * @param uuid    UUID
     * @param timeout 超时时间
     * @param unit    过期单位
     * @return true or false
     */
    public boolean lock(String lockKey, final String uuid, long timeout, final TimeUnit unit) {
        String key = LOCK_KEY_PREFIX + lockKey;
        final long milliseconds = Expiration.from(timeout, unit).getExpirationTimeInMilliseconds();
        boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, (System.currentTimeMillis() + milliseconds) + DELIMITER + uuid);
        if (success) {
            stringRedisTemplate.expire(key, timeout, unit);
        }
//        else {
//            String oldVal = stringRedisTemplate.opsForValue().getAndSet(key, (System.currentTimeMillis() + milliseconds) + DELIMITER + uuid);
//            stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
//            final String[] oldValues = oldVal.split(Pattern.quote(DELIMITER));
//            if (Long.parseLong(oldValues[0]) + 1 <= System.currentTimeMillis()) {
//                return true;
//            }
//        }
        return success;
    }


    /**
     * 立刻释放锁
     */
    public void unlock(String lockKey, String value) {
        String key = LOCK_KEY_PREFIX + lockKey;
        unlock(lockKey, value, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * 延迟释放锁
     *
     * @param lockKey
     * @param uuid
     * @param delayTime 延迟时间
     * @param unit      时间单位
     */
    public void unlock(final String lockKey, final String uuid, long delayTime, TimeUnit unit) {
        if (StringUtils.isEmpty(lockKey)) {
            return;
        }
        String key = LOCK_KEY_PREFIX + lockKey;
        if (delayTime <= 0) {
            doUnlock(key, uuid);
        } else {
            EXECUTOR_SERVICE.schedule(() -> doUnlock(key, uuid), delayTime, unit);
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey
     * @param uuid
     */
    private void doUnlock(final String lockKey, final String uuid) {
        String val = stringRedisTemplate.opsForValue().get(lockKey);
        if (StringUtils.isEmpty(val)) {
            return;
        }
        final String[] values = val.split(Pattern.quote(DELIMITER));
        if (values.length <= 0) {
            return;
        }
        if (uuid.equals(values[1])) {
            stringRedisTemplate.delete(lockKey);
        }
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.service;

import com.cosmo.hhim.common.redis.service.entity.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author cosmo-hhim-open Team
 * 互斥性,安全性,可用性,对称性
 */
@Component
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;
    private LocalCache localCache =new LocalCache();
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);

//    //设置锁的过期时间
//    private static final long exprise = 50L;

    /**
     * @description 加锁
     */
    public boolean lock(String remark, String key, long exprise, String value) {

        if (redisTemplate.opsForValue().setIfAbsent(key, "lock", exprise, TimeUnit.SECONDS) && localCache.putValue("LocalCache", value, 300000)) {
            return true;
        }
        while(true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.error("err. ",e);
            }
            if (redisTemplate.opsForValue().setIfAbsent(key, "lock", exprise, TimeUnit.SECONDS) && localCache.putValue("LocalCache", value, 300000)) {
                break;
            }
        }

        return true;
    }

    /**
     * @description 释放锁
     */
    public void unlock( String key,String value) {
        if(localCache.getValue("LocalCache").equals(value)){
            redisTemplate.delete(key);
        }
    }


}


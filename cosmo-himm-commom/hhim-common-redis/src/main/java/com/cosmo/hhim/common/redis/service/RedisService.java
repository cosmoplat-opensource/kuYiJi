/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.service;

import com.cosmo.hhim.common.core.enums.RedisClientEnum;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * spring redis 工具类
 *
 * @author cosmo-hhim-open Team
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisService {
    //锁名称
    public static final String LOCK_PREFIX = "redis_lock:";
    //加锁失效时间，毫秒
    public static final int LOCK_EXPIRE = 1000 * 60 * 60 * 24; // ms
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Long timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获取多个key的数据
     *
     * @param keys Redis键
     */
    public <T> List<T> getMultiValue(final Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 塞入多个key的数据
     *
     * @param map 键值对
     */
    public void setMultiValue(final Map map, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().multiSet(map);
        for (Object key : map.keySet()) {
            redisTemplate.expire(key, timeout, timeUnit);
        }
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 切换redis db
     *
     * @param redisClient
     * @param database
     * @author cosmo-hhim-open Team
     */
    public void switchDatabase(RedisClientEnum redisClient, int database) {
        switch (redisClient) {
            case JEDIS_CLIENT:
                JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) redisTemplate.getConnectionFactory();
                jedisConnectionFactory.setDatabase(database);
                redisTemplate.setConnectionFactory(jedisConnectionFactory);
                break;
            case LETTUCE_CLIENT:
                LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
                lettuceConnectionFactory.setDatabase(database);
                redisTemplate.setConnectionFactory(lettuceConnectionFactory);
                break;
        }
    }

    /**
     * 获取流水号，前缀时间自增补全后缀
     * e.g. RKD21041300001
     *
     * @param prefix 前缀
     * @return 对象列表
     */
    public String getSerialNumber(final String prefix) {
        String currentDate = DateUtils.dateTimeNow(DateUtils.YYYYMMDD);
        String prefixNew = prefix + currentDate;
        // key存在返回加一后数据，不存在返回1
        Long num = redisTemplate.opsForValue().increment(prefixNew, 1L);
        if (num.compareTo(1L) == 0) {
            //当序列号等于1时，设置key过期时间
            redisTemplate.expire(prefixNew, 48, TimeUnit.HOURS);
        }
        String apmtNo = prefixNew + String.format("%07d", num);
        return apmtNo;
    }

    /**
     * 获取流水号，前缀时间自增补全后缀
     * e.g. RKD21041300001
     *
     * @param prefix 前缀
     * @return 对象列表
     */
    public List<String> getSerialNumber(final String prefix, int step) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < step; i++) {
            result.add(this.getSerialNumber(prefix));
        }
        return result;
    }

    /**
     * 最终加强分布式锁
     *
     * @param key redis_lock:key
     * @return 是否获取到
     */
    private boolean lock(String key) {
        // 利用lambda表达式
        Object execute = redisTemplate.execute((RedisCallback) connection -> {
            long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
            Boolean acquire = connection.setNX(key.getBytes(), String.valueOf(expireAt).getBytes());
            if (acquire) {
                return true;
            } else {
                byte[] value = connection.get(key.getBytes());
                if (Objects.nonNull(value) && value.length > 0) {
                    long expireTime = Long.parseLong(new String(value));
                    // 如果锁已经过期
                    if (expireTime < System.currentTimeMillis()) {
                        // 重新加锁，防止死锁
                        byte[] oldValue = connection.getSet(key.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                        return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                    }
                }
            }
            return false;
        });
        return (boolean) execute;
    }

    /**
     * 校验锁
     *
     * @param k        redis key
     * @param function 方法体
     * @param <R>      返回值
     * @return
     */
    public <R> R checkLock(String k, Function<String, R> function) {
        return this.checkLock(k, function, 5, 100L);
    }

    /**
     * 校验锁
     *
     * @param k        redis key
     * @param function 方法体
     * @param tryCont  重试次数
     * @param millis   下次等待时间
     * @param <R>      返回值
     * @return
     */
    public <R> R checkLock(String k, Function<String, R> function, Integer tryCont, Long millis) {
        String key = LOCK_PREFIX + k;
        // redis分布式锁
        boolean lock = lock(key);
        try {
            if (lock) {
                // 执行逻辑操作
                return doBusiness(key, function);
            } else {
                // 设置失败次数计数器, 当到达5次时, 返回失败
                int failCount = 1;
                while (failCount <= tryCont) {
                    // 等待100ms重试
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        LOGGER.error("err. ", e);
                    }
                    if (lock(key)) {
                        // 执行逻辑操作
                        return doBusiness(key, function);
                    } else {
                        failCount++;
                    }
                }
                throw new RuntimeException("请稍等再试");
            }
        } finally {
            deleteObject(key);
        }
    }

    private <R> R doBusiness(String key, Function<String, R> function) {
        R apply = function.apply(key);
        deleteObject(key);
        return apply;
    }


    /**
     * 批量模式-一次性加多个Key进行处理
     *
     * @param ks       redis key
     * @param function 方法体
     * @param <R>      返回值
     * @return
     */
    public <R> R checkLockBatch(List<String> ks, Function<List<String>, R> function) {
        return this.checkLockBatch(ks, function, 5, 100L);
    }

    /**
     * 批量模式-一次性加多个Key进行处理
     *
     * @param ks       redis key
     * @param function 方法体
     * @param tryCont  重试次数
     * @param millis   下次等待时间
     * @param <R>      返回值
     * @return
     */
    public <R> R checkLockBatch(List<String> ks, Function<List<String>, R> function, Integer tryCont, Long millis) {
        List<String> keys = ks.stream().map(obj -> obj = LOCK_PREFIX + obj).collect(Collectors.toList());
        // redis分布式锁
        boolean lockResult = true;
        List<String> successKeys = new ArrayList<>();

        for (String key : keys) {//加锁
            boolean lock = this.lock(key);
            if (!lock) {
                int failCount = 1;
                while (failCount <= tryCont) {
                    // 等待100ms重试
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        LOGGER.error("err. ", e);
                    }
                    if (lock(key)) {
                        successKeys.add(key);
                    } else {
                        failCount++;
                    }
                }
                lockResult = false;
            } else {
                successKeys.add(key);
            }
        }
        //有失败的说明有一个失败了。失败的就需要解锁之前加锁成功的
        if (!lockResult) {
            successKeys.forEach(this::deleteObject);
            throw new CustomException("操作人数太多,请稍后再试！");
        }
        try {
            return doBusinessBatch(successKeys, function);
        } catch (Exception e) {
            throw e;
        } finally {
            successKeys.forEach(this::deleteObject);
        }
    }


    private <R> R doBusinessBatch(List<String> keys, Function<List<String>, R> function) {
        R apply = function.apply(keys);
        keys.forEach(this::deleteObject);
        return apply;
    }

    /**
     * 删除多个对象
     *
     * @param keys
     */
    public boolean deleteObjectBatch(final List<String> keys) {
        redisTemplate.delete(keys);
        return true;
    }

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.local.service;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.utils.SpringUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 三方接口缓存工具类
 * 使用guava LoadingCache(单例创建)
 * 本地多语言缓存最多保留1小时, 如果本地缓存没有命中, 会自动调用load()方法从redis中获取缓存
 * 如果redis中缓存同样无法命中, 将缓存的key当成value存入本地缓存中并作为返回结果返回
 * ⚠️: 因缓存命中逻辑的特殊性, 本类仅仅适用于多行业属性语言版本, 普通<K,V>本地缓存请使用其他工具类
 *
 * @author cosmo-hhim-open Team
 * @date 2022-07-04
 */
//@Component
public class ThirdInterfaceCache {

    private static final Logger log = LoggerFactory.getLogger(ThirdInterfaceCache.class);

    private static volatile ThirdInterfaceCache thirdInterfaceCache;
    private static volatile LoadingCache<String, Object> cacheServer;


    private ThirdInterfaceCache() {
        /*
        expireAfterWrite 是在指定项在一定时间内没有创建 / 覆盖时，会移除该 key，下次取的时候从 load 中取
        expireAfterAccess 是指定项在一定时间内没有读写，会移除该 key，下次取的时候从 load 中取
        refreshAfterWrite 是在指定时间内没有被创建 / 覆盖，则指定时间过后，再次访问时，会去刷新该缓存，在新值没有到来之前，始终返回旧值
        跟 expire 的区别是，指定时间过后，expire 是 remove 该 key，下次访问是同步去获取返回新值；
        而 refresh 则是指定时间后，不会 remove 该 key，下次访问会触发刷新，新值没有回来时返回旧值
         */
        cacheServer = CacheBuilder.newBuilder()
                .maximumSize(Integer.MAX_VALUE)
                .concurrencyLevel(2)
                .initialCapacity(10)
                .refreshAfterWrite(60, TimeUnit.MINUTES)
                .removalListener(notification -> log.debug(notification.getKey() + " 被移除了，原因： " + notification.getCause()))
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String key) throws Exception {
                        log.debug("thirdInterfaceCache can not hit cache, get cache from redis, the key is [{}]", key);
                        return getLoadData(key);
                    }
                });
    }

    /**
     * 获取ThirdInterfaceCache单例
     *
     * @return ThirdInterfaceCache
     */
    public static ThirdInterfaceCache getInstance() {
        if (thirdInterfaceCache == null) {
            synchronized (ThirdInterfaceCache.class) {
                if (thirdInterfaceCache == null) {
                    thirdInterfaceCache = new ThirdInterfaceCache();
                }
            }
        }
        return thirdInterfaceCache;
    }

    /**
     * 获取三方接口缓存中的值
     * 如果发生ExecutionException, 说明value为null(正常情况不会发生)
     *
     * @return 三方接口的value值
     */
    public Object getCacheObject(String customer, String method, String version) {
        String customKey = getCustomKey(customer, method, version);

        try {
            return cacheServer.get(customKey);
        } catch (ExecutionException e) {
            log.warn("local thirdInterfaceCache is null, the key is [{}]", customKey);
            Object loadData = getLoadData(customKey);
            if (!Objects.isNull(loadData)) {
                this.setCacheObject(customer, method, version, loadData);
            }
            return loadData;
        }
    }


    /**
     * 首先要组合key值, 然后存入本地缓存
     *
     * @param value 要存入本地缓存的value值 租户 方法 版本号为key
     */
    private void setCacheObject(String customer, String method, String version, Object value) {
        cacheServer.put(getCustomKey(customer, method, version), value);
    }

    /**
     * 首先要组合key值, 然后存入本地缓存
     *
     * @param value 要存入本地缓存的value值
     */
    public void setRedisCache(String customer, String method, String version, Object value) {
        RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
        redisTemplate.opsForValue().set(getCustomKey(customer, method, version), value);
    }

//    /**
//     * 从缓存中清除某一个单独的key
//     */
//    public void removeCache(String key) {
//        cacheServer.invalidate(getCustomKey(key));
//    }

//    /**
//     * 从缓存中批量清除某一个单独的key
//     */
//    public void removeBatchCache(Set<String> keys) {
//        cacheServer.invalidateAll(keys.stream().map(this::getCustomKey).collect(Collectors.toSet()));
//    }

    /**
     * 存储的key值为tenantCode+method+version
     * 此方法是组合key值
     */
    private String getCustomKey(String customer, String method, String version) {
        return CacheConstants.THIRD_INTERFACE_KEY + "method_orderly:" + customer + ":" + method + ":" + version;
    }

    /**
     * 如果guava缓存中无法命中key,调用redis方法查询key
     * 如果调用redis发生异常, 则返回一个空的字符串(不为null, null在guavaCache中会报错)
     * <p>
     */
    private Object getLoadData(String key) {
        try {
            RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
            Object obj = redisTemplate.opsForValue().get(key);
            //目前写死中文环境
//            return Objects.isNull(obj) && Locale.CHINA.equals(Locale.CHINA) ? key.substring(key.lastIndexOf(":") + 1) : obj;
            return Objects.isNull(obj) ? "NA" : obj;
        } catch (Exception e) {
            log.error("Failed to load thirdInterfaceCache from redis, the key: [{}], Error message:{}:{}, Error:{}", key, e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return "";
        }
    }
}
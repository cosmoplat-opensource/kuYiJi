/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.local.service;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.SpringUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 本地多语言缓存工具类
 * 使用guava LoadingCache(单例创建)
 * 本地多语言缓存最多保留1小时, 如果本地缓存没有命中, 会自动调用load()方法从redis中获取缓存
 * 如果redis中缓存同样无法命中, 将缓存的key当成value存入本地缓存中并作为返回结果返回
 * ⚠️: 因缓存命中逻辑的特殊性, 本类仅仅适用于多行业属性语言版本, 普通<K,V>本地缓存请使用其他工具类
 *
 * @author cosmo-hhim-open Team
 * @date 2022-07-04
 */
//@Component
public class MultiLangCache {

    private static final Logger log = LoggerFactory.getLogger(MultiLangCache.class);

    private static volatile MultiLangCache multiLangCache;
    private static volatile LoadingCache<String, Object> cacheServer;


    private MultiLangCache() {
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
                    public Object load(String key) {
                        log.debug("multiLangCache can not hit cache, get cache from redis, the key is [{}]", key);
                        return getLoadData(key);
                    }
                });
    }

    /**
     * 获取MultiLangCache单例
     *
     * @return MultiLangCache
     */
    public static MultiLangCache getInstance() {
        if (multiLangCache == null) {
            synchronized (MultiLangCache.class) {
                if (multiLangCache == null) {
                    multiLangCache = new MultiLangCache();
                }
            }
        }
        return multiLangCache;
    }

    /**
     * 获取本地多语言缓存中的值
     * 如果发生ExecutionException, 说明value为null(正常情况不会发生), 此时调用本类中getLoadData(String key)方法
     *
     * @param key 要查询的多行业属性语言
     * @return 本地缓存的value值
     */
    public Object getCacheObject(String key) {
        String customKey = getCustomKey(key);
        try {
            return cacheServer.get(customKey);
        } catch (ExecutionException e) {
            log.warn("local multiLangCache is null, the key is [{}]", customKey);
            Object loadData = getLoadData(customKey);
            if (!Objects.isNull(loadData)) {
                this.setCacheObject(key, loadData);
            }
            return loadData;
        }
    }

    /**
     * 首先要组合key值, 然后存入本地缓存
     *
     * @param key   要查询的多行业属性语言
     * @param value 要存入本地缓存的value值
     */
    private void setCacheObject(String key, Object value) {
        cacheServer.put(getCustomKey(key), value);
    }

    /**
     * 首先要组合key值, 然后存入本地缓存
     *
     * @param key   要查询的多行业属性语言
     * @param value 要存入本地缓存的value值
     */
    public void setRedisCache(String key, Object value) {
       RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
        redisTemplate.opsForValue().set(getCustomKey(key), value);
    }

    /**
     * 从缓存中清除某一个单独的key
     */
    public void removeCache(String key) {
        cacheServer.invalidate(getCustomKey(key));
    }

    /**
     * 从缓存中批量清除某一个单独的key
     */
    public void removeBatchCache(Set<String> keys) {
        cacheServer.invalidateAll(keys.stream().map(this::getCustomKey).collect(Collectors.toSet()));
    }

    /**
     * 由于多行业属性缓存的特殊性, 存储的key值为tenantCode+locale+key
     * 此方法是组合key值
     *
     * @param key 要查询的多行业属性语言
     */
    private String getCustomKey(String key) {
        return CacheConstants.MULTI_LANG_KEY + ThreadContext.get(Constants.TARGET_CUSTOMER) + ":" + Locale.CHINA.toString() + ":" + key;
    }

    /**
     * 如果guava缓存中无法命中key,调用redis方法查询key
     * value如果为空 并且 当前语言环境为中国中文 说明此次查询为多行业属性语言查询, 将key当做value返回
     * 如果调用redis发生异常, 则返回一个空的字符串(不为null, null在guavaCache中会报错)
     * <p>
     * TODO 后期要加一层从DB中获取数据(兜底方案)
     *
     * @param key 要查询的多行业属性语言
     */
    private Object getLoadData(String key) {
        try {
            RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
            Object obj =  redisTemplate.opsForValue().get(key);
            //目前写死中文环境
//            return Objects.isNull(obj) && Locale.CHINA.equals(Locale.CHINA) ? key.substring(key.lastIndexOf(":") + 1) : obj;
            return Objects.isNull(obj) ? key.substring(key.lastIndexOf(":") + 1) : obj;
        } catch (Exception e) {
            log.error("Failed to load multiLangCache from redis, the key: [{}], Error message:{}:{}, Error:{}", key, e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return "";
        }
    }
}
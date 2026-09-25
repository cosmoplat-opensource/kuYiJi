/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.multidb.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 序列化方案管理
 * @createTime 2021-09-10
 */
public class RedisSerializerManager {

    // 默认序列化方式
    private static RedisSerializer defaultSerializer;
    // 默认序列化方案
    private static RedisSerializerEntity defaultSerializerEntity;

    static {
        defaultSerializer = new StringRedisSerializer();

        defaultSerializerEntity = new RedisSerializerEntity();
        defaultSerializerEntity.setKeySerializer(defaultSerializer);
        defaultSerializerEntity.setValueSerializer(defaultSerializer);
        defaultSerializerEntity.setHashKeySerializer(defaultSerializer);
        defaultSerializerEntity.setHashValueSerializer(defaultSerializer);
    }

    // 维护添加的自定义的序列化方案
    private Map<Integer, RedisSerializerEntity> serializerEntityMap = new HashMap<>();

    public static RedisSerializerEntity getDefaultSerializerEntity() {
        return defaultSerializerEntity;
    }

    public static void setDefaultSerializerEntity(RedisSerializerEntity defaultSerializerEntity) {
        RedisSerializerManager.defaultSerializerEntity = defaultSerializerEntity;
    }

    /**
     * 添加序列化方案
     *
     * @param dbIndex
     * @param redisSerializerEntity
     */
    public void put(Integer dbIndex, RedisSerializerEntity redisSerializerEntity) {
        serializerEntityMap.put(dbIndex, redisSerializerEntity);
    }

    /**
     * 获取序列化方案
     *
     * @param dbIndex
     * @return
     */
    public RedisSerializerEntity get(Integer dbIndex) {
        if(null == serializerEntityMap.get(dbIndex)){
            return getDefaultSerializerEntity();
        }
        return serializerEntityMap.get(dbIndex);
    }

    public static RedisSerializer getDefaultSerializer() {
        return defaultSerializer;
    }

    public static void setDefaultSerializer(RedisSerializer defaultSerializer) {
        RedisSerializerManager.defaultSerializer = defaultSerializer;
    }

    /**
     * key和value的序列化方案
     */
    public static class RedisSerializerEntity {

        // key的序列化方案
        private RedisSerializer<?> keySerializer = defaultSerializer;

        // value的序列化方案
        private RedisSerializer<?> valueSerializer = defaultSerializer;

        // hash key的序列化方案
        private RedisSerializer<?> hashKeySerializer = defaultSerializer;

        // hash value的序列化方案
        private RedisSerializer<?> hashValueSerializer = defaultSerializer;

        public RedisSerializer<?> getHashKeySerializer() {
            return hashKeySerializer;
        }

        public void setHashKeySerializer(RedisSerializer<?> hashKeySerializer) {
            this.hashKeySerializer = hashKeySerializer;
        }

        public RedisSerializer<?> getHashValueSerializer() {
            return hashValueSerializer;
        }

        public void setHashValueSerializer(RedisSerializer<?> hashValueSerializer) {
            this.hashValueSerializer = hashValueSerializer;
        }

        public RedisSerializer<?> getKeySerializer() {
            return keySerializer;
        }

        public void setKeySerializer(RedisSerializer<?> keySerializer) {
            this.keySerializer = keySerializer;
        }

        public RedisSerializer<?> getValueSerializer() {
            return valueSerializer;
        }

        public void setValueSerializer(RedisSerializer<?> valueSerializer) {
            this.valueSerializer = valueSerializer;
        }
    }
}

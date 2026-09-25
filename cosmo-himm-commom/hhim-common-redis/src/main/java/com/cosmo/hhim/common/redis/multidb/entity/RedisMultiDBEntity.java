/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.multidb.entity;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description Redis多数据库配置信息类
 * @createTime 2021-09-11
 */
public class RedisMultiDBEntity extends RedisProperties {

    private MultiDB multiDB;

    public MultiDB getMultiDB() {
        return multiDB;
    }

    public void setMultiDB(MultiDB multiDB) {
        this.multiDB = multiDB;
    }

    public static class MultiDB {
        private String redisTemplatePrefix = "redisTemplate_";
        private List<Integer> databases;
        private String redisClientType = "jedis";

        public String getRedisClientType() {
            return redisClientType;
        }

        public void setRedisClientType(String redisClientType) {
            this.redisClientType = redisClientType;
        }

        public String getRedisTemplatePrefix() {
            return redisTemplatePrefix;
        }

        public void setRedisTemplatePrefix(String redisTemplatePrefix) {
            this.redisTemplatePrefix = redisTemplatePrefix;
        }

        public List<Integer> getDatabases() {
            return databases;
        }

        public void setDatabases(List<Integer> databases) {
            this.databases = databases;
        }
    }
}

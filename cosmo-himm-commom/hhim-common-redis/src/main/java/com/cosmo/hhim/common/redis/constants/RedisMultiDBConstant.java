/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.constants;

/**
 * @author cosmo-hhim-open Team
 * @description Redis多数据库枚举常量定义
 * @createTime 2021-09-11
 */
public class RedisMultiDBConstant {

    public static final String REDIS_MULTIDB_BIND_NAME = "spring.redis";

    /**
     * redis客户端类型
     */
    public enum RedisClientTypeEnum {

        JEDIS_CLIENT("jedis", "jedis客户端"),
        LETTUCE_CLIENT("lettuce", "lettuce客户端");

        private String key;
        private String desc;

        RedisClientTypeEnum(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }

        public static RedisClientTypeEnum parse(String key) {
            for (RedisClientTypeEnum typeEnum : RedisClientTypeEnum.values()) {
                if (typeEnum.getKey().equals(key)) {
                    return typeEnum;
                }
            }
            return null;
        }
    }
}

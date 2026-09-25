/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.multidb;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description Redis多数据库操作模版管理类
 * @createTime 2021-09-11
 */
public class RedisMultiDBTemplateManager {

    private Map<String, RedisTemplate> redisTemplateMap;

    private final String redisTemplatePrefix;

    public RedisMultiDBTemplateManager(Map<String, RedisTemplate> redisTemplateMap, String redisTemplatePrefix){
        this.redisTemplateMap = redisTemplateMap;
        this.redisTemplatePrefix = redisTemplatePrefix;
    }

    public RedisTemplate getRedisTemplate(Integer database) {
        return redisTemplateMap.get(redisTemplatePrefix + database);
    }

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.configure;

import com.cosmo.hhim.common.redis.multidb.RedisMultiDBRegistrar;
import com.cosmo.hhim.common.redis.multidb.RedisMultiDBTemplateManager;
import com.cosmo.hhim.common.redis.multidb.entity.RedisMultiDBEntity;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.cosmo.hhim.common.redis.constants.RedisMultiDBConstant.REDIS_MULTIDB_BIND_NAME;

/**
 * @author cosmo-hhim-open Team
 * @description Redis多数据库自动配置类
 * @createTime 2021-09-10
 */
@AutoConfigureAfter({RedisAutoConfiguration.class})
@Import(RedisMultiDBRegistrar.class)
public class RedisMultiDBAutoConfig implements EnvironmentAware, ApplicationContextAware {

    private Map<String, RedisTemplate> redisTemplateMap = new HashMap<>();
    private ApplicationContext applicationContext;
    private Binder binder;
    private String redisTemplatePrefix;

    @PostConstruct
    public void init() {
        RedisMultiDBEntity redisMultiDBEntity;
        try {
            redisMultiDBEntity = binder.bind(REDIS_MULTIDB_BIND_NAME, RedisMultiDBEntity.class).get();
        } catch (NoSuchElementException e) {
            throw new RuntimeException("multiDB redis配置加载失败，配置文件中" + REDIS_MULTIDB_BIND_NAME + "配置信息未指定！");
        }
        redisTemplatePrefix = redisMultiDBEntity.getMultiDB().getRedisTemplatePrefix();

        List<Integer> databases = redisMultiDBEntity.getMultiDB().getDatabases();
        if (!CollectionUtils.isEmpty(databases)) {
            for (Integer database : databases) {
                String key = redisTemplatePrefix + database;
                RedisTemplate redisTemplate = applicationContext.getBean(key, RedisTemplate.class);
                if (null != redisTemplate) {
                    redisTemplateMap.put(key, redisTemplate);
                }
            }
        }

    }

    @Bean
    public RedisMultiDBTemplateManager redisTemplateMap() {
        return new RedisMultiDBTemplateManager(redisTemplateMap, redisTemplatePrefix);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.binder = Binder.get(environment);
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.multidb;

import com.cosmo.hhim.common.redis.multidb.entity.RedisMultiDBEntity;
import com.cosmo.hhim.common.redis.multidb.serializer.RedisSerializerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.NoSuchElementException;

import static com.cosmo.hhim.common.redis.constants.RedisMultiDBConstant.REDIS_MULTIDB_BIND_NAME;

/**
 * @author cosmo-hhim-open Team
 * @description 自动注入Redis多数据库spring Import注册器
 * @createTime 2021-09-11
 */
public class RedisMultiDBRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMultiDBRegistrar.class);

    private Binder binder;

    @Override
    public void setEnvironment(Environment environment) {
        this.binder = Binder.get(environment);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        RedisMultiDBEntity redisMultiDBEntity;
        try {
            redisMultiDBEntity = binder.bind(REDIS_MULTIDB_BIND_NAME, RedisMultiDBEntity.class).get();
        } catch (NoSuchElementException e) {
            LOGGER.error("multiDB redis配置加载失败，配置文件中{} 配置信息未指定！", REDIS_MULTIDB_BIND_NAME);
            return;
        }

        List<Integer> databases = redisMultiDBEntity.getMultiDB().getDatabases();
        if (CollectionUtils.isEmpty(databases)) {
            return;
        }
        RedisSerializerManager redisSerializerManager = new RedisSerializerManager();
        for (Integer db : databases) {
            RedisTemplateDefinitionBuilder redisTemplateDefinitionBuilder = new RedisTemplateDefinitionBuilder(db,
                    redisSerializerManager, redisMultiDBEntity);
            registry.registerBeanDefinition(redisMultiDBEntity.getMultiDB().getRedisTemplatePrefix() + db,
                    redisTemplateDefinitionBuilder.builder());
        }

    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.multidb;

import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.redis.constants.RedisMultiDBConstant;
import com.cosmo.hhim.common.redis.multidb.entity.RedisMultiDBEntity;
import com.cosmo.hhim.common.redis.multidb.serializer.RedisSerializerManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description RedisTemplate的自定义BeanDefinition
 * @createTime 2021-09-10
 */
public class RedisTemplateDefinitionBuilder {
    private final Integer dbIndex;
    private final RedisMultiDBEntity redisMultiDBEntity;

    private RedisSerializerManager.RedisSerializerEntity redisSerializerEntity;

    public RedisTemplateDefinitionBuilder(Integer dbIndex, RedisSerializerManager redisSerializerManager,
                                          RedisMultiDBEntity redisMultiDBEntity) {
        this.dbIndex = dbIndex;
        this.redisMultiDBEntity = redisMultiDBEntity;
        RedisSerializerManager.RedisSerializerEntity redisSerializerEntity = redisSerializerManager.get(dbIndex);
        if (redisSerializerEntity != null) {
            this.redisSerializerEntity = redisSerializerEntity;
        }
    }

    public BeanDefinition builder() {
        GenericBeanDefinition redisTemplate = new GenericBeanDefinition();
        redisTemplate.setBeanClass(RedisTemplate.class);
        RedisConnectionFactory connectionFactory = null;

        // TODO 这里暂无方案自动判断redis客户端使用的类型，故先通过配置文件手动设置，后期再优化
        switch (RedisMultiDBConstant.RedisClientTypeEnum.parse(redisMultiDBEntity.getMultiDB().getRedisClientType())) {
            case JEDIS_CLIENT:
                connectionFactory = this.getJedisFactory(dbIndex);
                break;
            case LETTUCE_CLIENT:
                connectionFactory = this.getLettuceFactory(dbIndex);
                break;
        }
        redisTemplate.getPropertyValues().add("connectionFactory", connectionFactory);
        redisTemplate.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME);

        // key采用String的序列化方式，value采用json序列化方式
        redisTemplate.getPropertyValues().add("keySerializer", redisSerializerEntity.getKeySerializer());
        redisTemplate.getPropertyValues().add("valueSerializer", redisSerializerEntity.getValueSerializer());
        redisTemplate.getPropertyValues().add("hashKeySerializer", redisSerializerEntity.getHashKeySerializer());
        redisTemplate.getPropertyValues().add("hashValueSerializer", redisSerializerEntity.getHashValueSerializer());
        return redisTemplate;
    }

    @Scope(scopeName = "prototype")
    private JedisConnectionFactory getJedisFactory(int dbIndex) {
        //根据配置和客户端配置创建连接
        JedisConnectionFactory jedisConnectionFactory = null;
        if (redisMultiDBEntity.getSentinel() == null && redisMultiDBEntity.getCluster() == null) { //单机模式
            jedisConnectionFactory = new JedisConnectionFactory(redisConfiguration(), jedisClientConfiguration());
            jedisConnectionFactory.setDatabase(dbIndex);
            jedisConnectionFactory.afterPropertiesSet();
        } else if (redisMultiDBEntity.getCluster() == null) { //哨兵模式
            jedisConnectionFactory = new JedisConnectionFactory(getSentinelConfiguration(), jedisClientConfiguration());
            jedisConnectionFactory.setDatabase(dbIndex);
            jedisConnectionFactory.afterPropertiesSet();
        } else { //集群模式
            jedisConnectionFactory = new JedisConnectionFactory(getRedisClusterConfiguration(), jedisClientConfiguration());
            jedisConnectionFactory.setDatabase(dbIndex);
            jedisConnectionFactory.afterPropertiesSet();
        }
        return jedisConnectionFactory;
    }


    @Scope(scopeName = "prototype")
    private LettuceConnectionFactory getLettuceFactory(int dbIndex) {
        //根据配置和客户端配置创建连接
        LettuceConnectionFactory lettuceConnectionFactory = null;
        if (redisMultiDBEntity.getSentinel() == null && redisMultiDBEntity.getCluster() == null) { //单机模式
            lettuceConnectionFactory = new LettuceConnectionFactory(redisConfiguration(), lettuceClientConfiguration());
            lettuceConnectionFactory.setDatabase(dbIndex);
            lettuceConnectionFactory.afterPropertiesSet();
        } else if (redisMultiDBEntity.getCluster() == null) { //哨兵模式
            lettuceConnectionFactory = new LettuceConnectionFactory(getSentinelConfiguration(), lettuceClientConfiguration());
            lettuceConnectionFactory.setDatabase(dbIndex);
            lettuceConnectionFactory.afterPropertiesSet();
        } else { //集群模式
            lettuceConnectionFactory = new LettuceConnectionFactory(getRedisClusterConfiguration(), lettuceClientConfiguration());
            lettuceConnectionFactory.setDatabase(dbIndex);
            lettuceConnectionFactory.afterPropertiesSet();
        }
        return lettuceConnectionFactory;
    }

    /**
     * redis单机配置
     *
     * @return
     */
    private RedisStandaloneConfiguration redisConfiguration() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisMultiDBEntity.getHost());
        redisStandaloneConfiguration.setPort(redisMultiDBEntity.getPort());
        //设置密码
        if (redisMultiDBEntity.getPassword() != null) {
            redisStandaloneConfiguration.setPassword(RedisPassword.of(redisMultiDBEntity.getPassword()));
        }
        return redisStandaloneConfiguration;
    }

    /**
     * redis哨兵配置
     *
     * @return
     */
    private RedisSentinelConfiguration getSentinelConfiguration() {
        RedisProperties.Sentinel sentinel = redisMultiDBEntity.getSentinel();
        if (sentinel != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.setMaster(sentinel.getMaster());
            if (!StringUtils.isEmpty(redisMultiDBEntity.getPassword())) {
                config.setPassword(RedisPassword.of(redisMultiDBEntity.getPassword()));
            }
            if (StringUtils.isNotEmpty(redisMultiDBEntity.getSentinel().getPassword())) {
                config.setSentinelPassword(RedisPassword.of(redisMultiDBEntity.getSentinel().getPassword()));
            }
            config.setSentinels(createSentinels(sentinel));
            return config;
        }
        return null;
    }

    /**
     * 获取哨兵节点
     *
     * @param sentinel
     * @return
     */
    private List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : sentinel.getNodes()) {
            String[] parts = StringUtils.split(node, ":");
            Assert.state(parts.length == 2, "redis哨兵地址配置不合法！");
            nodes.add(new RedisNode(parts[0], NumberUtils.toInt(parts[1])));
        }
        return nodes;
    }

    /**
     * redis集群配置
     *
     * @return
     */
    private RedisClusterConfiguration getRedisClusterConfiguration() {
        RedisProperties.Cluster cluster = redisMultiDBEntity.getCluster();
        if (cluster != null) {
            RedisClusterConfiguration config = new RedisClusterConfiguration();
            config.setClusterNodes(createCluster(cluster));
            if (!StringUtils.isEmpty(redisMultiDBEntity.getPassword())) {
                config.setPassword(RedisPassword.of(redisMultiDBEntity.getPassword()));
            }
            config.setMaxRedirects(redisMultiDBEntity.getCluster().getMaxRedirects());
            return config;
        }
        return null;
    }

    /**
     * 获取集群节点
     *
     * @param cluster
     * @return
     */
    private List<RedisNode> createCluster(RedisProperties.Cluster cluster) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : cluster.getNodes()) {
            String[] parts = StringUtils.split(node, ":");
            Assert.state(parts.length == 2, "redis哨兵地址配置不合法！");
            nodes.add(new RedisNode(parts[0], NumberUtils.toInt(parts[1])));
        }
        return nodes;
    }


    /**
     * 连接池配置
     */
    private GenericObjectPoolConfig redisPool() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(redisMultiDBEntity.getLettuce().getPool().getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisMultiDBEntity.getLettuce().getPool().getMinIdle());
        genericObjectPoolConfig.setMaxTotal(redisMultiDBEntity.getLettuce().getPool().getMaxActive());
        genericObjectPoolConfig.setTestOnBorrow(true);
        genericObjectPoolConfig.setTestWhileIdle(true);
        genericObjectPoolConfig.setTestOnReturn(false);
        genericObjectPoolConfig.setMaxWaitMillis(5000);
        return genericObjectPoolConfig;
    }

    /**
     * redis客户端配置
     */
    private LettuceClientConfiguration lettuceClientConfiguration() {
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();
        builder.commandTimeout(redisMultiDBEntity.getLettuce().getShutdownTimeout());
        builder.shutdownTimeout(redisMultiDBEntity.getLettuce().getShutdownTimeout());
        builder.poolConfig(redisPool());
        return builder.build();
    }

    /**
     * redis客户端配置
     */
    private JedisClientConfiguration jedisClientConfiguration() {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = JedisClientConfiguration.builder();
        builder.connectTimeout(redisMultiDBEntity.getLettuce().getShutdownTimeout());
        builder.readTimeout(redisMultiDBEntity.getLettuce().getShutdownTimeout());
        builder.usePooling().poolConfig(redisPool());
        return builder.build();
    }
}

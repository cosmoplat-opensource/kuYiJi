/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.configure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * redis配置
 *
 * @author cosmo-hhim-open Team
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes", "deprecation"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);
        ParserConfig.getGlobalInstance().setAsmEnable(true);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(new MyGenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCache getRedisCacheBean() {
        return new RedisCache();
    }

    class MyGenericJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {

        @Override
        public byte[] serialize(Object object) throws SerializationException {
            if (object == null) {
                return new byte[0];
            }
            if (object instanceof Long || object instanceof Double) {
                return object.toString().getBytes(Charset.forName("UTF-8"));
            }
            try {
                return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
            } catch (Exception exception) {
                throw new SerializationException("Could not serialize : " + exception.getMessage(), exception);
            }
        }

    }


}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * Redis Stream 消息生产者（替代原 RocketMQ 生产者）。
 * 发送 JSON 字符串到指定 stream（hash 结构，payload 字段承载消息体），由消费者组内成员消费。
 */
@Slf4j
@Component
public class RedisStreamProducer {

    public static final String FIELD_PAYLOAD = "payload";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送消息到指定 stream（stream 不存在时自动创建）。
     *
     * @param streamKey stream 名称
     * @param payload   JSON 字符串
     * @return 消息 ID
     */
    public RecordId send(String streamKey, String payload) {
        RecordId recordId = stringRedisTemplate.opsForStream()
                .add(StreamRecords.string(Collections.singletonMap(FIELD_PAYLOAD, payload)).withStreamKey(streamKey));
        log.debug("Redis Stream 消息已发送, stream:{}, messageId:{}, payload:{}", streamKey, recordId, payload);
        return recordId;
    }
}

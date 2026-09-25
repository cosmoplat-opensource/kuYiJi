/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

/**
 * Redis Stream 消息消费容器（替代原 RocketMQ 消费者注册）。
 *
 * <p>轻量轮询实现（spring-data-redis 2.3 的 StreamMessageListenerContainer 序列化行为不透明，
 * 这里直接基于 StringRedisTemplate 的 XREADGROUP 阻塞读取，语义完全可控）：
 * <ul>
 *   <li>每个注册的 stream 起一个独立守护线程，单线程按序消费（满足原"有序队列"语义）；</li>
 *   <li>消息处理成功后才 XACK；处理失败不确认，消息保留在 PEL，
 *       由业务侧落库 + 补偿任务（/external/retryTask）兜底；</li>
 *   <li>消费组不存在时自动创建。</li>
 * </ul>
 */
@Slf4j
@Component
public class RedisStreamContainer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 注册一个 stream 消费者并启动消费线程。
     *
     * @param streamKey stream 名称
     * @param group     消费组
     * @param listener  消息监听器
     */
    public void register(String streamKey, String group, AbstractRedisStreamListener listener) {
        Thread consumerThread = new Thread(() -> consume(streamKey, group, listener),
                "redis-stream-consumer-" + streamKey);
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("Redis Stream 消费者已启动, stream:{}, group:{}", streamKey, group);
    }

    private void consume(String streamKey, String group, AbstractRedisStreamListener listener) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ensureGroup(streamKey, group);
                List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                        Consumer.from(group, "consumer-1"),
                        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1)),
                        StreamOffset.create(streamKey, ReadOffset.lastConsumed()));
                if (records == null || records.isEmpty()) {
                    continue;
                }
                for (MapRecord<String, Object, Object> record : records) {
                    String messageId = record.getId().getValue();
                    Object payloadObj = record.getValue().get(RedisStreamProducer.FIELD_PAYLOAD);
                    String payload = payloadObj == null ? null : String.valueOf(payloadObj);
                    try {
                        listener.handle(payload);
                        stringRedisTemplate.opsForStream().acknowledge(streamKey, group, record.getId());
                        log.debug("Redis Stream 消息处理成功并确认, stream:{}, messageId:{}", streamKey, messageId);
                    } catch (Exception e) {
                        // 处理失败：不确认（保留在 PEL），由业务侧落库 + 补偿任务兜底
                        log.error("Redis Stream 消息处理失败(不确认,等待补偿), stream:{}, group:{}, messageId:{}, error:{}",
                                streamKey, group, messageId, e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Redis Stream 消费异常, stream:{}, group:{}, error:{}", streamKey, group, e.getMessage(), e);
                sleepQuietly(1000);
            }
        }
    }

    private void ensureGroup(String streamKey, String group) {
        try {
            // 指定偏移 0：从 stream 头开始消费（默认 $ 会跳过建组前已存在的消息，
            // 消费者先于生产者启动时首条消息会被永久跳过）
            stringRedisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), group);
        } catch (Exception e) {
            // BUSYGROUP：消费组已存在；或 stream 尚不存在（等生产者首条消息创建），均忽略
            log.debug("Redis Stream 消费组创建跳过(已存在或 stream 未创建), stream:{}, group:{}", streamKey, group);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

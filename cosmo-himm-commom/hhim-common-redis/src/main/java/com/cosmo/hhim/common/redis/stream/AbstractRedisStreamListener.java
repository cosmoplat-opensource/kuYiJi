/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.stream;

/**
 * Redis Stream 消息监听器基类（替代原 RocketMQ 消费者）。
 *
 * <p>约定：{@link #handle(String)} 处理成功返回；处理失败抛出异常——消息不会被确认
 * （保留在 PEL 待确认队列），由业务侧落库 + 补偿任务（如 /external/retryTask）兜底，
 * 避免消息处理失败即丢失。
 */
public abstract class AbstractRedisStreamListener {

    /** 监听的 stream key */
    protected abstract String getStreamKey();

    /** 消费组名称 */
    protected abstract String getGroup();

    /**
     * 处理一条消息。
     *
     * @param payload 消息体（JSON 字符串）
     * @throws Exception 处理失败时抛出，消息不确认、等待补偿
     */
    protected abstract void handle(String payload) throws Exception;
}

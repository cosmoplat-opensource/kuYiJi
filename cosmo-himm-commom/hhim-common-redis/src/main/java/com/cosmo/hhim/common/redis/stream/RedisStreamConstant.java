/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.stream;

/**
 * Redis Stream 消息常量（替代原 RocketMQ topic 定义）。
 * 每个业务一条 stream + 一个消费组；多实例部署时同组内消息按顺序单消费者处理。
 */
public final class RedisStreamConstant {

    private RedisStreamConstant() {
    }

    /** 三方接口异步调用消息流（原 RocketMQ topic: thirdplatform_external_interface_*） */
    public static final String THIRD_INTERFACE_STREAM = "third_interface";

    /** 三方接口异步调用消费组 */
    public static final String THIRD_INTERFACE_GROUP = "third_interface_consumer";
}

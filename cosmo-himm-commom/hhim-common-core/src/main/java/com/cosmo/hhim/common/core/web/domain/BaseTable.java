/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * base_table 中间件基础类
 * @author cosmo-hhim-open Team
 */
@Data
public class BaseTable extends BaseEntity implements Serializable {
    private Long id;

    /**
     * 消息ID,防止重复发送
     */
    private String messageId;

    /**
     * 发送方(提供者)
     */
    private String provider;

    /**
     * 使用方(消费者)
     */
    private String consumer;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 关键字段1，运维快速定位
     */
    private String key1;

    /**
     * 关键字段2，运维快速定位
     */
    private String key2;

    /**
     * 关键字段3，运维快速定位
     */
    private String key3;

    /**
     * 消息内容，json结果
     */
    private MessageContent messageContent;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 处理次数(失败次数)
     */
    private Integer failCount;

    /**
     * 处理结果
     */
    private String resultMsg;

    /**
     * 识别码
     */
    private String identifyCode;

    /**
     * 校验码
     */
    private String privateKey;

    /**
     * 原始业务类型
     */
    private String oriBusinessType;

    /**
     * 消息内容，json结果
     */
    private MessageContent messageContentJson;
}
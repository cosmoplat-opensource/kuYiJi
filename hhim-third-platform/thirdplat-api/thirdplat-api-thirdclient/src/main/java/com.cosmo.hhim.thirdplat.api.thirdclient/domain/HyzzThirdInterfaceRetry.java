/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 对象 hyzz_third_interface_log
 */
@Data
public class HyzzThirdInterfaceRetry implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 请求唯一码
     */
    private String requestId;

    /**
     * 消息KEY
     */
    private String messageKey;
    /**
     * messageId
     */
    private String messageId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 客户系统平台/渠道
     */
    private String clientSupport;
    private String callback;
    private String callbackTopic;
    private String callbackTag;
    private String expandContent;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求方法版本
     */
    private String version;

    /**
     * 请求消息体
     */
    private String content;

    /**
     * 请求地址
     */
    private String requestUrl;


    /**
     * 发送状态(0发送失败 1发送成功 -1未发送)
     */
    private String sendStatus;
    /**
     * 客户端处理状态 0处理失败 1处理成功
     */
    private String clientStatus;

    /**
     * 重试次数
     */
    private Long retryCount;
    private String authInfo;

    private String requestType;

}

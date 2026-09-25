/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 对象 hyzz_third_interface_log
 */
@Data
public class HyzzThirdInterfaceLog implements Serializable {
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
    private String expandContent;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 首次请求时间
     */
    private Date requestTime;

    /**
     * 最终相应返回时间
     */
    private Date responseTime;

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

    /**
     * 创建人
     */
    private String createdBy = "thirdPlatform";

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy = "thirdPlatform";

    /**
     * 更新时间
     */
    private Date lastUpdDate;
    /**
     * VO冗余字段
     */
    private String authInfo;

}

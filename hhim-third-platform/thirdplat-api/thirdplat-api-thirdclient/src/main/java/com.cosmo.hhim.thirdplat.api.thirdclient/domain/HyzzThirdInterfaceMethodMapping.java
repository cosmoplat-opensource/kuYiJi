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
public class HyzzThirdInterfaceMethodMapping implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private String callback;
    private String callbackTopic;
    private String callbackTag;
    /**
     * 请求方式
     */
    private String requestType;

    /**
     * 请求方法版本
     */
    private String version;

    /**
     * 请求路径
     */
    private String requestUrl;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 签名类型
     */
    private String signType;
    /**
     * 公钥
     */
    private String publicKey;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * 第三方平台所需额外的认证信息等
     */
    private String authInfo;

}

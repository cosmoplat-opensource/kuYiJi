/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.third;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求第三方租户接口实体
 */
@Data
public class ThirdInterfaceTenant implements Serializable {
    private Long id;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 签名类型
     */
    private String signType;
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 请求路径
     */
    private String requestUrl;
    /**
     * 请求私钥
     */
    private String privateKey;
    /**
     * 公钥
     */
    private String publicKey;

}

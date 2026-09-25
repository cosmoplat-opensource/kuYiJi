/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 调用第三方接口租户配置对象 hyzz_third_interface_tenant
 *
 * @author cosmo-hhim-open Team
 * @date 2022-09-20
 */
@Data
public class HyzzThirdInterfaceTenant implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户编码
     */
    private String tenantName;

    /**
     * $column.columnComment
     */
    private String clientSupport;

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
     * 请求地址
     */
    private String requestUrl;

    /**
     * 外部平台额外的认证信息
     */
    private String authInfo;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

}

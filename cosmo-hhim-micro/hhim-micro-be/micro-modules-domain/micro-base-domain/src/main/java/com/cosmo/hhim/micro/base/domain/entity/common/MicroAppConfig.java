/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 应用配置对象 micro_app_config
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroAppConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 应用编码
     */
    private String appCode;

    /**
     * 应用签名（保留字段，登录不卡）
     */
    private String appSign;

    /**
     * 客户产品类型
     */
    private Integer customerProductType;

    /**
     * 配置 JSON
     */
    private String configJson;

    /**
     * 状态 1=启用 0=停用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

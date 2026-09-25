/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

/**
 * 微应用-应用配置信息对象 hyzz_micro_application_config
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
@Data
public class HyzzMicroApplicationConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    // 应用名称
    private String applicationName;

    /**
     * 应用标识
     */
    private String applicationSign;

    /**
     * 应用编码
     */
    private String applicationCode;

    /**
     * 是否删除 0-正常，1-删除
     */
    private String deleted;

    /**
     * 租户编码
     */
    private String customerCode;

}

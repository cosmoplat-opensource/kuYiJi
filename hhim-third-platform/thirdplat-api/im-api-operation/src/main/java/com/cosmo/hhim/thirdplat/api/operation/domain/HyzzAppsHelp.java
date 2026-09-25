/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

/**
 * 应用帮助对象 hyzz_apps_help
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzAppsHelp extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 帮助ID
     */
    private Integer helpId;

    /**
     * 应用编码
     */
    private String appsCode;

    /**
     * 应用名称
     */
    private String appsName;

    /**
     * 帮助分类（）
     */
    private String helpType;

    /**
     * 帮助标题
     */
    private String helpTitle;

    /**
     * 帮助内容
     */
    private String helpContent;

    /**
     * 帮助状态（0正常 1关闭）
     */
    private String status;

}

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
public class BaseTableParam extends BaseTable implements Serializable {

    /**
     * 查询msg
     */
    private String searchMessageContent;

    /**
     * 查询msg
     */
    private String searchResultMsg;

    /**
     * 处理状态
     */
    private String businessStatus;

}
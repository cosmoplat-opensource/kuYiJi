/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

@Data
public class DataSourceInfo { 
    // 租户编码 
    private String customerCode;

    // 数据库schema 
    private String databaseName;

    // 数据源 
    private String datasource;
}
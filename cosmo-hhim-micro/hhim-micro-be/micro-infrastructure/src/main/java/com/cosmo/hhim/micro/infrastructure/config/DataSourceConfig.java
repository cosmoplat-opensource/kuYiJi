/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-04
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.druid.db0")
public class DataSourceConfig {

    // 数据库连接URL 
    private String url;

    // 用户名 
    private String username;

    // 密码 
    private String password;
}

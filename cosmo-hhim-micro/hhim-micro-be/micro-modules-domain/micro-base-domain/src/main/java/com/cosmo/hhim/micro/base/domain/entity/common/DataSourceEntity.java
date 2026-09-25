/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-04
 */
@Data
public class DataSourceEntity {
    // 数据库连接URL 
    private String url;

    // 用户名 
    private String username;

    // 密码 
    private String password;
}

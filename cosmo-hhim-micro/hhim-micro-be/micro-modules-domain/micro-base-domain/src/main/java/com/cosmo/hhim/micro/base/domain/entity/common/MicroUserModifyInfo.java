/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-10
 */
@Data
public class MicroUserModifyInfo extends DataSourceInfo {

    // 新用户名 
    private String newName;

    // 用户手机号 
    private String phonenumber;

}

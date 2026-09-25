/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/8
 */
@Data
public class MicroAppInfo {

    // 应用编码 
    private String appCode;

    // 应用名称 
    private String appName;

    // 应用标识 
    private String appSign;

}

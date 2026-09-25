/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-04
 */
@Data
public class MicroProcessRenewalInfo extends DataSourceInfo {
    // 应用AppId 
    private String appid;

    // 续费有效期截至时间 
    private Long customerValidDate;

}

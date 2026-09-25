/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 选租户入参
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectTenantParam {
    private String tempToken;
    private String tenantCode;
}

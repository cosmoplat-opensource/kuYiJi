/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

@Data
public class HyzzCustomJobResult {

    private String db;
    private String schema;
    private String tenantCode;
    private String taskParam;
    private Long cronTime;

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.third;

import lombok.Data;

@Data
public class ThirdInterfaceMessageHeader {
    private String requestId;
    private String method;
    private String version;
    private String tenantCode;
    //use this key to select queue. for example: orderId, productId ...
    private String hashKey;
    private String targetSchema;
    private String targetDs;
}
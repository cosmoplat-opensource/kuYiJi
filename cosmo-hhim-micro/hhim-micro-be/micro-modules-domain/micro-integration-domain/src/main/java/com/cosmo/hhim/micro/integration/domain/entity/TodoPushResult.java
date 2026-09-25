/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TodoPushResult implements Serializable { 
    private String data;
    private String msg;
    private String code;
    private Boolean isSuccess;
}

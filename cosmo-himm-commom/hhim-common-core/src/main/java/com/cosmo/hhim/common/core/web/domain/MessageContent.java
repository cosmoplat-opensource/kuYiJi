/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MessageContent implements Serializable {
    private Integer code = 200;
    private String msg = "处理成功";
    private String status = "S";
    private Object data;
}
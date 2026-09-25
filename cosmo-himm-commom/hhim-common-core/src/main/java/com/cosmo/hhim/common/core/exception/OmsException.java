/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception;

import com.cosmo.hhim.common.core.enums.PromptEnum;

/**
 * @author cosmo-hhim-open Team
 * @version 1.0.0
 * @date 2022/9/2 14:40
 */
public class OmsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    private Object data;

    public OmsException(PromptEnum promptEnum) {
        this.code = PromptEnum.getCode(promptEnum.name());
        this.message = PromptEnum.getMsg(promptEnum.name());
    }

    public OmsException(String message) {
        this.message = message;
    }

    public OmsException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public OmsException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public OmsException(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception;

import com.cosmo.hhim.common.core.enums.PromptEnum;

/**
 * 自定义异常
 *
 * @author cosmo-hhim-open Team
 */
public class CustomException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;

    public CustomException(PromptEnum promptEnum) {
        this.code = PromptEnum.getCode(promptEnum.name());
        this.message = PromptEnum.getMsg(promptEnum.name());
    }

    public CustomException(String message) {
        this.message = message;
    }

    public CustomException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public CustomException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }
}

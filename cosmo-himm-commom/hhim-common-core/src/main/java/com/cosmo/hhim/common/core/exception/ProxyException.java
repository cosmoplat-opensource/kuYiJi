/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception;

/**
 * @author cosmo-hhim-open Team
 * @description 自定义代理异常
 * @createTime 2021-09-07
 */
public class ProxyException extends RuntimeException {
    private static final long serialVersionUID = -5563106933655728813L;

    public ProxyException() {
        super();
    }

    public ProxyException(String message) {
        super(message);
    }

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyException(Throwable cause) {
        super(cause);
    }
}
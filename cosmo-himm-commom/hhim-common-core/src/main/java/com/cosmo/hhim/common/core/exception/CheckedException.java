/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception;

/**
 * 检查异常
 * 
 * @author cosmo-hhim-open Team
 */
public class CheckedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CheckedException(String message)
    {
        super(message);
    }

    public CheckedException(Throwable cause)
    {
        super(cause);
    }

    public CheckedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CheckedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

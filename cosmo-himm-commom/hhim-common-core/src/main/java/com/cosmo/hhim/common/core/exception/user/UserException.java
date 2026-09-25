/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception.user;

import com.cosmo.hhim.common.core.exception.BaseException;

/**
 * 用户信息异常类
 * 
 * @author cosmo-hhim-open Team
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}

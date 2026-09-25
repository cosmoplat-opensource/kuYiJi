/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception.file;

import com.cosmo.hhim.common.core.exception.BaseException;

/**
 * 文件信息异常类
 * 
 * @author cosmo-hhim-open Team
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.utils;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.utils.ServletUtils;


/**
 * 权限获取工具类
 * 
 * @author cosmo-hhim-open Team
 */
public class RequestUtils
{
    /**
     * 获取用户
     */
    public static String getUsername()
    {
        String username = ServletUtils.getRequest().getHeader(CacheConstants.DETAILS_USERNAME);
        return ServletUtils.urlDecode(username);
    }

}

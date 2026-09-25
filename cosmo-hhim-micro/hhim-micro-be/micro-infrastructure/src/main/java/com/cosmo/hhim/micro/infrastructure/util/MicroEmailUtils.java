/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.cosmo.hhim.common.core.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
public class MicroEmailUtils {

    /**
     * 邮件格式验证
     *
     * @param receivedBy
     * @return
     */
    public static boolean isValidEmail(String receivedBy) {
        if (StringUtils.isNotEmpty(receivedBy)) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", receivedBy);
        }
        return false;
    }
}

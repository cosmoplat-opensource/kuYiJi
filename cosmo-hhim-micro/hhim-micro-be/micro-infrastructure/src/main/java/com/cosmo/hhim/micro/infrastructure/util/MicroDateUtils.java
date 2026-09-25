/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-04
 */
public class MicroDateUtils {

    public static final String TIME_FORMAT_A = "yyyy-MM-dd";

    /**
     * 将Date类型转为指定格式的Str
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static String dateToFormatStr(Date date, String dateFormat) {
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = MicroDateUtils.TIME_FORMAT_A;
        }

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(dateFormat));
    }

}

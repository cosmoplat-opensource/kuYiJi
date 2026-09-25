/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-02-01
 */
public class DateUtil {

    /**
     * 增加或减少日期
     *
     * @param localDateTime
     * @param plusValue
     * @param unit
     * @return
     */
    public static LocalDateTime plus(LocalDateTime localDateTime, long plusValue, ChronoUnit unit) {
        return localDateTime.plus(plusValue, unit);
    }

    /**
     * 增加或减少日期
     *
     * @param date
     * @param plusValue
     * @param unit
     * @return
     */
    public static Date plus(Date date, long plusValue, ChronoUnit unit) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneOffset.of("+8")).toLocalDateTime();
        LocalDateTime plus = plus(localDateTime, plusValue, unit);
        return Date.from(plus.atZone(ZoneOffset.of("+8")).toInstant());
    }

    /**
     * Date 转换 LocalDate
     *
     * @param date
     * @return
     */
    public static LocalDate date2LocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

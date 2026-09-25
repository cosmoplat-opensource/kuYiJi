/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * @auther: zyh
 * @date: 2023/2/7
 */

@Getter
@AllArgsConstructor
public enum PeriodTimeUnitEnum implements BaseEnum<String> { 

    SECONDS(ChronoUnit.SECONDS.name(), ChronoUnit.SECONDS, "秒"),
    MINUTES(ChronoUnit.MINUTES.name(), ChronoUnit.MINUTES, "分钟"),
    HOURS(ChronoUnit.HOURS.name(), ChronoUnit.HOURS, "小时"),
    DAYS(ChronoUnit.DAYS.name(), ChronoUnit.DAYS, "天"),
    WEEKS(ChronoUnit.WEEKS.name(), ChronoUnit.WEEKS, "周"),
    MONTHS(ChronoUnit.MONTHS.name(), ChronoUnit.MONTHS, "月"),
    YEARS(ChronoUnit.YEARS.name(), ChronoUnit.YEARS, "年");

    private String code;

    private ChronoUnit chronoUnit;

    private String desc;

    public static PeriodTimeUnitEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> b.code.equals(code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        PeriodTimeUnitEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }

}

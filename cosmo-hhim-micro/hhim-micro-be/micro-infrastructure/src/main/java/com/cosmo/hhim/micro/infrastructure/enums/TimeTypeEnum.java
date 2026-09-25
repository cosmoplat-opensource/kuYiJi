/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * <p>
 * 时间类型参数 （本天、本月、本年、自定义）
 */

@Getter
@AllArgsConstructor
public enum TimeTypeEnum implements BaseEnum<String> {

    /**
     * 本天
     */
    CURRENT_DAY("0", "本天"),

    /**
     * 本月
     */
    CURRENT_MONTH("1", "本月"),

    /**
     * 本年
     */
    CURRENT_YEAR("2", "本年"),

    /**
     * 自定义
     */
    UNDEFINED_TIME_TYPE("3", "自定义");

    private String code;
    private String desc;

    public static TimeTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        TimeTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.planning;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @descirption: 生产订单警示标示枚举
 */
@Getter
@AllArgsConstructor
public enum OrderWarnFlagEnum implements BaseEnum<String> {

    /**
     * 交付预警
     */
    DELIVERY_WARN("10", "交付预警"),

    /**
     * 草稿
     */
    OVER_DATE_WARN("20", "逾期预警");

    private String code;

    private String desc;

    public static OrderWarnFlagEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        OrderWarnFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

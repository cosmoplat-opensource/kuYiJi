/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.base;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @descirption: 订单状态枚举
 */

@Getter
@AllArgsConstructor
public enum ProductionModeEnum implements BaseEnum<String> {

    /**
     * 自制
     */
    SELF_CONTROL("10", "自制"),

    /**
     * 外购
     */
    OUTSOURCING("20", "外购"),

    /**
     * 委外
     */
    SUBCONTRACTING("30", "委外");


    private String code;

    private String desc;

    public static ProductionModeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ProductionModeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

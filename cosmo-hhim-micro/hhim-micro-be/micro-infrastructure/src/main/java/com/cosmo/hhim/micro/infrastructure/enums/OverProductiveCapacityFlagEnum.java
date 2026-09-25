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
 */

@Getter
@AllArgsConstructor
public enum OverProductiveCapacityFlagEnum implements BaseEnum<String> {

    /**
     * 是
     */
    OVER("0", "超产能"),
    /**
     * 否
     */
    NOT_OVER("1", "未超产能");

    private String code;
    private String desc;

    public static OverProductiveCapacityFlagEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        OverProductiveCapacityFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

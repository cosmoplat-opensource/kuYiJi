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
 * 启用禁用状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnableEnum implements BaseEnum<Integer> { 
    /**
     * 禁用
     */
    DISABLE(0, "DISABLE"),
    /**
     * 可用
     */
    ENABLE(1, "ENABLE");

    private Integer code;
    private String desc;

    public static EnableEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Integer code) {
        EnableEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

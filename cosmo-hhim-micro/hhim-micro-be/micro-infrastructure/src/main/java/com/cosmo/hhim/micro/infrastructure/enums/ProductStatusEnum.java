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
 * 良品/不良品枚举
 */
@Getter
@AllArgsConstructor
public enum ProductStatusEnum implements BaseEnum<String> { 
    /**
     * 不良品
     */
    NG("R", "不良品"),
    /**
     * 良品
     */
    PASS("N", "良品");

    private String code;
    private String desc;

    public static ProductStatusEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ProductStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

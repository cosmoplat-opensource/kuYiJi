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
public enum IsFirstProcessEnum implements BaseEnum<String> {

    /**
     * 是
     */
    YES("0", "是"),
    /**
     * 否
     */
    NO("1", "否");

    private String code;
    private String desc;

    public static IsFirstProcessEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        IsFirstProcessEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

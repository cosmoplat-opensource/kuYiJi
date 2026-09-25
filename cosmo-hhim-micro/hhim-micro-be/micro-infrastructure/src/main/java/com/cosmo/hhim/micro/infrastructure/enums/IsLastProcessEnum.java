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
 * 是否为最后一道工序枚举
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum IsLastProcessEnum implements BaseEnum<String> {

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

    public static IsLastProcessEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        IsLastProcessEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

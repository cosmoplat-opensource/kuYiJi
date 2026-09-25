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
 * @description: 补录标示
 */
@Getter
@AllArgsConstructor
public enum ExpiredRecordFlagEnum implements BaseEnum<Long> {

    /**
     * 是
     */
    YES(0L, "是"),
    /**
     * 否
     */
    NO(1L, "否");

    private Long code;
    private String desc;

    public static ExpiredRecordFlagEnum getEnum(Long code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Long code) {
        ExpiredRecordFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

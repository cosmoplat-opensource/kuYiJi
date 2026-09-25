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
public enum NegativeStockFlagEnum implements BaseEnum<String> {
    /**
     * 是
     */
    NEGATIVE_STOCK("0", "负库存"),
    /**
     * 否
     */
    POSITIVE_STOCK("1", "正库存");

    private String code;
    private String desc;

    public static NegativeStockFlagEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        NegativeStockFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

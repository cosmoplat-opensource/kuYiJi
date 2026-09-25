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
 * 产品类型枚举
 */
@Getter
@AllArgsConstructor
public enum ProductTypeEnum implements BaseEnum<String> { 
    /**
     * 成品
     */
    CP("CP", "成品"),
    /**
     * 半成品
     */
    BCP("BCP", "半成品"),
    /**
     * 原材料
     */
    YCL("YCL", "原材料");

    private String code;
    private String desc;

    public static ProductTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ProductTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

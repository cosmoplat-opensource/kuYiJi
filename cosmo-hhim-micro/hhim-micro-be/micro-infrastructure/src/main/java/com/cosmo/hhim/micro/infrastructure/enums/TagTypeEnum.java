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
 * 标签类型枚举
 */
@Getter
@AllArgsConstructor
public enum TagTypeEnum implements BaseEnum<String> { 
    /**
     * 库存变动
     */
    STORAGE_CHANGE("STORAGE_CHANGE", "库存变动");
    private String code;
    private String desc;

    public static TagTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        TagTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

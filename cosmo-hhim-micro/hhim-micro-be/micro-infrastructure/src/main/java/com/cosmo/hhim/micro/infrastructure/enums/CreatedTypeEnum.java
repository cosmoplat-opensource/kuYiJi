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
 * 自动或手动创建类型枚举
 */
@Getter
@AllArgsConstructor
public enum CreatedTypeEnum implements BaseEnum<String> { 
    /**
     * 自动
     */
    AUTO("0", "AUTO"),
    /**
     * 手动
     */
    MANUAL("1", "MANUAL");

    private String code;
    private String desc;

    public static CreatedTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        CreatedTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

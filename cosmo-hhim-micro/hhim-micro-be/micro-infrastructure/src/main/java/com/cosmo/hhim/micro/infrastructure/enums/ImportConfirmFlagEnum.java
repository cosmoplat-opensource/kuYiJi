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
 * @createTime 2023/4/19
 */
@Getter
@AllArgsConstructor
public enum ImportConfirmFlagEnum implements BaseEnum<String> {

    /**
     * 错误数据修复
     */
    ERROR_DATA_FIX("0", "错误数据修复"),
    /**
     * 确认导入
     */
    IMPORTED("1", "确认导入");

    private String code;
    private String desc;

    public static ImportConfirmFlagEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ImportConfirmFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

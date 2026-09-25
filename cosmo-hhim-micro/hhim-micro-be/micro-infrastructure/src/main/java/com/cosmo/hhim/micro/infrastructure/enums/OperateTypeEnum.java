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
 * @createTime 2022-11-02
 */
@Getter
@AllArgsConstructor
public enum OperateTypeEnum { 
    SUBMIT("10", "报工"),
    CHECK("20", "审产");

    private String code;
    private String desc;

    public static OperateTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        OperateTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

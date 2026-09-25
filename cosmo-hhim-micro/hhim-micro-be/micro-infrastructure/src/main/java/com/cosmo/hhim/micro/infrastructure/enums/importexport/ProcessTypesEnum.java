/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.importexport;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/16
 */
@Getter
@AllArgsConstructor
public enum ProcessTypesEnum implements BaseEnum<String> {

    /**
     * 首序
     */
    FIRST_PROCESS("10", "首"),

    /**
     * 尾序
     */
    LAST_PROCESS("20", "尾");

    private String code;
    private String desc;

    public static ProcessTypesEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ProcessTypesEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}


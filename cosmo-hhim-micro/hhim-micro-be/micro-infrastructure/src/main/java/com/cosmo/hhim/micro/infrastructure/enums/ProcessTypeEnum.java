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
 * @descirption: 工序类型(首序还是尾序)
 */

@Getter
@AllArgsConstructor
public enum ProcessTypeEnum implements BaseEnum<String> {

    /**
     * 首序
     */
    FIRST_PROCESS("0", "首序"),

    /**
     * 尾序
     */
    LAST_PROCESS("1", "尾序"),

    /**
     * 既是首序又是尾序
     */
    FIRST_AND_LAST_PROCESS("2", "既是首序又是尾序");

    private String code;
    private String desc;

    public static ProcessTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ProcessTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

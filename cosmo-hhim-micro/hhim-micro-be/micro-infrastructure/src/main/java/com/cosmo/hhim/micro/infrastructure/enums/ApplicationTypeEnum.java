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
public enum ApplicationTypeEnum implements BaseEnum<String> { 
    KU_YI_JI("micro_process", "Ku易记"),
    GONG_YI_PAI("micro_plan", "工易派");

    private String code;
    private String desc;

    public static ApplicationTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ApplicationTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

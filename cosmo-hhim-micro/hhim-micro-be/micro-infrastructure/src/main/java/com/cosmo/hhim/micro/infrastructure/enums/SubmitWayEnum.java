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
 * @description: 涉及批量报工的枚举
 */

@Getter
@AllArgsConstructor
public enum SubmitWayEnum implements BaseEnum<Long> { 

    BATCH_SUBMIT(1L, "批量报工"),
    BATCH_SUBMIT_AND_SEND_FOR_QC(2L, "批量送检");

    private Long code;
    private String desc;

    public static SubmitWayEnum getEnum(Long code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Long code) {
        SubmitWayEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

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
 * 报工类型
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum SubmitTypeEnum implements BaseEnum<Long> {
    /**
     * Ku易记
     */
    KU_YI_JI_SUBMIT(1L, "Ku易记，工序报工"),

    /**
     * 工易派
     */
    GONG_YI_PAI_SUBMIT(2L, "工易派，工单报工");

    private Long code;
    private String desc;

    public static SubmitTypeEnum getEnum(Long code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Long code) {
        SubmitTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

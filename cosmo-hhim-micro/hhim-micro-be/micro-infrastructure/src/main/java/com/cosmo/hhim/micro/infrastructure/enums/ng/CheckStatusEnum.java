/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.ng;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 */

@Getter
@AllArgsConstructor
public enum CheckStatusEnum implements BaseEnum<Long> {

    /**
     * 未送检
     */
    NOT_SUBMIT_FOR_INSPECTION(0L, "未送检"),

    /**
     * 已送检, 待质检
     */
    ALREADY_SUBMIT_FOR_INSPECTION(1L, "待质检"),

    /**
     * 质检完成
     */
    FINISHED_INSPECTION(2L, "质检完成");

    private Long code;
    private String desc;

    public static CheckStatusEnum getEnum(Long code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Long code) {
        CheckStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

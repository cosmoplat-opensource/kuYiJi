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
 * 报工记录是否变更枚举 (审核的时候才判断是否变更)
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum DataStatusEnum implements BaseEnum<Long> {
    /**
     * 变更
     */
    CHANGED(0L, "变更"),
    /**
     * 未变更
     */
    UN_CHANGED(1L, "未变更");

    private Long code;
    private String desc;

    public static DataStatusEnum getEnum(Long code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Long code) {
        DataStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

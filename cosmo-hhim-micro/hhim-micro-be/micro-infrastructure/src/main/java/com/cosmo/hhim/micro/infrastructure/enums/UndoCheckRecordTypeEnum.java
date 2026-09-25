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
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum UndoCheckRecordTypeEnum implements BaseEnum<String> {

    /**
     * 正常撤销
     */
    NORMAL_UNDO("0", "正常撤销"),

    /**
     * 质检撤销
     */
    QC_UNDO("1", "质检撤销");

    private String code;
    private String desc;

    public static UndoCheckRecordTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        UndoCheckRecordTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

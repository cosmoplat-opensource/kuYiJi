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
 * @createTime 2023/4/15
 */
@Getter
@AllArgsConstructor
public enum AsyncImportExportTaskStatusEnum implements BaseEnum<Integer>{ 

    CONFIRMED(4, "已确认"),
    CANCELED(5, "已取消");

    private Integer code;
    private String desc;

    public static AsyncImportExportTaskStatusEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Integer code) {
        AsyncImportExportTaskStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

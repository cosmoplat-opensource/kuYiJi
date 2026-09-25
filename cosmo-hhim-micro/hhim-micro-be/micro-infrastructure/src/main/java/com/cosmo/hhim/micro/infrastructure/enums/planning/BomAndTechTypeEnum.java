/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.planning;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @descirption: Bom与工艺类型枚举
 */

@Getter
@AllArgsConstructor
public enum BomAndTechTypeEnum implements BaseEnum<String> {

    /**
     * 标准
     */
    STANDARD("STANDARD", "标准"),

    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿");

    private String code;

    private String desc;

    public static BomAndTechTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        BomAndTechTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

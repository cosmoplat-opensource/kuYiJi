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
 * @createTime 2023/5/23
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum implements BaseEnum<String>{ 
    OFFICIAL_USER("10", "正式用户"),
    TRIAL_USER("20", "正式用户");

    private String code;
    private String desc;

    public static UserTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        UserTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

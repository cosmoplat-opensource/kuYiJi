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
 * @createTime 2022-11-02
 */
@Getter
@AllArgsConstructor
public enum GuideGroupConfigEnum implements BaseEnum<String> { 
    QUESTION_GROUP("10000", "问卷调研引导组"),
    NORMAL_GROUP("20000", "常规引导组"),
    INVITE_GROUP("30000", "邀请引导组"),
    ;

    private String code;
    private String desc;

    public static GuideGroupConfigEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        GuideGroupConfigEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

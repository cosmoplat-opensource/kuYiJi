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
public enum GuideStatusFlagEnum implements BaseEnum<String> { 
    NO_AUTH("10", "无权体验"),
    QUESTION_GUIDE_UNDONE("20", "调研问卷引导未完成"),
    NORMAL_GUIDE_UNDONE("30", "常规引导未完成"),
    INVITE_GUIDE_UNDONE("40", "邀请引导未完成"),
    GUIDE_DONE("50", "引导已完成"),
    OFFICIAL("60", "正式使用"),
    ;

    private String code;
    private String desc;

    public static GuideStatusFlagEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        GuideStatusFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

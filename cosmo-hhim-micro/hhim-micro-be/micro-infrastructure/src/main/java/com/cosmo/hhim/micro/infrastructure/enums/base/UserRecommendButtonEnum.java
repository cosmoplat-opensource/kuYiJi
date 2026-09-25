/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.base;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;


/**
 * 用户个性化推荐按钮枚举
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum UserRecommendButtonEnum implements BaseEnum<String> {
    /**
     * 记工
     */
    SUBMIT("SUBMIT", "记工"),
    /**
     * 质检
     */
    QC("QC", "送检");

    private final String code;
    private final String desc;

    public static UserRecommendButtonEnum getEnum(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code.toUpperCase())).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        UserRecommendButtonEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

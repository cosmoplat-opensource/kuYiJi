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
 * 用户个性化推荐页面枚举
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum UserRecommendPageEnum implements BaseEnum<String> {
    /**
     * 默认的首页
     */
    INDEX("INDEX", "默认的首页"),
    /**
     * 审产维度
     */
    CHECK_DIMENSIONS("CHECK_DIMENSIONS", "审产维度页");

    private final String code;
    private final String desc;

    public static UserRecommendPageEnum getEnum(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code.toUpperCase())).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        UserRecommendPageEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

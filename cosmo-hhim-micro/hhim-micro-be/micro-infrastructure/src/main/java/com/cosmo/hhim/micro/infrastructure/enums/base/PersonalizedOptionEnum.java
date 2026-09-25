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
 * 个性化推荐选项
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum PersonalizedOptionEnum implements BaseEnum<Integer> {
    /**
     * 关闭
     */
    DISABLE(0, "关闭"),
    /**
     * 开启
     */
    ENABLE(1, "开启"),
    /**
     * 展示个性化推荐弹窗
     */
    SHOW_MODAL(2, "展示个性化推荐弹窗"),
    /**
     * 不展示开始个性化推荐弹窗
     */
    DISPLAY(3, "不展示开始个性化推荐弹窗");


    private final Integer code;
    private final String desc;

    public static PersonalizedOptionEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Integer code) {
        PersonalizedOptionEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

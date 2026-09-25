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
 * @desc 提示方式-供前端显示标识（10:状态栏显示，20:弹窗显示）
 */
@Getter
@AllArgsConstructor
public enum TipWayEnum implements BaseEnum<String>{ 
    STATUS_BAR_SHOW("10", "状态栏显示"),
    POPUP_WINDOW_SHOW("20", "弹窗显示");

    private String code;
    private String desc;

    public static TipWayEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        TipWayEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}
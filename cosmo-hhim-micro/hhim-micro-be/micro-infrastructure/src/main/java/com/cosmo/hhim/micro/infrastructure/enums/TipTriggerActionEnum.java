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
 * @createTime 2023-02-02
 */
@Getter
@AllArgsConstructor
public enum TipTriggerActionEnum implements BaseEnum<String> { 

    ENTER_PROGRAM("enterProgram", "进入小程序"),
    SUBMIT_FINISH("submit", "提交"),
    SWITCH_PAGE("switchPage", "切换页面");

    private String code;
    private String desc;

    public static TipTriggerActionEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        TipTriggerActionEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }

}

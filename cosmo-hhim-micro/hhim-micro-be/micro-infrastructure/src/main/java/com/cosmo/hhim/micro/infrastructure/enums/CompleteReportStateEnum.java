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
 */
@Getter
@AllArgsConstructor
public enum CompleteReportStateEnum implements BaseEnum<String> {

    /**
     * 正常
     */
    NORMAL("0", "正常"),
    /**
     * 撤销
     */
    CANCEL("1", "撤销"),
    /**
     * 已结算
     */
    SETTLED("2", "已结算");

    private String code;
    private String desc;

    public static CompleteReportStateEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        CompleteReportStateEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

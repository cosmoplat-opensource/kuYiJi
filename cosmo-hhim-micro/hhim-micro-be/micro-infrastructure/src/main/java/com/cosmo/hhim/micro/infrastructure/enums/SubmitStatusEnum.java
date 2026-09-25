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
 * 报工审核状态枚举
 */
@Getter
@AllArgsConstructor
public enum SubmitStatusEnum implements BaseEnum<Long> { 
    /**
     * 审核通过
     */
    APPROVED(0L, "审核通过"),

    /**
     * 待审核
     */
    UN_APPROVE(1L, "待审核"),

    /**
     * 审核驳回
     */
    REJECT(2L, "审核驳回"),

    /**
     * 已完工
     */
    COMPLETE(3L, "已完工");

    private Long code;
    private String desc;

    public static SubmitStatusEnum getEnum(Long code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Long code) {
        SubmitStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

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
 * @createTime 2022-11-01
 */
@Getter
@AllArgsConstructor
public enum ToDoPushStatusEnum implements BaseEnum<String> {
    /**
     * 完成
     */
    DONE("0", "完成"),
    /**
     * 激活
     */
    TODO("1", "激活"),
    /**
     * 撤回
     */
    RECALL("2", "撤回"),
    /**
     * 拒绝
     */
    REJECT("3", "拒绝");

    private String code;
    private String desc;

    public static ToDoPushStatusEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ToDoPushStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

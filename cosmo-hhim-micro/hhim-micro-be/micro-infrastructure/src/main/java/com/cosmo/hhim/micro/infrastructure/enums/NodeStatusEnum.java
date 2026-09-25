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
 * @createTime 2022-11-01
 */
@Getter
@AllArgsConstructor
public enum NodeStatusEnum implements BaseEnum<String> { 
    UNDONE("0", "未完成"),
    DONE("1", "已完成");

    private String code;
    private String desc;

    public static NodeStatusEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        NodeStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

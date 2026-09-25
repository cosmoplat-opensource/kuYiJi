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
 * 关注类型枚举
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum FollowTypeEnum implements BaseEnum<String> {
    /**
     * 员工
     */
    EMPLOYEE("EMPLOYEE", "员工"),
    PRODUCT("PRODUCT", "产品"),
    PROCESS("PROCESS", "工序");

    private String code;
    private String desc;

    public static FollowTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        FollowTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

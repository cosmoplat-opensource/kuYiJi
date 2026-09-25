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
 * 数据治理类型枚举
 */
@Getter
@AllArgsConstructor
public enum DataWarnTypeEnum implements BaseEnum<String> { 
    /**
     * 工序主数据
     */
    MASTER_PROCESS("MASTER_PROCESS", "工序主数据"),
    /**
     * 产品主数据
     */
    MASTER_PRODUCT("MASTER_PRODUCT", "产品主数据"),
    /**
     * 良品率
     */
    PASS_RATE("PASS_RATE", "良品率"),
    /**
     * 平均产能
     */
    AVG_PRODUCTION_CAPACITY("AVG_PRODUCTION_CAPACITY", "平均产能");

    private String code;
    private String desc;

    public static DataWarnTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        DataWarnTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

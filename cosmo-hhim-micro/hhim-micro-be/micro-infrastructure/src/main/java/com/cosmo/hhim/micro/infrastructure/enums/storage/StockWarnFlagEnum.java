/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.storage;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @description: 库存预警枚举
 * @date 2023/5/6 11:25
 */
@AllArgsConstructor
@Getter
public enum StockWarnFlagEnum implements BaseEnum<String> {

    /**
     * 负库存风险标示
     */
    NEGATIVE_STOCK("1", "负库存风险标示"),

    /**
     * 低于安全库存
     */
    LOWER_SAFETY_STOCK("2", "低于安全库存"),

    /**
     * 库存超高
     */
    UPPER_SAFETY_STOCK("3", "库存超高");

    private String code;
    private String desc;

    public static StockWarnFlagEnum getEnum(String  code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        StockWarnFlagEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

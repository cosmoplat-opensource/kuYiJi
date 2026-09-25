/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.planning;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 工艺形式枚举类
 *
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum TechPatternEnum implements BaseEnum<Integer> {

    /**
     * 顺序
     */
    SERIAL(10, "顺序"),

    /**
     * 乱序
     */
    UN_ORDER(20, "乱序"),
    /**
     * 预研推荐
     */
    RECOMMEND(30, "预研推荐"),
    /**
     * 工单下发草稿
     */
    ISSUED_ORDER(35, "工单下发草稿"),
    /**
     * 期初导入
     */
    INITIAL(40, "期初导入");

    private Integer code;

    private String desc;

    public static TechPatternEnum getEnum(Integer code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(Integer code) {
        TechPatternEnum e = getEnum(code);
        return e != null ? e.getDesc() : null;
    }
}

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
 * @descirption: 工单状态枚举
 */

@Getter
@AllArgsConstructor
public enum WorkOrderStatusEnum implements BaseEnum<String> {

    /**
     * 待生产
     */
    TO_BE_PRODCED("10", "待生产"),

    /**
     * 生产中
     */
    IN_PRODUCTION("20", "生产中"),


    /**
     * 已完成
     */
    FINISHED("30", "已完成"),

    /**
     * 关闭
     */
    CLOSED("40", "关闭");


    private String code;

    private String desc;

    public static WorkOrderStatusEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        WorkOrderStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

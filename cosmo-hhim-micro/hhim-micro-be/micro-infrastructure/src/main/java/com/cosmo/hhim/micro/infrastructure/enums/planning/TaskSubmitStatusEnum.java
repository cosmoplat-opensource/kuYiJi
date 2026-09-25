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
 * @descirption: 生产任务状态枚举
 */

@Getter
@AllArgsConstructor
public enum TaskSubmitStatusEnum implements BaseEnum<String> {

    /**
     * 待报工
     */
    TO_BE_REPORTED("00", "待报工"),

    /**
     * 报工中
     */
    WORK_REPORTING("10", "报工中"),


    /**
     * 报工完成
     */
    FINISHED("20", "报工完成"),

    /**
     * 关闭
     */
    CLOSED("30", "关闭");


    private String code;

    private String desc;

    public static TaskSubmitStatusEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        TaskSubmitStatusEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

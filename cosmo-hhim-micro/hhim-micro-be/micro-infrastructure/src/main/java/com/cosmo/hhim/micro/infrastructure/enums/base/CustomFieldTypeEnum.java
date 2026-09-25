/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.base;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @description: 自定义字段类型枚举
 * @classname: CustomFieldTypeEnum
 * @date: 2023/3/24 14:19
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CustomFieldTypeEnum implements BaseEnum<String> {
    /**
     * 字符串
     */
    CUSTOM_FIELD_TYPE_STRING("1", "字符串"),

    /**
     * 数字
     */
    CUSTOM_FIELD_TYPE_NUMBER("2", "数字"),

    /**
     * 日期
     */
    CUSTOM_FIELD_TYPE_DATETIME("3", "日期时间"),

    /**
     * 单选
     */
    CUSTOM_FIELD_TYPE_SINGLE("4", "单选"),

    /**
     * 多选
     */
    CUSTOM_FIELD_TYPE_MULTIPLY("5", "多选");


    private String code;

    private String desc;


    public static CustomFieldTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        CustomFieldTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

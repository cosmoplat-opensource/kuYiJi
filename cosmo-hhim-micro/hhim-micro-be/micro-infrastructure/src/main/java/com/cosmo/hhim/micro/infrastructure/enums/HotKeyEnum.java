/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 良品/不良品枚举
 */
@Getter
@AllArgsConstructor
public enum HotKeyEnum implements BaseEnum<String> { 
    /**
     * 前工序热点KEY
     */
    PRE_PROCESS(CommonConstants.HOT_KEY_PRE_PROCESS, "PRE_PROCESS"),
    /**
     * 操作工序热点KEY
     */
    OPERATE_PROCESS(CommonConstants.HOT_KEY_OPERATE_PROCESS, "OPERATE_PROCESS"),
    /**
     * 高频产品KEY
     */
    HIGH_FREQUENCY_PRODUCT(CommonConstants.HOT_KEY_HIGH_FREQUENCY_PRODUCT, "HIGH_FREQUENCY_PRODUCT"),
    /**
     * 高频库存变动标签
     */
    HOT_TAG_STORAGE_CHANGE(CommonConstants.HOT_TAG_STORAGE_CHANGE, "HOT_TAG_STORAGE_CHANGE");

    private String code;
    private String desc;

    public static HotKeyEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static HotKeyEnum getEnumByDesc(String desc) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.desc, desc)).findFirst().orElse(null);
    }
}

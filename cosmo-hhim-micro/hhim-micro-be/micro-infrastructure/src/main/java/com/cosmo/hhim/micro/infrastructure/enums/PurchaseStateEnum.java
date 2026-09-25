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
 * @createTime 2023-01-05
 * @desc 购买标记
 */
@Getter
@AllArgsConstructor
public enum PurchaseStateEnum implements BaseEnum<String> { 
    UN_PURCHASED("0", "未购买"),
    PURCHASED("1", "已购买");

    private String code;
    private String desc;

    public static PurchaseStateEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }
}

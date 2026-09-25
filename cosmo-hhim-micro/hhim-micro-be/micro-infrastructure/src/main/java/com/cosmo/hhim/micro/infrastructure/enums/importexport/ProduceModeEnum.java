/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.importexport;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/16
 * @desc 生产模式
 */
@Getter
@AllArgsConstructor
public enum ProduceModeEnum implements BaseEnum<String> { 
    SEQUENCE("10", "顺序生产"),
    PARALLEL("20", "并序生产");

    private String code;
    private String desc;

    public static ProduceModeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        ProduceModeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

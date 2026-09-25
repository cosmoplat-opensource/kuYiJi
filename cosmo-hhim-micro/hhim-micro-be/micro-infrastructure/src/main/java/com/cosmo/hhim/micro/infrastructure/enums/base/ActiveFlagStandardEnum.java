/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.base;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @createTime 2023/03/08
 */
@Getter
@AllArgsConstructor
public enum ActiveFlagStandardEnum implements BaseEnum<String> { 

    NORMAL("1", "正常"),
    DISABLE("0", "停用");

    private String code;
    private String desc;


}

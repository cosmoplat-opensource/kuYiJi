/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/21
 */
@Getter
@AllArgsConstructor
public enum ActiveFlagEnum implements BaseEnum<String> { 

    NORMAL("0", "正常"),
    DISABLE("1", "停用");

    private String code;
    private String desc;


}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.planning;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @createTime 2023/03/08
 */
@Getter
@AllArgsConstructor
public enum MaterialsTypeEnum implements BaseEnum<String> { 

    PICKING("0", "投料单"),
    RETURN("1", "退料单");

    private String code;
    private String desc;


}

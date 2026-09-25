/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums.base;

import com.cosmo.hhim.common.core.utils.StringUtils;

/**
 * 表单操作类型
 */

public enum FormTypeEnum { 
    ADD("新增", "A"),
    EDIT("修改", "E"),
    DELETE("删除", "D");
    private String name;
    private String code;
    FormTypeEnum(String name, String code) {
       this.name=name;
       this.code=code;
    }

    public static FormTypeEnum getFormTypeEnum(String flag) {
        if (StringUtils.isBlank(flag)) {
            return null;
        }
        for (FormTypeEnum result : FormTypeEnum.values()) {
            if (result.getCode().equals(flag)) {
                return result;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}

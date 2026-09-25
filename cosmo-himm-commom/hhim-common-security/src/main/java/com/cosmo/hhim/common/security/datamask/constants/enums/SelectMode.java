/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.constants.enums;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏操作模式枚举项
 * @createTime 2022-01-12
 */
public enum SelectMode {

    NORMAL_SELECT("0", "常规查询"),
    EDIT_SELECT("1", "编辑查询");

    SelectMode(String code, String desc){
        this.code = code;
        this.desc = desc;
    }

    private String code;
    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SelectMode parse(String code) {
        for (SelectMode modeEnum : SelectMode.values()) {
            if (modeEnum.getCode().equals(code)) {
                return modeEnum;
            }
        }
        return null;
    }

}

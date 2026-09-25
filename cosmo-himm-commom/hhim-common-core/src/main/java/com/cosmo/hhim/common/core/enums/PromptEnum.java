/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.enums;

/**
 * 系统提示枚举
 */
public enum PromptEnum {
    CIM_PROCESS_EXPORT(500, "系统异常");


    PromptEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private int code;
    private String msg;

    public static int getCode(String name){
       return PromptEnum.valueOf(name).code;
    }

    public static String getMsg(String name){
        return PromptEnum.valueOf(name).msg;
    }
}

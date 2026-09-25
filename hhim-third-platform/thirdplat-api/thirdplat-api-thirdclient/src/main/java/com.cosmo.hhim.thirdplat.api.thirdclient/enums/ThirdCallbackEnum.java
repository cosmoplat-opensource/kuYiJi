/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.enums;


public enum ThirdCallbackEnum {
    NEED_CALLBACK("1"),
    NO_NEED_CALLBACK("0");
    private final String status;

    ThirdCallbackEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static ThirdCallbackEnum getStatus(String status) {
        if (status == null) {
            return null;
        }
        for (ThirdCallbackEnum value : ThirdCallbackEnum.values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}

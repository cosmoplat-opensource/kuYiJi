/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.enums;

public enum ThirdStatusEnum {
    CODE_ERROR("-1"),
    REQUEST_ERROR("0"),
    REQUEST_SUCCESS("1");
    private final String status;

    ThirdStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static ThirdStatusEnum getStatus(String status) {
        if (status == null) {
            return null;
        }
        for (ThirdStatusEnum value : ThirdStatusEnum.values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}

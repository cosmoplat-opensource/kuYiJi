/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.enums;


public enum ThirdClientEnum {
    YONYOU_U8("yonyouU8"),
    THIRD_COMMON_CLIENT("thirdCommonClient");
    private final String strategy;

    ThirdClientEnum(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    public static ThirdClientEnum getStrategy(String strategy) {
        if (strategy == null) {
            return null;
        }
        for (ThirdClientEnum value : ThirdClientEnum.values()) {
            if (value.getStrategy().equals(strategy)) {
                return value;
            }
        }
        return null;
    }
}

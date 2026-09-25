/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.enums;

public enum ThirdFlushEnum {
    HYZZ_COMMON_MYSQL("hyzz_common_mysql");
    private final String strategy;

    ThirdFlushEnum(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    public static ThirdFlushEnum getStrategy(String strategy) {
        if (strategy == null) {
            return null;
        }
        for (ThirdFlushEnum value : ThirdFlushEnum.values()) {
            if (value.getStrategy().equals(strategy)) {
                return value;
            }
        }
        return HYZZ_COMMON_MYSQL;
    }
}

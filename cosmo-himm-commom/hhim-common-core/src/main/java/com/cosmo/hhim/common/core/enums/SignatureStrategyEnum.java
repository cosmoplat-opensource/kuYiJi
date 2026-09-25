/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.enums;

import org.springframework.util.StringUtils;

public enum SignatureStrategyEnum {
    RSA("RSA2"),
    MD5("MD5"),
    DES("DES"),
    AES("AES");
    private final String strategy;

    SignatureStrategyEnum(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    public static SignatureStrategyEnum getStrategy(String strategy) {
        if (StringUtils.isEmpty(strategy)) {
            return null;
        }
        for (SignatureStrategyEnum value : SignatureStrategyEnum.values()) {
            if (value.getStrategy().equals(strategy)) {
                return value;
            }
        }
        return null;
    }
}
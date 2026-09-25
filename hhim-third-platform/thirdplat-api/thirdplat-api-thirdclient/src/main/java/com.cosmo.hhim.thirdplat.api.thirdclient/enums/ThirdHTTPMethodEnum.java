/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.enums;

import java.util.Arrays;
import java.util.Objects;

public enum ThirdHTTPMethodEnum {
    /**
     * http GET
     */
    GET,
    /**
     * http POST
     */
    POST,
    /**
     * http PUT
     */
    PUT,
    /**
     * http HEAD
     */
    HEAD,
    /**
     * http DELETE
     */
    DELETE;

    ThirdHTTPMethodEnum() {
    }

    public String value() {
        return this.name();
    }

    public static ThirdHTTPMethodEnum fromValue(String v) {
        return valueOf(v.toUpperCase());
    }

    public static ThirdHTTPMethodEnum convertEnum(String v) {
        return Arrays.stream(values()).filter(a -> Objects.equals(a.value(), v)).findFirst().orElse(null);
    }
}
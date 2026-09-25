/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @description 微应用标识枚举（应用 sign 与展示名称映射）
 * @createTime 2022-03-08
 */
public enum MqtagEnum {

    /**
     * 登录设备类型
     */
    MICRO_PROCESS("micro_process", "KU易记"),
    MICRO_PLAN("micro_plan", "工易派");

    private String key;
    private String desc;

    MqtagEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public static MqtagEnum getEnum(String key) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.key, key)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String key) {
        MqtagEnum e = getEnum(key);
        return e != null ? e.desc : null;
    }


}

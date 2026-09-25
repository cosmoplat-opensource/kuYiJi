/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum FinishStorageChangeTypeEnum implements BaseEnum<String> {

    /**
     * 完工入库
     */
    FINISH_INBOUND("10", "完工入库"),

    /**
     * 完工撤销
     */
    FINISH_CANCEL("20", "完工撤销"),

    /**
     * 库存变动
     */
    STORAGE_CHANGE("30", "库存变动"),

    /**
     * 手动出库
     */
    STORAGE_OUTBOUND("40", "手动出库"),

    /**
     * 库存导入
     */
    STORAGE_IMPORTED("50", "库存导入"),

    /**
     * 手动入库
     */
    STORAGE_INBOUND("60", "手动入库"),

    /**
     * 生产投料
     */
    PRODUCTION_FEEDING("70", "生产投料"),

    /**
     * 生产退料
     */
    PRODUCTION_RETURN("80", "生产退料");

    private String code;
    private String desc;

    public static FinishStorageChangeTypeEnum getEnum(String code) {
        return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
    }

    public static String getEnumDesc(String code) {
        FinishStorageChangeTypeEnum e = getEnum(code);
        return e != null ? e.desc : null;
    }
}

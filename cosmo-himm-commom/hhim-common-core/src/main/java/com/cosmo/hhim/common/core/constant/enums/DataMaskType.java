/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant.enums;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏类型
 * @createTime 2022-01-11
 */
public enum DataMaskType {
    SALE_PRICE("1", "销售价"),
    PURCHASE_PRICE("2", "采购价"),
    COST_PRICE("3", "成本价");

    DataMaskType(String code, String desc){
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

    public static DataMaskType parse(String code) {
        for (DataMaskType modeEnum : DataMaskType.values()) {
            if (modeEnum.getCode().equals(code)) {
                return modeEnum;
            }
        }
        return null;
    }
}

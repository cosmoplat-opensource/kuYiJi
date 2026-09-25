/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.enums;

import com.cosmo.hhim.common.core.utils.CheckObjectUtils;

/**
 * 条码序列化枚举
 *
 * @author cosmo-hhim-open Team
 */
public class BarcodeSerialize {
    public enum Sort {
        W("W", "物料编码", "matCode"),
        P("P", "批次号", "batchNo"),
        S("S", "供应商编码", "supplierCode"),
        K("K", "客编码户", "customerCode"),
        C("C", "规格", "specifications");
        //A(" A", "生产批次号", "");

        private final String code;
        private final String info;
        private final String column;

        Sort(String code, String info, String column) {
            this.code = code;
            this.info = info;
            this.column = column;
        }

        public String getCode() {
            return code;
        }

        public String getInfo() {
            return info;
        }

        public String getColumn() {
            return column;
        }

        //根据编码查询查注释
        public static Sort valueOfCode(String code) {
            if (CheckObjectUtils.isEmpty(code)) {
                return null;
            }
            for (Sort value : Sort.values()) {
                if ((value.getCode() + "").equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    public enum epPlag {
        N(0, "不加密"),
        P(1, "加密");

        private final int code;
        private final String info;

        epPlag(int code, String info) {
            this.code = code;
            this.info = info;
        }

        public int getCode() {
            return code;
        }

        public String getInfo() {
            return info;
        }
    }


}

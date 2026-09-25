/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

import com.cosmo.hhim.common.core.converter.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/9
 */
public class ActionTypeEnums {

    /**
     * 切换页面动作类型
     */
    @Getter
    @AllArgsConstructor
    public enum SwitchPageActionEnum implements BasicEnum<String> { 
        WORK_SUBMIT_LIST("10", "记工列表"),
        WORK_CHECK_LIST("20", "审核列表"),
        STORAGE_LIST("30", "库存列表"),
        WORKBENCH_LIST("40", "工作台列表");

        private String code;
        private String desc;

        public static SwitchPageActionEnum getEnum(String code) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
        }

        public static String getEnumDesc(String code) {
            SwitchPageActionEnum e = getEnum(code);
            return e != null ? e.desc : null;
        }
    }

    /**
     * 提交动作类型
     */
    @Getter
    @AllArgsConstructor
    public enum SubmitActionEnum implements BasicEnum<String> { 
        WORK_SUBMIT("10", "记工提交"),
        WORK_CHECK_SUBMIT("20", "审核提交");

        private String code;
        private String desc;

        public static SubmitActionEnum getEnum(String code) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.code, code)).findFirst().orElse(null);
        }

        public static String getEnumDesc(String code) {
            SubmitActionEnum e = getEnum(code);
            return e != null ? e.desc : null;
        }
    }

}

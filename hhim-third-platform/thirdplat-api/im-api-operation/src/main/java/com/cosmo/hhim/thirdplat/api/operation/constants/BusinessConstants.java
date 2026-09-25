/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.constants;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
public class BusinessConstants {

    /**
     * 账号类型
     */
    public enum AccountTypeEnum {
        MANAGER("1", "企业管理员"),
        NORMAL_USER("2", "普通账号");

        AccountTypeEnum(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        private String key;
        private String desc;

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 产品类型
     */
    public enum ProductTypeEnum {
        MOM(0, "mom"),
        MICRO_APPLICATION(1, "微应用");

        ProductTypeEnum(Integer key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        private Integer key;
        private String desc;

        public Integer getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 应用使用状态
     */
    public enum ApplicationUseStatusEnum {
        TRIAL("0", "试用"),
        OFFICIAL_USE("1", "正式使用");

        ApplicationUseStatusEnum(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        private String key;
        private String desc;

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }

        public static ApplicationUseStatusEnum parseEnum(String key) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.key, key)).findFirst().orElse(null);
        }
    }

    /**
     * 用户认证状态
     */
    public enum UserAuthenticationStatusEnum {
        UNAUTHORIZED(0, "未认证"),
        AUTHENTICATED(1, "已认证");

        UserAuthenticationStatusEnum(Integer key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        private Integer key;
        private String desc;

        public Integer getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }

        public static UserAuthenticationStatusEnum parseEnum(Integer key) {
            return Arrays.stream(values()).filter(b -> Objects.equals(b.key, key)).findFirst().orElse(null);
        }
    }


    /**
     * 账号类型
     */
    public enum FaqIndexSourceEnum {
        FAQ("faq", "来源问答"),
        OTHER("other", "其他来源");

        FaqIndexSourceEnum(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        private final String key;
        private final String desc;

        public String getKey() {
            return key;
        }

        public String getDesc() {
            return desc;
        }
    }

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.constants;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-10-27
 */
public class RedisKeys {

    //==================运营平台模块相关redis key============================
    public static final String CUSTOMER_ALL_REDIS_KEY = "allCustomer";
    public static final String CUSTOMER_TO_USER_REDIS_KEY = "userToCustomer";
    public static final String CUSTOMER_TO_CUSTOMERCODE_REDIS_KEY = "codeToCustomer";
    public static final String CUSTOMER_TO_CUBAID_REDIS_KEY = "cubaIdToCustomer";

    public static final String HELP_ALL_GROUP_REDIS_KEY = "allGroupHelp";
    public static final String HELP_TO_APPCODES_REDIS_KEY = "appcodeToHelp";
    public static final String HELP_TO_ID_REDIS_KEY = "idToHelp";
    public static final String APK_VERSION_TO_TYPE_REDIS_KEY = "typeToApkVersion";


    //==================消息推送模块相关redis key============================
    public static final String DEVICE_BIND_USER_KEY = "deviceBindUser";

    /**
     * 运营平台相关
     */
    public enum Operation {
        // 客户模块相关
        CUSTOMER("third:operation:customer:"),
        // 基础模块相关
        BASIC("third:operation:basic:");

        private String prefix;

        Operation(String prefix) {
            this.prefix = prefix;

        }

        public String value(String value) {
            return this.prefix + value;
        }
    }

    /**
     * 消息推送相关
     */
    public enum Unipush {
        // 消息推送管理模块
        MANAGER("third:unipush:manager:");

        private String prefix;

        Unipush(String prefix) {
            this.prefix = prefix;

        }

        public String value(String value) {
            return this.prefix + value;
        }
    }

    /**
     * 微信公众号相关
     */
    public enum Wechat {
        // 微信公众号管理模块
        MP("third:wechat:mp:");

        private String prefix;

        Wechat(String prefix) {
            this.prefix = prefix;

        }

        public String value(String value) {
            return this.prefix + value;
        }
    }

    /**
     * 卡奥斯内部基础支持相关
     */
    public enum CosmoSupport {
        // 卡奥斯内部支持短信管理模块
        SMS("third:cosmosupport:sms:");

        private String prefix;

        CosmoSupport(String prefix) {
            this.prefix = prefix;

        }

        public String value(String value) {
            return this.prefix + value;
        }
    }
}

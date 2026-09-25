/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.constants;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
public class WechatMpConstant {

    public static final String WXMP_BASE_URL = "wxmpBaseUrl";
    public static final String WXMP_WEBPAGE_URL = "wxmpWebPageUrl";
    public static final String WXMP_ENCODING = "wxmpEncoding";
    public static final String WXMP_CONTENTTYPE = "wxmpContentType";


    public enum Lang{
        ZH_CN("zh_CN", "简体"),zh_TW("zh_TW", "繁体"), EN("en", "英语");

        Lang(String key, String desc){
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

}

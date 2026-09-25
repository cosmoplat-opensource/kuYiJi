/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.constants;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
public enum WebPageScopeEnum {
    SNSAPI_BASE("snsapi_base", "基础作用域"),
    SNSAPI_USERINFO("snsapi_userinfo", "用户信息作用域");

    WebPageScopeEnum(String key, String desc){
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

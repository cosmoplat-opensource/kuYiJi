/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant;

/**
 * 扫码操作
 * @author cosmo-hhim-open Team
 */

public enum QRStatusEnum {
    JUMP_MENU("跳转菜单", "01"),
    PC_LOGIN("PC登录", "02"),
    TV_LOGIN("TV登录", "03"),
    ID_VERIFY("身份验证", "04"),
    PAD_LOGIN("PAD登录", "05");

    private String name;
    private String code;
    QRStatusEnum(String name, String code) {
       this.name=name;
       this.code=code;
    }

    public String getName() {
        return name;
    }



    public String getCode() {
        return code;
    }


}

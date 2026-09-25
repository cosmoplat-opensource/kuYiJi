/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.enums;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/24
 */
public enum WechatErrorEnums {

    REFUSE_TO_ACCEPT(43101, "用户拒绝接受消息或用户未订阅消息！");

    private Integer errorCode;
    private String errorMsg;

    WechatErrorEnums(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }


}

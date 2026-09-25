/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.constants;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
public class WechatMiniAppConstants {

    public enum SendMessageResultEnum {

        SUCCESS(10, "推送消息成功！"),
        FAIL(20, "推送消息失败！"),
        REFUSE_TO_ACCEPT(30, "用户拒绝接受消息或用户未订阅消息！");

        private Integer errorCode;
        private String errorMsg;

        SendMessageResultEnum(Integer errorCode, String errorMsg) {
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

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.exception;

import com.alibaba.fastjson.JSONObject;

public class OpenAPIException extends Exception {
    private String errcode;
    private String errmsg;

    public OpenAPIException(JSONObject jo) {
        super("\n error:" + jo.getString("errmsg") + " error_code:" + jo.getString("errcode"));
        this.errcode = jo.getString("errcode");
        this.errmsg = jo.getString("errmsg");
    }

    public OpenAPIException(String msg, Exception cause) {
        super(msg, cause);
    }

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

}

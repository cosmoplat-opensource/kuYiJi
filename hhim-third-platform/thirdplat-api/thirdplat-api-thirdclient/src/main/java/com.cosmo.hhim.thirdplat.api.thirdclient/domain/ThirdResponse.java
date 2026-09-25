/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ThirdResponse implements Serializable {
    public static final String SUCCESS_STATUS = "S";
    public static final String FAIL_STATUS = "F";
    /**
     * 响应码
     */
    private String code;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 响应数据
     */
    private String data;
    /**
     * 请求ID
     */
    @JSONField(name = "request_id")
    @JsonProperty("request_id")
    private String requestId;

    public ThirdResponse() {
    }


    public ThirdResponse(String code, String msg, String data, String requestId) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.requestId = requestId;
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static ThirdResponse success(String msg, String data, String requestId) {
        return new ThirdResponse(SUCCESS_STATUS, msg, data, requestId);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static ThirdResponse error(String msg, String data, String requestId) {
        return new ThirdResponse(FAIL_STATUS, msg, data, requestId);
    }
}

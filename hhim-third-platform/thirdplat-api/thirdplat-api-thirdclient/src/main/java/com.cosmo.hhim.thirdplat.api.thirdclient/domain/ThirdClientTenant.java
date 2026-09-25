/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class ThirdClientTenant implements Serializable {
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 请求路径
     */
    private String requestUrl;
    /**
     * 渠道平台
     */
    private String clientSupport;
    /**
     * 是否需要回调
     */
    private String callback;
    /**
     * 回调topic
     */
    private String callbackTopic;
    /**
     * 回调tag
     */
    private String callbackTag;

    /**
     * 请求方式
     */
    private String requestType;
    /**
     * 鉴权信息
     */
    private JSONObject authInfo;

    public ThirdClientTenant() {
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getClientSupport() {
        return clientSupport;
    }

    public void setClientSupport(String clientSupport) {
        this.clientSupport = clientSupport;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getCallbackTopic() {
        return callbackTopic;
    }

    public void setCallbackTopic(String callbackTopic) {
        this.callbackTopic = callbackTopic;
    }

    public String getCallbackTag() {
        return callbackTag;
    }

    public void setCallbackTag(String callbackTag) {
        this.callbackTag = callbackTag;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }


    public JSONObject getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(JSONObject authInfo) {
        this.authInfo = authInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThirdClientTenant that = (ThirdClientTenant) o;
        return Objects.equals(tenantCode, that.tenantCode) && Objects.equals(requestUrl, that.requestUrl) && Objects.equals(clientSupport, that.clientSupport) && Objects.equals(callback, that.callback) && Objects.equals(callbackTopic, that.callbackTopic) && Objects.equals(callbackTag, that.callbackTag) && Objects.equals(requestType, that.requestType) && Objects.equals(authInfo, that.authInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantCode, requestUrl, clientSupport, callback, callbackTopic, callbackTag, requestType, authInfo);
    }

    @Override
    public String toString() {
        return "ThirdClientTenant{" +
                "tenantCode='" + tenantCode + '\'' +
                ", requestUrl='" + requestUrl + '\'' +
                ", clientSupport='" + clientSupport + '\'' +
                ", callback='" + callback + '\'' +
                ", callbackTopic='" + callbackTopic + '\'' +
                ", callbackTag='" + callbackTag + '\'' +
                ", requestType='" + requestType + '\'' +
                ", authInfo=" + authInfo +
                '}';
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdHTTPMethodEnum;

import java.io.Serializable;
import java.util.Objects;

public class ThirdClientRequest implements Serializable {
    private String requestUrl;
    private ThirdHTTPMethodEnum requestType;
    private String mediaType;
    private String charset;
    private String requestBody;

    public ThirdClientRequest() {
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public ThirdHTTPMethodEnum getRequestType() {
        return requestType;
    }

    public void setRequestType(ThirdHTTPMethodEnum requestType) {
        this.requestType = requestType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThirdClientRequest request = (ThirdClientRequest) o;
        return Objects.equals(requestUrl, request.requestUrl) && requestType == request.requestType && Objects.equals(mediaType, request.mediaType) && Objects.equals(charset, request.charset) && Objects.equals(requestBody, request.requestBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestUrl, requestType, mediaType, charset, requestBody);
    }

    @Override
    public String toString() {
        return "ThirdClientRequest{" +
                "requestUrl='" + requestUrl + '\'' +
                ", requestType=" + requestType +
                ", mediaType='" + mediaType + '\'' +
                ", charset='" + charset + '\'' +
                ", requestBody='" + requestBody + '\'' +
                '}';
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class APIResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private T data;
    private int code;
    private String message;

    public APIResponse() {

    }

    public APIResponse(int code) {
        this.code = code;
        this.success = false;
        if (code == 200) {
            this.success = true;
        }
    }

    public APIResponse(int code, T data) {
        this.code = code;
        this.success = false;
        if (code == 200) {
            this.success = true;
        }
        this.data = data;
    }

    public APIResponse(int code, String message) {
        this.code = code;
        this.success = false;
        if (code == 200) {
            this.success = true;
        }
        this.message = message;
    }

    public APIResponse(int code, String message, T data) {
        this.code = code;
        this.success = false;
        if (code == 200) {
            this.success = true;
        }
        this.message = message;
        this.data = data;
    }

    public static <T> APIResponse<T> success() {
        return new APIResponse<T>(200);
    }

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<T>(200, data);
    }

    public static <T> APIResponse<T> fail(String msg, int code) {
        return new APIResponse<T>(code, msg);
    }

    public static <T> APIResponse<T> fail(String msg, int code, T data) {
        return new APIResponse<T>(code, msg, data);
    }


}

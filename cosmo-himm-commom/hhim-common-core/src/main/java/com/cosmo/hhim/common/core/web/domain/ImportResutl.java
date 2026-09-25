/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.web.domain;

import com.cosmo.hhim.common.core.constant.HttpStatus;
import com.cosmo.hhim.common.core.utils.StringUtils;

import java.util.List;

public class ImportResutl extends AjaxResult {

    /**
     * 状态码
     */
    public static final String SUCCESS_LIST = "successList";

    /**
     * 返回内容
     */
    public static final String FAIL_LIST = "failList";

    public static final String SUCCESS_DATA = "successData";

    public static ImportResutl success(String msg, Object data, List<?> success, List<?> error,List<String> successData) {
        return new ImportResutl(HttpStatus.SUCCESS, msg, data, success, error,successData);
    }

    public static ImportResutl fail(String msg, Object data, List<?> success, List<?> error,List<String> successData) {
        return new ImportResutl(-1, msg, data, success, error,successData);
    }

    public ImportResutl(int code, String msg, Object data, List<?> success, List<?> error,List<String> successData) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
        super.put(SUCCESS_LIST, success);
        super.put(FAIL_LIST, error);
        super.put(SUCCESS_DATA, successData);
    }


    public ImportResutl(int code, String msg, Object data, List<?> success, List<?> error) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
        super.put(SUCCESS_LIST, success);
        super.put(FAIL_LIST, error);
    }

    /**
     * 返回错误消息
     *
     * @return
     */
    public static ImportResutl error() {
        return ImportResutl.error("操作失败",null,null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static ImportResutl error(String msg, List<?> success, List<?> error) {
        return ImportResutl.error(msg, null,success,error);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static ImportResutl error(String msg, Object data, List<?> success, List<?> error) {
        return new ImportResutl(HttpStatus.ERROR, msg, data, success, error);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 警告消息
     */
    public static ImportResutl error(int code, String msg, List<?> success, List<?> error) {
        return new ImportResutl(code, msg, null, success, error);
    }

    public static ImportResutl error(String msg) {
        return ImportResutl.error(msg, null,null,null);
    }

}

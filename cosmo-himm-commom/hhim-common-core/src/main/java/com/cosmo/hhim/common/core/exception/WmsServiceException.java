/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.exception;

import com.cosmo.hhim.common.core.enums.PromptEnum;
import com.cosmo.hhim.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * 自定义异常
 *
 * @author cosmo-hhim-open Team
 */
public class WmsServiceException extends RuntimeException {


    private String msg; // 错误信息
    private Integer code;
    private Object[] msgParams; // 错误参数

    public WmsServiceException(PromptEnum promptEnum) {
        this.code = PromptEnum.getCode(promptEnum.name());
        this.msg = PromptEnum.getMsg(promptEnum.name());
    }

    public WmsServiceException(String msg) { // 如果不传参数，直接调用父类构造方法
        super(msg);
        this.msg = msg;

    }

    public WmsServiceException(String msg, Object... msgParams) {
        this.msg = msg;
        this.msgParams = msgParams;
        throw new CustomException(this.getMessage());
    }

    public WmsServiceException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;

    }

    public WmsServiceException(Throwable e) {
        super(e);

    }

    @Override
    public String getMessage() {
        if (StringUtils.isNotBlank(this.getMsg())) {
            if (msgParams != null && msgParams.length > 0) {
                return MessageFormat.format(msg, msgParams);
            }
        }
        return super.getMessage(); // 如果不传参数，直接调用父类方法
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object[] getMsgParams() {
        return msgParams;
    }

    public void setMsgParams(Object[] msgParams) {
        this.msgParams = msgParams;
    }
}

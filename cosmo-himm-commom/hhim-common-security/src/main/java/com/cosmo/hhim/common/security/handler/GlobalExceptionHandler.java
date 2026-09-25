/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.handler;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.BaseException;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.exception.OmsException;
import com.cosmo.hhim.common.core.exception.PreAuthorizeException;
import com.cosmo.hhim.common.core.exception.WmsServiceException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author cosmo-hhim-open Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 全局表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）。
     * 说明：仅对表单/query 参数绑定（WebDataBinder）生效，@RequestBody 由 Jackson 处理，不受影响。
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    /**
     * 基础异常
     */
    @ExceptionHandler(BaseException.class)
    public AjaxResult baseException(BaseException e) {
        log.warn("BaseException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        return AjaxResult.error(e.getDefaultMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(CustomException.class)
    public AjaxResult customException(CustomException e) {
        log.warn("CustomException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        if (StringUtils.isNull(e.getCode())) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public AjaxResult validatedBindException(BindException e) {
        log.warn("BindException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        StringBuilder sb = new StringBuilder();
        for (ObjectError a : e.getAllErrors()) {
            sb.append(a.getDefaultMessage());
        }
        String message = sb.toString();
        return AjaxResult.error(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object validExceptionHandler(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return AjaxResult.error(message);
    }

    /**
     * 权限异常
     */
    @ExceptionHandler(PreAuthorizeException.class)
    public AjaxResult preAuthorizeException(PreAuthorizeException e) {
        return AjaxResult.error("没有权限，请联系管理员授权");
    }

    /**
     * wms业务异常
     */
    @ExceptionHandler(WmsServiceException.class)
    public AjaxResult handleWmsServiceException(WmsServiceException e) {
        log.warn("WmsServiceException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        if (StringUtils.isNull(e.getCode())) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 文件过大异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public AjaxResult handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        return AjaxResult.error("上传失败，文件过大");
    }

    /**
     * Exception
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        try {
            log.info("请求路径：{}", ServletUtils.getRequest().getRequestURI());
        } catch (Exception a) {

        }

        if (null == e || null == e.getMessage()) {
            log.error("exception:{}", Throwables.getStackTraceAsString(e));
            return AjaxResult.error("系统异常，请联系管理员");
        }

        try {
            if (e.getMessage().contains("Broken pipe")) {
                log.warn(e.getMessage(), e);
            } else {
                log.error("Exception:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
            return AjaxResult.error("系统异常，请联系管理员");
        } catch (Exception a) {
            log.error("Exception:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return AjaxResult.error("系统异常，请联系管理员");
        }
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(OmsException.class)
    public AjaxResult omsException(OmsException e) {
        log.warn("OmsException:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        if (StringUtils.isNull(e.getCode())) {
            return AjaxResult.error(e.getMessage());
        }
        return new AjaxResult(e.getCode(), e.getMessage(), e.getData());
    }


}

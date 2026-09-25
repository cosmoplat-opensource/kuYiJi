/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.exception;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.constants.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-11-14
 */
@Slf4j
@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ExceptionHandler
    public APIResponse handle(HttpServletRequest request, HandlerMethod method, Exception exception) {
        if (exception instanceof BindException) { // BindException异常返回信息封装
            log.error(String.format("访问 %s -> %s 出现参数信息异常！", request.getRequestURI(), method.toString()), exception);
            String errorMessage = exception.getMessage();
            List<ObjectError> allErrors = ((BindException) exception).getBindingResult().getAllErrors();
            if(!CollectionUtils.isEmpty(allErrors)){
                StringBuilder errorSb = new StringBuilder();
                for (int i = 0; i < allErrors.size(); i++) {
                    errorSb.append(allErrors.get(i).getDefaultMessage());
                    if(i<allErrors.size()){
                        errorSb.append(";");
                    }
                }
                errorMessage = errorSb.toString();
            }
            return APIResponse.fail(errorMessage, Constant.ERROR_REQUEST_CODE);
        } else if (exception instanceof MethodArgumentNotValidException){
            log.error(String.format("访问 %s -> %s 出现参数信息异常！", request.getRequestURI(), method.toString()), exception);
            String errorMessage = exception.getMessage();
            List<ObjectError> allErrors = ((MethodArgumentNotValidException) exception).getBindingResult().getAllErrors();
            if(!CollectionUtils.isEmpty(allErrors)){
                StringBuilder errorSb = new StringBuilder();
                for (int i = 0; i < allErrors.size(); i++) {
                    errorSb.append(allErrors.get(i).getDefaultMessage());
                    if(i<allErrors.size()){
                        errorSb.append(";");
                    }
                }
                errorMessage = errorSb.toString();
            }
            return APIResponse.fail(errorMessage, Constant.ERROR_REQUEST_CODE);
        } else{ // 其他异常异常返回信息封装
            log.error(String.format("访问 %s -> %s 服务发生异常！", request.getRequestURI(), method.toString()), exception);
            return APIResponse.fail(exception.getMessage(), Constant.ERROR_SERVER_CODE);
        }
    }
}

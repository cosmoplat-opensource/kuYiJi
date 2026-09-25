/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.security.datamask.annotations.DataMaskApi;
import com.cosmo.hhim.common.security.datamask.cache.PricePermissionCache;
import com.cosmo.hhim.common.security.datamask.constants.DataMaskConstant;
import com.cosmo.hhim.common.security.datamask.constants.enums.SelectMode;
import com.cosmo.hhim.common.security.datamask.filter.DataMaskBeanPropertyFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏Controller拦截器
 * @createTime 2022-01-11
 */
@Slf4j
@ControllerAdvice
public class DataMaskControllerAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        AnnotatedElement annotatedElement = returnType.getAnnotatedElement();

        // 判断类或者方法是否支持数据脱敏
        Method method = returnType.getMethod();
        DataMaskApi methodSupport = method.getAnnotation(DataMaskApi.class);
        DataMaskApi clazzSupport = method.getDeclaringClass().getAnnotation(DataMaskApi.class);
        return methodSupport != null || clazzSupport != null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("[DataMaskResponseBodyAdvice][beforeBodyWrite] start==> responseData:{}, responseDataType:{}", JSON.toJSONString(body), returnType);

        // 针对编辑查询不做数据脱敏
        List<String> selectModeList = request.getHeaders().get(DataMaskConstant.HEADER_SELECT_MODE);
        if (!CollectionUtils.isEmpty(selectModeList)) {
            if (SelectMode.parse(selectModeList.get(0)) == SelectMode.EDIT_SELECT) {
                log.info("[DataMaskResponseBodyAdvice][beforeBodyWrite] end（跳过脱敏处理）==> responseData:{}, responseDataType:{}", JSON.toJSONString(body), returnType);
                return body;
            }
        }

        try {
            // 数据脱敏处理
            body = JSONObject.parseObject(JSON.toJSONString(body, new DataMaskBeanPropertyFilter()));
        } catch (Exception e) {
            log.error("数据脱敏处理发生异常！message:{}", e.getMessage());

        } finally {
            PricePermissionCache.clearCache();
        }
        stopWatch.stop();
        log.info("[DataMaskResponseBodyAdvice][beforeBodyWrite] end（脱敏处理）==> responseData:{}, responseDataType:{}, useTime:{}s", JSON.toJSONString(body), returnType, stopWatch.getTotalTimeSeconds());
        return body;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.domain.R;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.reflect.ReflectUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RestTemplate通用请求方法
 */
public class RestTemplateUtils {
    // request
    public static <T> T restTemplateForExchange(String url, HttpMethod method, Map<String, Object> queryParam, Class<T> resClz) {
        return restTemplateForExchange(url, method, queryParam, null, resClz,"");
    }

    // body
    public static <T> T restTemplateForExchange(String url, HttpMethod method, JSONObject postParams, Class<T> resClz) {
        return restTemplateForExchange(url, method, null, postParams, resClz, "");
    }

    // pathVariables
    public static <T> T restTemplateForExchange(String url, HttpMethod method, Class<T> resClz, String... uriVariables) {
        return restTemplateForExchange(url, method, null, null, resClz, uriVariables);
    }

    // request + body
    public static <T> T restTemplateForExchange(String url, HttpMethod method, Map<String, Object> queryParams, JSONObject postParams, Class<T> resClz) {
        return restTemplateForExchange(url, method, queryParams, postParams, resClz, "");
    }


    // request/body (实体类参数)
    public static <T, K> T restTemplateForExchange(String url, HttpMethod method, K k, Class<T> resClz) {
        if (HttpMethod.GET.equals(method)) {
            Map<String, Object> queryParam = getParamsMap(k);
            return restTemplateForExchange(url, method, queryParam, null, resClz,"");
        }
        if (HttpMethod.POST.equals(method)) {
            JSONObject postParams = (JSONObject) JSON.toJSON(k);
            return restTemplateForExchange(url, method, null, postParams, resClz, "");
        }
        return null;
    }

    // request + body(实体类参数)
    public static <T, K, V> T restTemplateForExchange(String url, HttpMethod method, K k, V v, Class<T> resClz) {
        Map<String, Object> queryParams = getParamsMap(k);
        JSONObject postParams = (JSONObject) JSON.toJSON(v);
        return restTemplateForExchange(url, method, queryParams, postParams, resClz, "");
    }

    // request + body + pathVariables
    public static <T> T restTemplateForExchange(String url, HttpMethod method, Map<String, Object> queryParams, JSONObject postParams, Class<T> resClz, String... uriVariables) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            if (queryParams != null) {
                queryParams.forEach(builder::queryParam);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> request = new HttpEntity<>(headers);
            if (postParams != null) {
                request = new HttpEntity<>(postParams.toJSONString(), headers);
            }
            // 接口返回类型
            if (TableDataInfo.class.equals(resClz) || AjaxResult.class.equals(resClz)) {
                // 1.TableDataInfo
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(5*1000);
                requestFactory.setReadTimeout(5*1000);
                HttpEntity<T> response = new RestTemplate(requestFactory).exchange(builder.build().toUriString(), method, request, resClz, uriVariables);
                T body = response.getBody();
                if (body == null) {
                    throw new CustomException("调用接口异常");
                }
                return body;
            } else {
                // 2.R
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(5*1000);
                requestFactory.setReadTimeout(5*1000);
                HttpEntity<R> response = new RestTemplate(requestFactory).exchange(builder.build().toUriString(), method, request, R.class, uriVariables);
                R body = response.getBody();
                if (body == null) {
                    throw new CustomException("调用接口异常");
                }
                if (body.getCode() != 200) {
                    throw new CustomException(body.getMsg(), body.getCode());
                }
                return getEntity(body.getData(), resClz);
            }
        } catch (CustomException e) {

            throw new CustomException(e.getMessage(), e);
        } catch (Exception e) {

            throw new CustomException("调用接口异常", e);
        }

    }

    public static <T> List<T> getEntity(List<?> objectList, Class<T> clz) {
        if (objectList == null) {
            return null;
        }
        return objectList.stream().map(t -> JSON.parseObject(JSON.toJSONString(t), clz)).collect(Collectors.toList());
    }

    public static <T> T getEntity(Object object, Class<T> clz) {
        return JSON.parseObject(JSON.toJSONString(object), clz);
    }

    public static <T> Map<String, Object> getParamsMap(T t) {
        Map<String, Object> params = new HashMap<>();
        getAccessibleFields(t).forEach(f -> {
            String genericType = f.getGenericType().toString();
            if (!"java.util.Map".equals(genericType) && "java.lang.List".equals(genericType)) {
                params.put(f.getName(), ReflectUtils.getFieldValue(t, f.getName()));
            }
        });
        return params;

    }

    public static List<Field> getAccessibleFields(Object obj) {
        List<Field> fieldList = new ArrayList<>();
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            Field[] fields = superClass.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
        }
        return fieldList;
    }

}

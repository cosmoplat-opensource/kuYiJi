/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.infrastructure.config.TodoPushConfig;
import com.cosmo.hhim.micro.integration.domain.entity.TodoModifyParam;
import com.cosmo.hhim.micro.integration.domain.entity.TodoPushParam;
import com.cosmo.hhim.micro.integration.domain.entity.TodoPushResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class TodoPushService { 
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TodoPushConfig pushConfig;
    /**
 * @author cosmo-hhim-open Team
     * MAC算法的标准名称
     */
    public static final String MAC_ALGORITHM = "HmacSHA1";

    /**
     * 推送待办
     *
     * @param pushParam
     * @return
     */
    public TodoPushResult push(TodoPushParam pushParam) {
        return this.invokeRequest(convertPushParam(pushParam));
    }

    /**
     * 修改待办状态
     *
     * @param pushParam
     * @return
     */
    public TodoPushResult modify(TodoModifyParam pushParam) {
        return this.invokeRequest(convertModifyParam(pushParam));
    }

    /**
     * 转换修改待办实体
     *
     * @param pushParam
     * @return
     */
    private Map<String, String> convertModifyParam(TodoModifyParam pushParam) {
        Map<String, String> paramMap = new TreeMap<>();
        String sourceId = pushParam.getSourceId();
        Assert.notNull(pushParam.getRedirectUrl(), "待办跳转URL不能为空");
        Assert.notNull(pushParam.getSenderId(), "待办发送人不能为空");
        Assert.notNull(pushParam.getHandlerId(), "待办接收人不能为空");
        Assert.notNull(pushParam.getStatus(), "待办状态不能为空");
        paramMap.put("appid", pushConfig.getAppId());
        paramMap.put("parentId", pushConfig.getParentId());
        paramMap.put("sourceId", sourceId == null ? UUID.randomUUID().toString() : sourceId);
        paramMap.put("redirectUrl", pushParam.getRedirectUrl());
        paramMap.put("senderId", pushParam.getSenderId());
        paramMap.put("handlerId", pushParam.getHandlerId());
        if (!StringUtils.isEmpty(pushParam.getLabel())) {
            paramMap.put("label", pushParam.getLabel());
        }
        paramMap.put("status", pushParam.getStatus().getCode());
        paramMap.put("signature", this.createSign(paramMap));
        return paramMap;

    }

    /**
     * 发起请求
     *
     * @param paramMap
     * @return
     */
    private TodoPushResult invokeRequest(Map<String, String> paramMap) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("appid", pushConfig.getAppId());
        HttpEntity<Map<String, String>> request = new HttpEntity<>(paramMap, headers);
        log.info("todo_push request info is {}", JSONObject.toJSONString(request));
        String pushUrl = pushConfig.getGatewayUrl() + pushConfig.getPushPath();
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(pushUrl, request, JSONObject.class);
        log.info("todo_push response info is {}", JSONObject.toJSONString(responseEntity));
        JSONObject body = responseEntity.getBody();
        Assert.notNull(body, "response body is empty");
        return body.toJavaObject(TodoPushResult.class);
    }

    /**
     * 转换参数
     *
     * @param pushParam
     * @return
     */
    private Map<String, String> convertPushParam(TodoPushParam pushParam) {
        Map<String, String> paramMap = new TreeMap<>();
        String sourceId = pushParam.getSourceId();
        Assert.notNull(pushParam.getTitle(), "待办标题不能为空");
        Assert.notNull(pushParam.getRedirectUrl(), "待办跳转URL不能为空");
        Assert.notNull(pushParam.getSenderId(), "待办发送人不能为空");
        Assert.notNull(pushParam.getHandlerId(), "待办接收人不能为空");
        Assert.notNull(pushParam.getStatus(), "待办状态不能为空");
        paramMap.put("appid", pushConfig.getAppId());
        paramMap.put("parentId", pushConfig.getParentId());
        paramMap.put("sourceId", sourceId == null ? UUID.randomUUID().toString() : sourceId);
        paramMap.put("title", pushParam.getTitle());
        if (!StringUtils.isEmpty(pushParam.getContent())) {
            paramMap.put("content", pushParam.getContent());
        }
        if (!StringUtils.isEmpty(pushParam.getHeadImg())) {
            paramMap.put("headImg", pushParam.getHeadImg());
        }
        paramMap.put("redirectUrl", pushParam.getRedirectUrl());
        paramMap.put("senderId", pushParam.getSenderId());
        paramMap.put("handlerId", pushParam.getHandlerId());
        if (!StringUtils.isEmpty(pushParam.getLabel())) {
            paramMap.put("label", pushParam.getLabel());
        }
        paramMap.put("status", pushParam.getStatus().getCode());
        paramMap.put("signature", this.createSign(paramMap));
        return paramMap;
    }

    /**
     * 构建签名
     *
     * @param paramMap
     * @return
     */
    private String createSign(Map<String, String> paramMap) {
        Iterator<String> it = paramMap.keySet().iterator();
        StringBuilder sortQueryStringTmp = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paramMap.get(key)));
        }
        String stringToSign = sortQueryStringTmp.substring(1);
        return this.sign(stringToSign);
    }

    /**
     * 对参数编码
     *
     * @param value
     * @return
     */
    private String specialUrlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%2B").replace("*", "%2A").replace("~", "%7E");
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to special url encode:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
        return null;
    }

    /**
     * 对参数签名
     *
     * @param stringToSign
     * @return
     */
    private String sign(String stringToSign) {
        try {
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            // 签名密钥从配置读取（todopush.appSecret），避免硬编码到代码
            String appSecret = pushConfig.getAppSecret();
            if (!StringUtils.hasText(appSecret)) {
                throw new CustomException("未配置待办签名密钥（todopush.appSecret）");
            }
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), MAC_ALGORITHM));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            log.error("签名失败:signContent->{},{}:{}, Error:{}", stringToSign, e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            throw new CustomException("签名失败,签名内容" + stringToSign);
        }
    }
}

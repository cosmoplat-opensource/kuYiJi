/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.infrastructure.events.OpenFeishuEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class OpenFeishuEventListener implements ApplicationListener<OpenFeishuEvent> {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.profiles.active}")
    private String env;

    public static final String PROD_ENV = "prod";
    /**
     * 飞书机器人地址，通过配置注入
     */
    @Value("${notify.feishu.suggest-bot-url:please_set_feishu_suggest_bot_url}")
    private String suggestInfoPushFeishuBotUrl;

    @Async("asyncEventExecutor")
    @Override
    public void onApplicationEvent(OpenFeishuEvent event) {
        if (!PROD_ENV.equals(env)) {
            return;
        }
        String msg = event.getMessage();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HashMap<String, String> msgMap = new HashMap<>(8);
        msgMap.put("text", msg);
        HashMap paramMap = new HashMap<String, String>(8);
        paramMap.put("msg_type", "text");
        paramMap.put("content", msgMap);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(paramMap, headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(suggestInfoPushFeishuBotUrl, request, JSONObject.class);
        log.info("推送意见反馈信息结果:{}", responseEntity);
    }
}

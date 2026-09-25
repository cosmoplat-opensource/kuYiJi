/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


@Configuration
public class SendIhaierMsg {
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${hhim.envgroup}")
    private String envgroup;
    @Value("${spring.application.name}")
    private String name;
    protected final Logger logger = LoggerFactory.getLogger(SendIhaierMsg.class);
    @Value("${local:false}")
    private boolean local;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${notify.feishu.prod-webhook:please_set_feishu_prod_webhook}")
    private String prodWebhook;
    @Value("${notify.feishu.test-webhook:please_set_feishu_test_webhook}")
    private String testWebhook;
    @Bean
    public void sendMsg()  {


        Thread thread =new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    logger.error("发送消息线程被中断", e);
                }
                if("dev".equals(active) || local){
                    return;
                }
                String url="";
                try{
                    JSONObject jsonObject = new JSONObject();
                    JSONObject jsonObject1 = new JSONObject();
                    if(Constants.PROFILES_ACTIVE_PROD.equals(active)){
                        jsonObject1.put("text","生产环境"+envgroup+"组，应用名:"+name+"发版完成");
                        url=prodWebhook;
                    }else if(Constants.PROFILES_ACTIVE_TEST.equals(active)){
                        jsonObject1.put("text","测试环境"+name+"发版完成");
                        url=testWebhook;
                    }else{
                        jsonObject1.put("text","预发环境"+name+"发版完成");
                        url=testWebhook;
                    }
                    jsonObject.put("content", JSONObject.toJSONString(jsonObject1));
                    jsonObject.put("msg_type", "text");
                    HttpHeaders headers = new HttpHeaders();
                    MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
                    headers.setContentType(type);
                    headers.add("Accept", MediaType.APPLICATION_JSON.toString());
                    HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(jsonObject), headers);
                    restTemplate.postForEntity(url, request, JSONObject.class);
                }catch (Exception e){
                    logger.error("推送发版消息失败",e);
                }
            }
        });
        thread.start();

    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzMicroMiniappConfigMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzWechatConfigMapper;
import com.cosmo.hhim.thirdplat.web.web.TenantWebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author cosmo-hhim-open Team
 */
@Configuration
public class TenantWebInterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private HyzzWechatConfigMapper hyzzWechatConfigMapper;

    @Autowired
    private HyzzMicroMiniappConfigMapper hyzzMicroMiniappConfigMapper;

    @Value("${forest.variables.wxmpAppId}")
    private String wxmpAppId;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantWebFilter(hyzzMicroMiniappConfigMapper, hyzzWechatConfigMapper, wxmpAppId));
    }

    @Bean
    public RestTemplate getRestTemplateBean() {
        return new RestTemplate();
    }


    @Value("${spring.application.name}")
    private String name;
    @Value("${spring.profiles.active}")
    private String active;
    @Value("${hhim.envgroup}")
    private String envgroup;
    @Autowired
    private RestTemplate restTemplate;
    protected final Logger logger = LoggerFactory.getLogger(TenantWebInterceptorConfig.class);

    @Value("${notify.feishu.prod-webhook:please_set_feishu_prod_webhook}")
    private String prodWebhook;
    @Value("${notify.feishu.test-webhook:please_set_feishu_test_webhook}")
    private String testWebhook;
    @Bean
    public void sendMsg()  {

        if("dev".equals(active)){
            return;
        }
        String url="";
        try{
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObject1 = new JSONObject();
            if("prod".equals(active)){
                jsonObject1.put("text","生产环境"+envgroup+name+"发版");
                url=prodWebhook;
            }else if("test".equals(active)){
                jsonObject1.put("text","测试环境"+name+"发版");
                url=testWebhook;
            }else{
                jsonObject1.put("text","预发环境"+name+"发版");
                url=testWebhook;
            }
            jsonObject.put("content", JSONObject.toJSONString(jsonObject1));
            jsonObject.put("msg_type", "text");
            HttpHeaders headers = new HttpHeaders();
            MediaType type = MediaType.parseMediaType("application/json;charset=UTF-8");
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(jsonObject), headers);
            ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(url, request, JSONObject.class);
            logger.info("webhook response: {}", JSONObject.toJSONString(responseEntity));
        }catch (Exception e){
            logger.error("推送发版消息失败",e);
        }

    }
}

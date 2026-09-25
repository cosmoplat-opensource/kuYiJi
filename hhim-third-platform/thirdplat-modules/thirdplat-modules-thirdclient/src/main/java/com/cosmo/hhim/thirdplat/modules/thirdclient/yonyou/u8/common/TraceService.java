/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.common;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.exception.OpenAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Objects;

@Slf4j
@Service
public class TraceService {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RestTemplate restTemplate;

    private static String YONYOU_U8_TRADEID_URL = "https://api.yonyouup.com/system/tradeid";

    public String getTradeId(ThirdClientTenant clientTenant) throws OpenAPIException {
        JSONObject authInfo = clientTenant.getAuthInfo();
        String from_account = authInfo.getString("from_account");
        String app_key = authInfo.getString("app_key");
        String token = tokenService.getToKenId(clientTenant);
        String app_secret = authInfo.getString("app_secret");
        String to_account = authInfo.getString("to_account");
        String ds_sequence = authInfo.getString("ds_sequence");

        MultiValueMap<String, String> basicParams = new LinkedMultiValueMap<>();
        basicParams.add("from_account", from_account);
        basicParams.add("to_account", to_account);
        basicParams.add("app_key", app_key);
        basicParams.add("app_secret", app_secret);
        basicParams.add("ds_sequence", ds_sequence);
        basicParams.add("token",token);

        String tradeId;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(YONYOU_U8_TRADEID_URL);
            URI uri = builder.queryParams(basicParams).build().encode().toUri();
            ResponseEntity<JSONObject> forEntity = restTemplate.getForEntity(uri, JSONObject.class);
            JSONObject jsonObject = Objects.requireNonNull(forEntity.getBody());
            log.info("请求U8接口生成tradeId----请求结果:{}",jsonObject);
            JSONObject tradeObject = jsonObject.getJSONObject("trade");
            tradeId = tradeObject.getString("id");
        } catch (Exception e) {
            throw new OpenAPIException(e.getMessage(), e);
        }

        return tradeId;
    }

}

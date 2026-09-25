/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.common;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.exception.OpenAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Objects;

@Slf4j
@Service("yonyouU8TokenService")
public class TokenService {

    /**
     * U8 token请求路径
     */
    private static String YONYOU_U8_TOKEN_URL = "https://api.yonyouup.com/system/token";

    @Autowired
    private RestTemplate restTemplate;

    public String getToKenId(ThirdClientTenant clientTenant) {

        AccessToken accessToken = new AccessToken();
        try {
            accessToken = getAccessToken(clientTenant.getAuthInfo());
            log.info("请求U8最终获取token为:{}", accessToken);
        } catch (OpenAPIException e) {
            log.error("获取U8 token失败", e);
        }
        return accessToken.getId();
    }

    private AccessToken getAccessToken(JSONObject authInfo) throws OpenAPIException {
        String from_account = authInfo.getString("from_account");
        String app_key = authInfo.getString("app_key");
        String app_secret = authInfo.getString("app_secret");
        String to_account = authInfo.getString("to_account");
        String ds_sequence = authInfo.getString("ds_sequence");
        MultiValueMap<String, String> basicParams = new LinkedMultiValueMap<>();
        basicParams.add("from_account", from_account);
        basicParams.add("to_account", to_account);
        basicParams.add("app_key", app_key);
        basicParams.add("app_secret", app_secret);
        basicParams.add("ds_sequence", ds_sequence);

        JSONObject jsonObject;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(YONYOU_U8_TOKEN_URL);
            URI uri = builder.queryParams(basicParams).build().encode().toUri();
            ResponseEntity<JSONObject> forEntity = restTemplate.getForEntity(uri, JSONObject.class);
            jsonObject = Objects.requireNonNull(forEntity.getBody());
        } catch (Exception e) {
            throw new OpenAPIException(e.getMessage(), e);
        }
        log.info("获取U8token:{}", jsonObject);
        JSONObject tokenObject = jsonObject.getJSONObject("token");
        AccessToken token = new AccessToken();
        token.setId(tokenObject.getString("id"));
        token.setAppKey(tokenObject.getString("appKey"));
        token.setCreateTime(System.currentTimeMillis());
        token.setExpiresIn(tokenObject.getLongValue("expiresIn"));
        return token;
    }

    public static class AccessToken {

        private String id;

        private String appKey;

        private long createTime;

        private long expiresIn;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        @Override
        public String toString() {
            return "AccessToken{" +
                    "id='" + id + '\'' +
                    ", appKey='" + appKey + '\'' +
                    ", createTime=" + createTime +
                    ", expiresIn=" + expiresIn +
                    '}';
        }
    }
}

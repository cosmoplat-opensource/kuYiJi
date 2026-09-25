/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.interceptor;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.config.ForestGlobalConfig;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.constants.UniPushConstant;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.auth.TokenInfo;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.http.body.NameValueRequestBody;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Slf4j
public class AcquireTokenInterceptor implements Interceptor<UniPushResponseResult<TokenInfo>> {

    private static final String APPKEY = "appkey";
    private static final String TIMESTAMP = "timestamp";
    private static final String SIGN = "sign";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ForestGlobalConfig forestGlobalConfig;

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.info("http请求前置拦截器--->token的请求参数赋值");
        Map<String, String> variablesMap = forestGlobalConfig.getVariables();
        if (CollectionUtils.isEmpty(variablesMap)) {
            log.warn("forest global config is null!!!");
            return false;
        }

        String appKey = variablesMap.get(UniPushConstant.UNI_APPKEY);

        // 从redis中获取缓存的token，存在则不发起请求
        String redisResult = redisTemplate.opsForValue().get(Constant.RedisKeys.UNIPUSH.value(appKey));
        if (StringUtils.hasText(redisResult)) {
            TokenInfo tokenInfo = JSON.parseObject(redisResult, TokenInfo.class);
            // TODO 如何包装一个response，返回token信息？？？

            return false;
        }

        String masterSecret = variablesMap.get(UniPushConstant.UNI_MASTERSECRET);
        String currentTime = String.valueOf(System.currentTimeMillis());
        String sign = SecureUtil.sha256(appKey + currentTime + masterSecret);
        request.addBody(APPKEY, appKey);
        request.addBody(TIMESTAMP, currentTime);
        request.addBody(SIGN, sign);

        return true;
    }

    @Override
    public void onSuccess(UniPushResponseResult<TokenInfo> data, ForestRequest request, ForestResponse response) {
        log.info("http请求后置成功拦截器--->token缓存redis");
        List<NameValueRequestBody> bodys = request.getBody();
        if (!CollectionUtils.isEmpty(bodys)) {
            for (NameValueRequestBody body : bodys) {
                if (APPKEY.equals(body.getName())) {
                    TokenInfo tokenInfo = data.getData();
                    String key = Constant.RedisKeys.UNIPUSH.value((String) body.getValue());
                    String value = JSON.toJSONString(tokenInfo);
                    long expireTime = Long.parseLong(tokenInfo.getExpireTime()) - System.currentTimeMillis();
                    expireTime = expireTime - 5000; // redis缓存提起5s过期，防止出现边界时间点token过期问题
                    redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}

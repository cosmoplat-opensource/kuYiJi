/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.config.ForestGlobalConfig;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.api.UniAuthApi;
import com.cosmo.hhim.thirdplat.modules.unipush.constants.UniPushConstant;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.auth.TokenInfo;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Slf4j
public class TokenHeaderInterceptor implements Interceptor<UniPushResponseResult> {

    private static final String TOKENKEY = "token";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ForestGlobalConfig forestGlobalConfig;

    @Autowired
    private UniAuthApi uniAuthApi;

    @Override
    public boolean beforeExecute(ForestRequest request) {
        log.info("http请求前置拦截器--->header token赋值");
        String token = null;
        Map<String, String> variablesMap = forestGlobalConfig.getVariables();
        if (!CollectionUtils.isEmpty(variablesMap)) {
            String appKey = variablesMap.get(UniPushConstant.UNI_APPKEY);
            String tokenInfo = redisTemplate.opsForValue().get(Constant.RedisKeys.UNIPUSH.value(appKey));
            if (StringUtils.hasText(tokenInfo)) {
                token = JSON.parseObject(tokenInfo, TokenInfo.class).getToken();
            } else {
                ForestResponse<UniPushResponseResult<TokenInfo>> response = uniAuthApi.getToken();
                if (response.isSuccess() && response.getResult().isSuccess()) {
                    token = response.getResult().getData().getToken();
                }
            }
        }
        if (StringUtils.hasText(token)) {
            request.addHeader(TOKENKEY, token);
        } else {
            log.error("can't to request, token get failed!!!");
            return false;
        }
        return true;
    }
}

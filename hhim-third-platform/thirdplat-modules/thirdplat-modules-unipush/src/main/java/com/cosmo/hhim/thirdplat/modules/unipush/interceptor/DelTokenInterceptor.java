/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.interceptor;

import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.config.ForestGlobalConfig;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.constants.UniPushConstant;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Slf4j
public class DelTokenInterceptor implements Interceptor<UniPushResponseResult<String>> {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ForestGlobalConfig forestGlobalConfig;

    @Override
    public void onSuccess(UniPushResponseResult<String> data, ForestRequest request, ForestResponse response) {
        log.info("http请求后置成功拦截器--->清理redis token缓存");
        Map<String, String> variablesMap = forestGlobalConfig.getVariables();
        if (!CollectionUtils.isEmpty(variablesMap)) {
            String appKey = variablesMap.get(UniPushConstant.UNI_APPKEY);
            redisTemplate.delete(Constant.RedisKeys.UNIPUSH.value(appKey));
        }
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.application.service.base.IMicroThirdPlatFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppConfig;
import com.cosmo.hhim.micro.base.domain.service.common.IAppConfigService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.google.common.collect.HashBiMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * decouple-from-ops-platform：app_sign ↔ app_code 映射改读本地 micro_app_config
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroThirdPlatFacadeServiceImpl implements IMicroThirdPlatFacadeService {

    @Autowired
    private IAppConfigService appConfigService;
    @Autowired
    private RedisCache redisCache;

    /**
     * 读取本地 micro_app_config 表的 app_sign ↔ app_code 映射
     *
     * @return Map<app_sign, app_code>
     */
    @Override
    public Map<String, String> cacheMicroApplicationConfig() {
        HashBiMap<String, String> tempMap = HashBiMap.create();

        MicroAppConfig query = new MicroAppConfig();
        query.setStatus(1);
        List<MicroAppConfig> configList = appConfigService.list(query);
        if (!CollectionUtils.isEmpty(configList)) {
            configList.forEach(c -> tempMap.put(c.getAppSign(), c.getAppCode()));
            redisCache.setCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY, tempMap);
            return tempMap.inverse();
        }
        return Collections.emptyMap();
    }
}

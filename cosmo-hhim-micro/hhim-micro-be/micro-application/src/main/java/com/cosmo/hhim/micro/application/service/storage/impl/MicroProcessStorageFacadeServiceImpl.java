/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage.impl;

import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.application.service.storage.IMicroProcessStorageFacadeService;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.security.SecureRandom;


/**
 * 工序库存
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroProcessStorageFacadeServiceImpl implements IMicroProcessStorageFacadeService {

    @Autowired
    private IMicroProcessStorageService storageService;
    @Autowired
    private MicroProcessStorageMapper storageMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 获取所有产品+工序目前的良品率及日产能指标
     */
    @Override
    public void flushWarningMetrics(String tenantCode) {
        String redisKeyPrefix = CommonConstants.WARNING_KEY + tenantCode + CommonConstants.SEPARATOR_SYMBOL;
        Map<String, Map<String, Object>> metrics = storageMapper.getProductAndProcessWarningMetrics();
        metrics.forEach((k, v) -> {
            String key = redisKeyPrefix + k;
            redisCache.setCacheMap(key, v);
            redisCache.expire(key, getTimeout(), TimeUnit.SECONDS);
        });
    }

    /**
     * 根据产品id集合删除库存为0的数据
     *
     * @param productIds
     * @return
     */
    @Override
    public int removeZeroProcessStorage(List<Long> productIds) {
        return storageService.removeZeroProcessStorageByProduct(productIds);
    }

    /** 安全的随机数源（用于超时时间随机抖动） */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private long getTimeout() {
        return 60 * 60 * 6L + SECURE_RANDOM.nextInt(100);
    }
}

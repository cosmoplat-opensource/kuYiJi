/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserCompleteInfo;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroTenantMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.base.domain.service.common.IUserTenantIndexService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 租户元数据 Service 实现
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroTenantServiceImpl implements IMicroTenantService {

    @Autowired
    private MicroTenantMapper microTenantMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IUserTenantIndexService userTenantIndexService;

    @Override
    public MicroTenant getByCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isEmpty()) {
            return null;
        }
        return microTenantMapper.selectByCode(tenantCode);
    }

    @Override
    public List<MicroTenant> listActive() {
        return microTenantMapper.selectActiveList();
    }

    @Override
    public List<MicroTenant> list(MicroTenant query) {
        return microTenantMapper.selectList(query);
    }

    @Override
    public int insert(MicroTenant record) {
        return microTenantMapper.insert(record);
    }

    @Override
    public int updateByCode(MicroTenant record) {
        return microTenantMapper.updateByCode(record);
    }

    @Override
    public void dissolve(String tenantCode, String operatedBy) {
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户编码不能为空", 400);
        }

        // 1. 校验租户存在且处于启用状态
        MicroTenant tenant = microTenantMapper.selectByCode(tenantCode);
        if (tenant == null) {
            throw new CustomException("租户不存在", 404);
        }
        if (tenant.getStatus() == null || tenant.getStatus() != 1) {
            throw new CustomException("租户已解散或已停用", 400);
        }

        // 2. 标记租户为已解散
        Date now = DateUtils.getNowDate();
        MicroTenant update = new MicroTenant();
        update.setTenantCode(tenantCode);
        update.setStatus(0);
        update.setUpdatedAt(now);
        microTenantMapper.updateByCode(update);
        log.info("dissolve: tenantCode={} 已标记为解散, operatedBy={}", tenantCode, operatedBy);

        // 3. 清理用户-租户索引（所有用户将查不到该租户）
        int deletedIdx = userTenantIndexService.deleteByTenantCode(tenantCode);
        log.info("dissolve: tenantCode={} 清理 micro_user_tenant_index {} 条", tenantCode, deletedIdx);

        // 4. 清理 Redis 中该租户所有用户的 login token（key 格式与 MicroAccessAuthFilterCondition 一致）
        String tokenPattern = Constants.LOGIN_MICRO_TOKEN_KEY + "*";
        Set<String> tokenKeys = redisCache.scan(tokenPattern);
        int clearedTokens = 0;
        for (String key : tokenKeys) {
            try {
                String json = redisCache.getCacheObject(key);
                if (!StringUtils.hasText(json)) {
                    continue;
                }
                MicroUserCompleteInfo info = JSON.parseObject(json, MicroUserCompleteInfo.class);
                if (info != null && tenantCode.equals(info.getCustomer())) {
                    redisCache.deleteObject(key);
                    clearedTokens++;
                }
            } catch (Exception e) {
                log.warn("dissolve: 清理 token 异常 key={}", key, e);
            }
        }
        log.info("dissolve: tenantCode={} 清理 Redis token {} 条", tenantCode, clearedTokens);
    }
}

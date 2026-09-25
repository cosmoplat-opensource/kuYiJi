/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenantIndividuationConfig;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroTenantIndividuationConfigMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantIndividuationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 企业个性化设置配置Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-04
 */
@Service
public class MicroTenantIndividuationConfigServiceImpl implements IMicroTenantIndividuationConfigService {
    @Autowired
    private MicroTenantIndividuationConfigMapper microTenantIndividuationConfigMapper;

    /**
     * 查询企业个性化设置配置
     *
     * @return 企业个性化设置配置（无记录时返回默认值，避免前端因 data 为 null 导致页面初始化失败）
     */
    @Override
    public MicroTenantIndividuationConfig selectMicroTenantIndividuationConfigByTenant() {
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        if (!StringUtils.hasText(tenantCode)) {
            return defaultConfig();
        }
        MicroTenantIndividuationConfig config = microTenantIndividuationConfigMapper.selectMicroTenantIndividuationConfigByTenant(tenantCode);
        if (config == null) {
            return defaultConfig();
        }
        return config;
    }

    /**
     * 返回默认配置（各开关默认为关闭状态：'1'）
     */
    private MicroTenantIndividuationConfig defaultConfig() {
        MicroTenantIndividuationConfig config = new MicroTenantIndividuationConfig();
        config.setSubmitInspectSwitch("1");
        config.setBatchSubmitSwitch("1");
        config.setWorkerBaseDataConfine("1");
        return config;
    }

    /**
     * 更新企业个性化配置
     * @param microTenantIndividuationConfig
     */
    @Override
    public void updateMicroTenantIndividuationConfig(MicroTenantIndividuationConfig microTenantIndividuationConfig) {
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("无法获取租户信息");
        }
        microTenantIndividuationConfig.setLastUpdBy(SecurityUtils.getUserId().toString());
        microTenantIndividuationConfig.setLastUpdDate(DateUtils.getNowDate());
        // created 字段 NOT NULL：首次插入时兜底用当前用户/时间（更新分支不会覆盖，保留首次创建信息）
        if (microTenantIndividuationConfig.getCreatedBy() == null) {
            microTenantIndividuationConfig.setCreatedBy(microTenantIndividuationConfig.getLastUpdBy());
        }
        if (microTenantIndividuationConfig.getCreatedDate() == null) {
            microTenantIndividuationConfig.setCreatedDate(microTenantIndividuationConfig.getLastUpdDate());
        }
        // upsert：依赖表唯一约束 uk_tenant_code(tenant_code)，冲突自动转更新。
        // 替代原"先查后插"（check-then-act 竞态：并发首次保存会双插，导致 selectOne 报 TooManyResults）
        microTenantIndividuationConfigMapper.upsertMicroTenantIndividuationConfig(microTenantIndividuationConfig, tenantCode);
    }
}

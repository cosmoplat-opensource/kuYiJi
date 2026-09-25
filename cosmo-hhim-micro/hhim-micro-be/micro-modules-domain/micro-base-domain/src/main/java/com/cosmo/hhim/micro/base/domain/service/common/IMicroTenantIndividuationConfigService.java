/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenantIndividuationConfig;

/**
 * 企业个性化设置配置Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-04
 */
public interface IMicroTenantIndividuationConfigService {
    /**
     * 查询企业个性化设置配置
     *
     * @return 企业个性化设置配置
     */
    MicroTenantIndividuationConfig selectMicroTenantIndividuationConfigByTenant();

    /**
     * 更新企业个性化配置
     * @param microTenantIndividuationConfig
     */
    void updateMicroTenantIndividuationConfig(MicroTenantIndividuationConfig microTenantIndividuationConfig);

}

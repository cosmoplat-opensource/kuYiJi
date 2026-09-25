/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenantIndividuationConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 企业个性化设置配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-04
 */
public interface MicroTenantIndividuationConfigMapper {
    /**
     * 查询企业个性化设置配置
     *
     * @param tenantCode 租户编码
     * @return 企业个性化设置配置
     */
    MicroTenantIndividuationConfig selectMicroTenantIndividuationConfigByTenant(@Param("tenantCode") String tenantCode);

    /**
     * 更新企业个性化配置
     * @param microTenantIndividuationConfig
     * @return
     */
    int updateMicroTenantIndividuationConfig(MicroTenantIndividuationConfig microTenantIndividuationConfig);

    /**
     * 新增企业个性化配置（配置记录不存在时首次保存用）
     * @param microTenantIndividuationConfig 配置
     * @param tenantCode 租户编码
     * @return
     */
    int insertMicroTenantIndividuationConfig(@Param("microTenantIndividuationConfig") MicroTenantIndividuationConfig microTenantIndividuationConfig, @Param("tenantCode") String tenantCode);

    /**
     * 幂等保存企业个性化配置（推荐使用）：
     * tenant_code 唯一键冲突时自动转更新，并发安全（替代"先查后插"，避免并发双插）
     * @param microTenantIndividuationConfig 配置
     * @param tenantCode 租户编码
     * @return
     */
    int upsertMicroTenantIndividuationConfig(@Param("microTenantIndividuationConfig") MicroTenantIndividuationConfig microTenantIndividuationConfig, @Param("tenantCode") String tenantCode);

}

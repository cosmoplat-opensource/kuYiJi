/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;

import java.util.List;

/**
 * 租户元数据 Service
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroTenantService {

    MicroTenant getByCode(String tenantCode); 

    List<MicroTenant> listActive(); 

    List<MicroTenant> list(MicroTenant query); 

    int insert(MicroTenant record); 

    int updateByCode(MicroTenant record); 

    /**
     * 解散租户：标记 status=0，清理用户-租户索引及 Redis 缓存
     *
     * @param tenantCode 租户编码
     * @param operatedBy 操作人手机号
     */
    void dissolve(String tenantCode, String operatedBy);
}

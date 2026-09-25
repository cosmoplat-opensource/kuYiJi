/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserTenantIndex;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-租户索引 Mapper
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroUserTenantIndexMapper {

    List<MicroUserTenantIndex> selectByPhone(@Param("phone") String phone); 

    MicroUserTenantIndex selectByPhoneAndCode(@Param("phone") String phone, 
                                              @Param("tenantCode") String tenantCode);

    int insert(MicroUserTenantIndex record); 

    int deleteByTenantCode(@Param("tenantCode") String tenantCode); 
}

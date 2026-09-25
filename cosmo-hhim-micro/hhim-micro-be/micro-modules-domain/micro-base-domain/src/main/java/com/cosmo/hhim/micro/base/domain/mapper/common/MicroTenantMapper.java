/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租户元数据 Mapper
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroTenantMapper {

    MicroTenant selectByCode(@Param("tenantCode") String tenantCode); 

    List<MicroTenant> selectActiveList(); 

    List<MicroTenant> selectList(MicroTenant query); 

    int insert(MicroTenant record); 

    int updateByCode(MicroTenant record); 
}

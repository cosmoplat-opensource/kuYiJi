/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应用配置 Mapper
 *
 * @author cosmo-hhim-open Team
 */
public interface MicroAppConfigMapper {

    MicroAppConfig selectByAppCode(@Param("appCode") String appCode); 

    List<MicroAppConfig> selectList(MicroAppConfig query); 

    int insert(MicroAppConfig record); 

    int updateById(MicroAppConfig record); 
}

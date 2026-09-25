/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppConfig;

import java.util.List;

/**
 * 应用配置 Service
 *
 * @author cosmo-hhim-open Team
 */
public interface IAppConfigService {

    MicroAppConfig getByAppCode(String appCode); 

    List<MicroAppConfig> list(MicroAppConfig query); 

    int insert(MicroAppConfig record); 

    int updateById(MicroAppConfig record); 
}

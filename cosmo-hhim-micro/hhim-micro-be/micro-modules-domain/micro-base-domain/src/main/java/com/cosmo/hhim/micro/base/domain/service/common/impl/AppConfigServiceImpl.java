/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppConfig;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroAppConfigMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IAppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 应用配置 Service 实现
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class AppConfigServiceImpl implements IAppConfigService {

    @Autowired
    private MicroAppConfigMapper appConfigMapper;

    @Override
    public MicroAppConfig getByAppCode(String appCode) {
        if (appCode == null || appCode.isEmpty()) {
            return null;
        }
        return appConfigMapper.selectByAppCode(appCode);
    }

    @Override
    public List<MicroAppConfig> list(MicroAppConfig query) {
        return appConfigMapper.selectList(query);
    }

    @Override
    public int insert(MicroAppConfig record) {
        return appConfigMapper.insert(record);
    }

    @Override
    public int updateById(MicroAppConfig record) {
        return appConfigMapper.updateById(record);
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserTenantIndex;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserTenantIndexMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IUserTenantIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户-租户索引 Service 实现
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class UserTenantIndexServiceImpl implements IUserTenantIndexService {

    @Autowired
    private MicroUserTenantIndexMapper userTenantIndexMapper;

    @Override
    public List<MicroUserTenantIndex> listByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userTenantIndexMapper.selectByPhone(phone);
    }

    @Override
    public MicroUserTenantIndex getByPhoneAndCode(String phone, String tenantCode) {
        if (phone == null || phone.isEmpty() || tenantCode == null || tenantCode.isEmpty()) {
            return null;
        }
        return userTenantIndexMapper.selectByPhoneAndCode(phone, tenantCode);
    }

    @Override
    public int insert(MicroUserTenantIndex record) {
        return userTenantIndexMapper.insert(record);
    }

    @Override
    public int deleteByTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isEmpty()) {
            return 0;
        }
        return userTenantIndexMapper.deleteByTenantCode(tenantCode);
    }
}

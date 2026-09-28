/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserTenantIndex;

import java.util.List;

/**
 * 用户-租户索引 Service
 *
 * @author cosmo-hhim-open Team
 */
public interface IUserTenantIndexService {

    List<MicroUserTenantIndex> listByPhone(String phone); 

    MicroUserTenantIndex getByPhoneAndCode(String phone, String tenantCode); 

    int insert(MicroUserTenantIndex record); 

    int deleteByTenantCode(String tenantCode); 
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.service;

import com.cosmo.hhim.external.mapper.HyzzThirdInterfaceMethodMappingMapper;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceMethodMapping;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ThirdInterfaceMethodMappingService {

    @Resource
    private HyzzThirdInterfaceMethodMappingMapper methodMappingMapper;

    public List<HyzzThirdInterfaceMethodMapping> getClientSupportList(String method, String version, String tenant) {
        return methodMappingMapper.selectClientSupportList(method, version, tenant);
    }
}

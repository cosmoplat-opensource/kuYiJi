/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.bean.BeanUtil;
import com.cosmo.hhim.micro.application.dto.base.MicroFactoryDTO;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team

 * @author cosmo-hhim-open Team
 * @version 1.0
 */
public class MicroFactoryAssembler {
    /**
     * @author cosmo-hhim-open Team
     * @return com.cosmo.hhim.micro.base.domain.entity.factory.MicroFactory
     **/
    public static MicroFactory convert2Condition(MicroFactoryDTO condition,String tenantCode) {
        MicroFactory queryParams = BeanUtil.copyProperties(condition,MicroFactory.class);
        queryParams.setTenantCode(tenantCode);
        return queryParams;
    }

    /**
     * @author cosmo-hhim-open Team
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroFactoryDTO>
     **/
    public static List<MicroFactoryDTO> convertDomains2DTOs(List<MicroFactory> factoryList,boolean isLazy) { 
        if (CollectionUtils.isNotEmpty(factoryList)){
            return factoryList.stream().map(factory->convertDomain2Dto(factory,isLazy)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.base.MicroFactoryDTO
     **/
    public static MicroFactoryDTO convertDomain2Dto(MicroFactory inParam, boolean isLazy) {
        MicroFactoryDTO result = new MicroFactoryDTO();
        result.setId(inParam.getId());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setLastUpdDate(inParam.getLastUpdDate());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setLastUpdBy(inParam.getLastUpdBy());
        if (!isLazy&&CollectionUtils.isNotEmpty(inParam.getWorkShopList())){
            result.setWorkShopList(MicroWorkShopAssembler.convertDomains2DTOs(inParam.getWorkShopList(),false));
        }
        return result;
    }

    public static MicroFactory convertDto2Domain(MicroFactoryDTO inParam, String tenantCode) {
        MicroFactory result = convertDto2Domain(inParam);
        result.setTenantCode(tenantCode);
        return result;
    }

    public static MicroFactory convertDto2Domain(MicroFactoryDTO inParam) {
        MicroFactory result = new MicroFactory();
        result.setId(inParam.getId());
        result.setRemark(inParam.getRemark());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setLastUpdBy(inParam.getLastUpdBy());
        result.setLastUpdDate(inParam.getLastUpdDate());
        return result;
    }
}

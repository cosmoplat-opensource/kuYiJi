/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.bean.BeanUtil;
import com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team

 * @author cosmo-hhim-open Team
 * @version 1.0
 */
public class MicroWorkShopAssembler {
    /**
     * @author cosmo-hhim-open Team
     * @param workShopList, isLazy
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO>
     **/
    public static List<MicroWorkShopDTO> convertDomains2DTOs4Page(List<MicroWorkShop> workShopList,boolean isLazy) { 
        if (CollectionUtils.isNotEmpty(workShopList)){
            List<MicroWorkShopDTO> targetList = convertDomains2DTOs(workShopList,isLazy);
            return MicroPageUtils.listToPage(workShopList, targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param workShopList, isLazy
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO>
     **/
    public static List<MicroWorkShopDTO> convertDomains2DTOs(List<MicroWorkShop> workShopList,boolean isLazy) { 
        if (CollectionUtils.isNotEmpty(workShopList)){
            return workShopList.stream().map(workShop->convertDomain2Dto(workShop,isLazy)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, isLazy
     * @return com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO
     **/
    public static MicroWorkShopDTO convertDomain2Dto(MicroWorkShop inParam, boolean isLazy) {
        MicroWorkShopDTO result = convertDomain2Dto(inParam);
        if (!isLazy&&CollectionUtils.isNotEmpty(inParam.getManufactureLineList())){
            result.setManufactureLineList(MicroManufactureLineAssembler.convertDomains2DTOs(inParam.getManufactureLineList()));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO
     **/
    public static MicroWorkShopDTO convertDomain2Dto(MicroWorkShop inParam) {
        MicroWorkShopDTO result = new MicroWorkShopDTO();
        result.setId(inParam.getId());
        result.setWshopCode(inParam.getWshopCode());
        result.setWshopName(inParam.getWshopName());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setLastUpdDate(inParam.getLastUpdDate());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setLastUpdBy(inParam.getLastUpdBy());
        return result;
    }

    public static MicroWorkShop convert2Condition(MicroWorkShopDTO condition) {
        MicroWorkShop params = BeanUtil.copyProperties(condition,MicroWorkShop.class);
        params.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        params.setSearchValue(condition.getKey());
        return params;
    }

    public static MicroWorkShop convertDto2Domain(MicroWorkShopDTO inParam, Long userId, String tenantCode) {
        MicroWorkShop result = convertDto2Domain(inParam);
        result.setTenantCode(tenantCode);
        result.setCreatedBy(String.valueOf(userId));
        return result;
    }

    public static MicroWorkShop convertDto2Domain(MicroWorkShopDTO inParam) {
        MicroWorkShop result = new MicroWorkShop();
        result.setId(inParam.getId());
        result.setWshopCode(inParam.getWshopCode());
        result.setWshopName(inParam.getWshopName());
        result.setRemark(inParam.getRemark());
        return result;
    }

    public static MicroWorkShop convert2Domain(String wshopName,Long userId,String tenantCode) {
        MicroWorkShop result = new MicroWorkShop();
        result.setWshopName(wshopName);
        result.setCreatedBy(String.valueOf(userId));
        result.setTenantCode(tenantCode);
        return result;
    }

    public static MicroWorkShop convertDto2Domain4Update(MicroWorkShopDTO inParam, Long userId) {
        MicroWorkShop result = convertDto2Domain(inParam);
        result.setLastUpdBy(String.valueOf(userId));
        return result;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
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
public class MicroManufactureLineAssembler {

    /**
     * @author cosmo-hhim-open Team
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO>
     **/
    public static List<MicroManufactureLineDTO> convertDomains2DTOs4Page(List<MicroManufactureLine> manufactureLineList) { 
        if (CollectionUtils.isNotEmpty(manufactureLineList)){
            List<MicroManufactureLineDTO> targetList = convertDomains2DTOs(manufactureLineList);
            return MicroPageUtils.listToPage(manufactureLineList,targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO>
     **/
    public static List<MicroManufactureLineDTO> convertDomains2DTOs(List<MicroManufactureLine> manufactureLineList) { 
        if (CollectionUtils.isNotEmpty(manufactureLineList)){
            return manufactureLineList.stream().map(manufactureLine->convertDomain2Dto(manufactureLine)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO
     **/
    public static MicroManufactureLineDTO convertDomain2Dto(MicroManufactureLine inParam) {
        MicroManufactureLineDTO result = new MicroManufactureLineDTO();
        result.setId(inParam.getId());
        result.setWshopCode(inParam.getWshopCode());
        result.setWshopName(inParam.getWshopName());
        result.setMlineCode(inParam.getMlineCode());
        result.setMlineName(inParam.getMlineName());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setLastUpdDate(inParam.getLastUpdDate());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setLastUpdBy(inParam.getLastUpdBy());
        return result;
    }

    public static MicroManufactureLine convert2Condition(MicroManufactureLineDTO condition) {
        MicroManufactureLine manufactureLine = BeanUtil.copyProperties(condition,MicroManufactureLine.class);
        manufactureLine.setSearchValue(condition.getKey());
        manufactureLine.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        return manufactureLine;
    }

    public static MicroManufactureLine convertDto2Domain(MicroManufactureLineDTO inParam,String tenantCode) {
        MicroManufactureLine result = convertDto2Domain(inParam);
        result.setTenantCode(tenantCode);
        return result;
    }

    public static MicroManufactureLine convertDto2Domain(MicroManufactureLineDTO inParam) {
        MicroManufactureLine result = new MicroManufactureLine();
        result.setId(inParam.getId());
        result.setWshopCode(inParam.getWshopCode());
        result.setWshopName(inParam.getWshopName());
        result.setMlineCode(inParam.getMlineCode());
        result.setMlineName(inParam.getMlineName());
        result.setRemark(inParam.getRemark());
        return result;
    }

    public static MicroManufactureLine convert2Domain(String wshopCode, String wshopName, String mlineName, Long userId, String tenantCode) {
        MicroManufactureLine result = new MicroManufactureLine();
        result.setWshopCode(wshopCode);
        result.setWshopName(wshopName);
        result.setMlineName(mlineName);
        result.setTenantCode(tenantCode);
        result.setCreatedBy(String.valueOf(userId));
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine
     **/
    public static MicroManufactureLine convertDto2Domain4Insert(MicroManufactureLineDTO inParam, Long userId, String tenantCode) {
        MicroManufactureLine result = convertDto2Domain(inParam);
        if (ObjectUtil.isNotEmpty(userId)){
            result.setCreatedBy(String.valueOf(userId));
            result.setLastUpdBy(String.valueOf(userId));
        }
        result.setTenantCode(tenantCode);
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine
     **/
    public static MicroManufactureLine convertDto2Domain4Update(MicroManufactureLineDTO inParam, Long userId) {
        MicroManufactureLine result = convertDto2Domain(inParam);
        if (ObjectUtil.isNotEmpty(userId)){
            result.setLastUpdBy(String.valueOf(userId));
        }
        return result;
    }
}

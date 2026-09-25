/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO;
import com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer;
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
public class MicroCustomerAssembler {


    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer
     **/
    public static MicroCustomer convertDto2Domain(MicroCustomerDTO inParam) {
        MicroCustomer result = new MicroCustomer();
        result.setId(inParam.getId());
        result.setCustomerCode(inParam.getCustomerCode());
        result.setCustomerName(inParam.getCustomerName());
        result.setCustomerShortName(inParam.getCustomerShortName());
        result.setRemark(inParam.getRemark());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param customers
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO>
     **/
    public static List<MicroCustomerDTO> convertDomains2DTOs4Page(List<MicroCustomer> customers) { 
        if (CollectionUtils.isNotEmpty(customers)){
            List<MicroCustomerDTO> targetList = convertDomains2DTOs(customers);
            return MicroPageUtils.listToPage(customers, targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param customers
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO>
     **/
    public static List<MicroCustomerDTO> convertDomains2DTOs(List<MicroCustomer> customers) { 
        if (CollectionUtils.isNotEmpty(customers)){
            return customers.stream().map(customer -> convertDomain2Dto(customer)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO
     **/
    public static MicroCustomerDTO convertDomain2Dto(MicroCustomer inParam) {
        MicroCustomerDTO result = new MicroCustomerDTO();
        result.setId(inParam.getId());
        result.setCustomerCode(inParam.getCustomerCode());
        result.setCustomerName(inParam.getCustomerName());
        result.setCustomerShortName(inParam.getCustomerShortName());
        result.setRemark(inParam.getRemark());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setUpdatedDate(inParam.getUpdatedDate());
        result.setCreateBy(inParam.getCreateBy());
        result.setUpdateBy(inParam.getUpdateBy());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param condition
     * @return com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer
     **/
    public static MicroCustomer convert2Condition(MicroCustomerDTO condition) {
        MicroCustomer queryParam = BeanUtil.copyProperties(condition,MicroCustomer.class);
        queryParam.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        queryParam.setSearchValue(condition.getKey());
        return queryParam;
    }

    public static MicroCustomer convert2Customer4Insert(String owName, Long userId, String tenantCode) {
        MicroCustomer result = new MicroCustomer();
        result.setCustomerName(owName);
        result.setTenantCode(tenantCode);
        result.setCreateBy(String.valueOf(userId));
        result.setCreatedDate(DateUtils.getNowDate());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer
     **/
    public static MicroCustomer convertDto2Domain4Insert(MicroCustomerDTO inParam, Long userId, String tenantCode) {
        MicroCustomer result = convertDto2Domain(inParam);
        if (ObjectUtil.isNotEmpty(userId)){
            result.setCreateBy(String.valueOf(userId));
            result.setUpdateBy(String.valueOf(userId));
        }
        result.setTenantCode(tenantCode);
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer
     **/
    public static MicroCustomer convertDto2Domain4Update(MicroCustomerDTO inParam, Long userId) {
        MicroCustomer result = convertDto2Domain(inParam);
        if (ObjectUtil.isNotEmpty(userId)){
            result.setUpdateBy(String.valueOf(userId));
        }
        return result;
    }
}

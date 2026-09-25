/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.CustomFieldTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team

 * @author cosmo-hhim-open Team
 * @version 1.0
 */
public class MicroExtendFieldAssembler {

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO
     **/
    public static MicroExtendFieldDTO convertDomain2DTO(MicroExtendField inParam) {
        MicroExtendFieldDTO result = new MicroExtendFieldDTO();
        result.setActiveFlag(inParam.getActiveFlag());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setExtField(inParam.getExtField());
        result.setExtFieldLabel(inParam.getExtFieldLabel());
        result.setFieldType(inParam.getFieldType());
        result.setId(inParam.getId());
        result.setLastUpdBy(inParam.getLastUpdBy());
        result.setLastUpdDate(inParam.getLastUpdDate());
        if (StringUtils.hasText(inParam.getExtFieldOption())){
            String[] options = inParam.getExtFieldOption().split("#");
            List<String> optionList = new ArrayList<>(options.length);
            result.setOptionalRange(optionList);
            for (String option : options){
                optionList.add(option);
            }
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField
     **/
    public static MicroExtendField convert2Condition(MicroExtendFieldDTO inParam) {
        MicroExtendField result = BeanUtil.copyProperties(inParam,MicroExtendField.class);
        result.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param extendFields
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO>
     **/
    public static List<MicroExtendFieldDTO> convertDomains2DTOs4Page(List<MicroExtendField> extendFields) { 
        if (CollectionUtil.isNotEmpty(extendFields)){
            List<MicroExtendFieldDTO> targetList = convertDomains2DTOs(extendFields);
            return MicroPageUtils.listToPage(extendFields,targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param extendFields
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO>
     **/
    public static List<MicroExtendFieldDTO> convertDomains2DTOs(List<MicroExtendField> extendFields) { 
        if (CollectionUtil.isNotEmpty(extendFields)){
            return extendFields.stream().map(extendField -> convertDomain2DTO(extendField)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField
     **/
    public static MicroExtendField convertDTO2Domain4Insert(MicroExtendFieldDTO inParam,Long userId,String tenantCode) { 
        MicroExtendField result = convertDTO2Domain(inParam);
        result.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        if (ObjectUtil.isNotEmpty(userId)){
            result.setCreatedBy(String.valueOf(userId));
            result.setLastUpdBy(String.valueOf(userId));
        }
        result.setTenantCode(tenantCode);
        StringBuffer options = new StringBuffer();
        if(CustomFieldTypeEnum.CUSTOM_FIELD_TYPE_SINGLE.getCode().equals(inParam.getFieldType()) || CustomFieldTypeEnum.CUSTOM_FIELD_TYPE_MULTIPLY.getCode().equals(inParam.getFieldType())){
            if(CollectionUtil.isNotEmpty(inParam.getOptionalRange())){
                for (String option : inParam.getOptionalRange()) {
                    options = options.append(option).append("#");
                }
                result.setExtFieldOption(options.substring(0,options.length()-1));
            }
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField
     **/
    public static MicroExtendField convertDTO2Domain4Update(MicroExtendFieldDTO inParam, Long userId) { 
        MicroExtendField result = convertDTO2Domain(inParam);
        if (ObjectUtil.isNotEmpty(userId)){
            result.setLastUpdBy(String.valueOf(userId));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField
     **/
    private static MicroExtendField convertDTO2Domain(MicroExtendFieldDTO inParam) { 
        MicroExtendField result = new MicroExtendField();
        result.setActiveFlag(inParam.getActiveFlag());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setExtField(inParam.getExtField());
        result.setExtFieldLabel(inParam.getExtFieldLabel());
        result.setFieldType(inParam.getFieldType());
        result.setId(inParam.getId());
        result.setLastUpdBy(inParam.getLastUpdBy());
        result.setLastUpdDate(inParam.getLastUpdDate());
        return result;
    }


    public static MicroExtendField convert2Domain(String extFieldLabel, String fieldType, String businessCode) {
        MicroExtendField result = new MicroExtendField();
        result.setExtFieldLabel(extFieldLabel);
        result.setFieldType(fieldType);
        return result;
    }
}

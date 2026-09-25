/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
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
public class MicroExtendFieldRelationAssembler {

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO
     **/
    public static MicroExtendFieldRelationDTO convertDomain2DTO(MicroExtendFieldRelation inParam) {
        MicroExtendFieldRelationDTO result = new MicroExtendFieldRelationDTO();
        result.setBusinessCode(inParam.getBusinessCode());
        result.setExtFieldId(inParam.getExtFieldId());
        result.setId(inParam.getId());
        result.setExtField(inParam.getExtField());
        result.setExtFieldLabel(inParam.getExtFieldLabel());
        result.setFieldType(inParam.getFieldType());
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
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation
     **/
    public static MicroExtendFieldRelation convert2Condition(MicroExtendFieldRelationDTO inParam) {
        MicroExtendFieldRelation result = new MicroExtendFieldRelation();
        result.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        result.setBusinessCode(inParam.getBusinessCode());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microExtendFieldRelations
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO>
     **/
    public static List<MicroExtendFieldRelationDTO> convertDomains2DTOs(List<MicroExtendFieldRelation> microExtendFieldRelations) { 
        if (CollectionUtil.isNotEmpty(microExtendFieldRelations)){
            return microExtendFieldRelations.stream().map(microExtendFieldRelation -> convertDomain2DTO(microExtendFieldRelation)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microExtendFieldRelations
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO>
     **/
    public static List<MicroExtendFieldRelationDTO> convertDomains2DTOs4Page(List<MicroExtendFieldRelation> microExtendFieldRelations) { 
        if (CollectionUtil.isNotEmpty(microExtendFieldRelations)){
            List<MicroExtendFieldRelationDTO> targetList = convertDomains2DTOs(microExtendFieldRelations);
            MicroPageUtils.listToPage(microExtendFieldRelations,targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation
     **/
    public static MicroExtendFieldRelation convertDTO2Domain(MicroExtendFieldRelationDTO inParam) { 
        MicroExtendFieldRelation result = new MicroExtendFieldRelation();
        result.setBusinessCode(inParam.getBusinessCode());
        result.setExtFieldId(inParam.getExtFieldId());
        result.setId(inParam.getId());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation
     **/
    public static MicroExtendFieldRelation convertDTO2Domain4Insert(MicroExtendFieldRelationDTO inParam,Long userId,String tenantCode) { 
        MicroExtendFieldRelation result = convertDTO2Domain(inParam);
        if (ObjectUtil.isNotNull(userId)){
            result.setCreatedBy(String.valueOf(userId));
            result.setLastUpdBy(String.valueOf(userId));
        }
        result.setCreatedDate(DateUtils.getNowDate());
        result.setLastUpdDate(DateUtils.getNowDate());
        result.setTenantCode(tenantCode);
        result.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation
     **/
    public static MicroExtendFieldRelation convertDTO2Domain4Update(MicroExtendFieldRelationDTO inParam, Long userId) { 
        MicroExtendFieldRelation result = convertDTO2Domain(inParam);
        if (ObjectUtil.isNotNull(userId)){
            result.setLastUpdBy(String.valueOf(userId));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param extendFieldRelationList, userId, tenantCode
     * @return java.util.List<com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation>
     **/
    public static List<MicroExtendFieldRelation> convertDTOS2Domains4Insert(List<MicroExtendFieldRelationDTO> extendFieldRelationList, Long userId, String tenantCode) { 
        if (CollectionUtil.isNotEmpty(extendFieldRelationList)){
            return extendFieldRelationList.stream().map(extendFieldRelation->convertDTO2Domain4Insert(extendFieldRelation,userId,tenantCode)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

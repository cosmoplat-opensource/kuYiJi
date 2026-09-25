/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroExtendFieldRelationAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroExtendFieldRelationFacadeService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExtendFieldRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 扩展字段关系Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroExtendFieldRelationFacadeServiceImpl implements IMicroExtendFieldRelationFacadeService
{
    @Autowired
    private IMicroExtendFieldRelationService microExtendFieldRelationService;

    /**
     * 查询扩展字段关系
     * 
     * @param id 扩展字段关系ID
     * @return 扩展字段关系
     */
    @Override
    public MicroExtendFieldRelationDTO selectMicroExtendFieldRelationById(Long id)
    {
        return MicroExtendFieldRelationAssembler.convertDomain2DTO(microExtendFieldRelationService.selectMicroExtendFieldRelationById(id));
    }

    /**
     * 查询扩展字段关系列表
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 扩展字段关系
     */
    @Override
    public List<MicroExtendFieldRelationDTO> selectMicroExtendFieldRelationList(MicroExtendFieldRelationDTO microExtendFieldRelation)
    {
        return MicroExtendFieldRelationAssembler.convertDomains2DTOs4Page(microExtendFieldRelationService.selectMicroExtendFieldRelationList(MicroExtendFieldRelationAssembler.convert2Condition(microExtendFieldRelation)));
    }

    /**
     * 新增扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    @Override
    public int insertMicroExtendFieldRelation(MicroExtendFieldRelationDTO microExtendFieldRelation)
    {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        return microExtendFieldRelationService.insertMicroExtendFieldRelation(MicroExtendFieldRelationAssembler.convertDTO2Domain4Insert(microExtendFieldRelation,userId,tenantCode));
    }

    /**
     * 修改扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    @Override
    public int updateMicroExtendFieldRelation(MicroExtendFieldRelationDTO microExtendFieldRelation)
    {
        return microExtendFieldRelationService.updateMicroExtendFieldRelation(MicroExtendFieldRelationAssembler.convertDTO2Domain4Update(microExtendFieldRelation,SecurityUtils.getUserId()));
    }

    /**
     * 批量删除扩展字段关系
     * 
     * @param ids 需要删除的扩展字段关系ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldRelationByIds(Long[] ids)
    {
        return microExtendFieldRelationService.deleteMicroExtendFieldRelationByIds(ids);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microExtendFieldRelation
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO>
     **/
    @Override
    public List<MicroExtendFieldRelationDTO> selectMicroExtendFieldRelationListByCondition(MicroExtendFieldRelationDTO microExtendFieldRelation) {
        return MicroExtendFieldRelationAssembler.convertDomains2DTOs(microExtendFieldRelationService.selectMicroExtendFieldRelationList(MicroExtendFieldRelationAssembler.convert2Condition(microExtendFieldRelation)));
    }
}

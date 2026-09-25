/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroExtendFieldAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroExtendFieldFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField;
import com.cosmo.hhim.micro.base.domain.service.custom.IMicroExtendFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 扩展字段Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroExtendFieldFacadeServiceImpl implements IMicroExtendFieldFacadeService
{
    @Autowired
    private IMicroExtendFieldService microExtendFieldService;

    /**
     * 查询扩展字段
     * 
     * @param id 扩展字段ID
     * @return 扩展字段
     */
    @Override
    public MicroExtendFieldDTO selectMicroExtendFieldById(Long id)
    {
        return MicroExtendFieldAssembler.convertDomain2DTO(microExtendFieldService.selectMicroExtendFieldById(id));
    }

    /**
     * 查询扩展字段列表 分页
     * 
     * @param microExtendField 扩展字段
     * @return 扩展字段
     */
    @Override
    public List<MicroExtendFieldDTO> selectMicroExtendFieldList(MicroExtendFieldDTO microExtendField)
    {
        MicroExtendField condition = MicroExtendFieldAssembler.convert2Condition(microExtendField);
        List<MicroExtendField> extendFields = microExtendFieldService.selectMicroExtendFieldList(condition);
        return MicroExtendFieldAssembler.convertDomains2DTOs4Page(extendFields);
    }

    /**
     * 新增扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    @Override
    public MicroExtendFieldDTO insertMicroExtendField(MicroExtendFieldDTO microExtendField)
    {
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroExtendField result = microExtendFieldService.saveMicroExtendField(MicroExtendFieldAssembler.convertDTO2Domain4Insert(microExtendField, SecurityUtils.getUserId(),tenantCode));
        return MicroExtendFieldAssembler.convertDomain2DTO(result);
    }


    /**
     * 修改扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    @Override
    public int updateMicroExtendField(MicroExtendFieldDTO microExtendField)
    {
        return microExtendFieldService.updateMicroExtendField(MicroExtendFieldAssembler.convertDTO2Domain4Update(microExtendField, SecurityUtils.getUserId()));
    }

    /**
     * 批量删除扩展字段
     * 
     * @param ids 需要删除的扩展字段ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldByIds(Long[] ids)
    {
        return microExtendFieldService.deleteMicroExtendFieldByIds(ids);
    }

    /**
     * 删除扩展字段信息
     * 
     * @param id 扩展字段ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldById(Long id)
    {
        return microExtendFieldService.deleteMicroExtendFieldById(id);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microExtendField
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO>
     **/
    @Override
    public List<MicroExtendFieldDTO> selectMicroExtendFieldListByCondition(MicroExtendFieldDTO microExtendField) {
        MicroExtendField condition = MicroExtendFieldAssembler.convert2Condition(microExtendField);
        List<MicroExtendField> extendFields = microExtendFieldService.selectMicroExtendFieldList(condition);
        return MicroExtendFieldAssembler.convertDomains2DTOs(extendFields);
    }
}

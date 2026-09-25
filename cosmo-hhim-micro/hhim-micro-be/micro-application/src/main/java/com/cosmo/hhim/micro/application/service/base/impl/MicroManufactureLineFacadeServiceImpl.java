/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroManufactureLineAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroManufactureLineFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroManufactureLineService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 生产线基础信息Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Service
public class MicroManufactureLineFacadeServiceImpl implements IMicroManufactureLineFacadeService
{

    @Autowired
    private IMicroManufactureLineService manufactureLineService;
    /**
     * 查询生产线基础信息
     *
     * @param id 生产线基础信息ID
     * @return 生产线基础信息
     */
    @Override
    public MicroManufactureLineDTO selectMicroManufactureLineById(Long id)
    {
        MicroManufactureLine manufactureLine =  manufactureLineService.selectMicroManufactureLineById(id);
        return Objects.nonNull(manufactureLine)? MicroManufactureLineAssembler.convertDomain2Dto(manufactureLine):null;
    }

    /**
     * 查询生产线基础信息列表
     *
     * @param condition 生产线基础信息
     * @return 生产线基础信息
     */
    @Override
    public List<MicroManufactureLineDTO> selectMicroManufactureLineList(MicroManufactureLineDTO condition)
    {
        MicroManufactureLine queryParams = MicroManufactureLineAssembler.convert2Condition(condition);
        List<MicroManufactureLine> manufactureLines = manufactureLineService.selectMicroManufactureLineList(queryParams);
        return MicroManufactureLineAssembler.convertDomains2DTOs4Page(manufactureLines);
    }

    /**
     * 新增生产线基础信息
     *
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    @Override
    public int insertMicroManufactureLine(MicroManufactureLineDTO microManufactureLine)
    {
        if (!manufactureLineService.isUniqueLineNameByWorkShop(microManufactureLine.getWshopCode(),microManufactureLine.getMlineName(),null)){
            throw new CustomException("当前车间下，产线【"+microManufactureLine.getMlineName()+"】已存在");
        }
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        return manufactureLineService.insertMicroManufactureLine(MicroManufactureLineAssembler.convertDto2Domain4Insert(microManufactureLine,SecurityUtils.getUserId(),tenantCode));
    }

    /**
     * 修改生产线基础信息
     *
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    @Override
    public int updateMicroManufactureLine(MicroManufactureLineDTO microManufactureLine)
    {
        if (!manufactureLineService.isUniqueLineNameByWorkShop(microManufactureLine.getWshopCode(),microManufactureLine.getMlineName(),microManufactureLine.getId())){
            throw new CustomException("当前车间下，产线【"+microManufactureLine.getMlineName()+"】已存在");
        }
        MicroManufactureLine manufactureLine = MicroManufactureLineAssembler.convertDto2Domain4Update(microManufactureLine,SecurityUtils.getUserId());
        return manufactureLineService.updateMicroManufactureLine(manufactureLine);
    }

    /**
     * 批量删除生产线基础信息
     *
     * @param ids 需要删除的生产线基础信息ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureLineByIds(Long[] ids)
    {
        return manufactureLineService.deleteMicroManufactureLineByIds(ids);
    }
}

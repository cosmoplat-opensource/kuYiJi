/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroFactoryAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroFactoryDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroFactoryFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroFactory;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroFactoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 工厂基础信息Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Service
public class MicroFactoryFacadeServiceImpl implements IMicroFactoryFacadeService
{

    @Autowired
    private IMicroFactoryService microFactoryService;

    /**
     * 查询工厂基础信息
     * 
     * @param id 工厂基础信息ID
     * @return 工厂基础信息
     */
    @Override
    public MicroFactoryDTO selectMicroFactoryById(Long id)
    {
        return null;
    }

    /**
     * 查询工厂基础信息列表
     * 
     * @param condition 工厂基础信息
     * @param isLazy 是否懒加载子节点
     * @return 工厂基础信息
     */
    @Override
    public List<MicroFactoryDTO> selectMicroFactoryList(MicroFactoryDTO condition,boolean isLazy)
    {
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroFactory factoryParams = MicroFactoryAssembler.convert2Condition(condition,tenantCode);
        List<MicroFactory> factoryList = microFactoryService.selectMicroFactoryList(factoryParams,isLazy);
        if (CollectionUtils.isNotEmpty(factoryList)){
            //不是懒加载，持续加载车间和产线
            if (!isLazy){
                return MicroFactoryAssembler.convertDomains2DTOs(factoryList,false);
            }
            return MicroFactoryAssembler.convertDomains2DTOs(factoryList,true);
        }
        return Collections.emptyList();
    }

    /**
     * 新增工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    @Override
    public int insertMicroFactory(MicroFactoryDTO microFactory)
    {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroFactory factory = MicroFactoryAssembler.convertDto2Domain(microFactory,tenantCode);
        factory.setCreatedBy(String.valueOf(userId));
        return microFactoryService.insertMicroFactory(factory);
    }

    /**
     * 修改工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    @Override
    public int updateMicroFactory(MicroFactoryDTO microFactory)
    {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroFactory factory = MicroFactoryAssembler.convertDto2Domain(microFactory,tenantCode);
        factory.setCreatedBy(String.valueOf(userId));
        return microFactoryService.insertMicroFactory(factory);
    }

    /**
     * 批量删除工厂基础信息
     * 
     * @param ids 需要删除的工厂基础信息ID
     * @return 结果
     */
    @Override
    public int deleteMicroFactoryByIds(Long[] ids)
    {
        return microFactoryService.deleteMicroFactoryByIds(ids);
    }
}

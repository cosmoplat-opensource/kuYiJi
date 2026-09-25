/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.factory.impl;

import com.cosmo.hhim.micro.base.domain.entity.factory.MicroFactory;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroFactoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroManufactureLineMapper;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroWorkShopMapper;
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
 */
@Service
public class MicroFactoryServiceImpl implements IMicroFactoryService
{
    @Autowired
    private MicroFactoryMapper microFactoryMapper;
    @Autowired
    private MicroWorkShopMapper microWorkShopMapper;
    @Autowired
    private MicroManufactureLineMapper microManufactureLineMapper;

    /**
     * 查询工厂基础信息
     * 
     * @param id 工厂基础信息ID
     * @return 工厂基础信息
     */
    @Override
    public MicroFactory selectMicroFactoryById(Long id)
    {
        return microFactoryMapper.selectMicroFactoryById(id);
    }

    /**
     * 查询工厂基础信息列表
     * 
     * @param microFactory 工厂基础信息
     * @return 工厂基础信息
     */
    @Override
    public List<MicroFactory> selectMicroFactoryList(MicroFactory microFactory)
    {
        return microFactoryMapper.selectMicroFactoryList(microFactory);
    }

    /**
     * 新增工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    @Override
    public int insertMicroFactory(MicroFactory microFactory)
    {
        return microFactoryMapper.insertMicroFactory(microFactory);
    }

    /**
     * 修改工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    @Override
    public int updateMicroFactory(MicroFactory microFactory)
    {
        return microFactoryMapper.updateMicroFactory(microFactory);
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
        return microFactoryMapper.deleteMicroFactoryByIds(ids);
    }

    /**
     * 删除工厂基础信息信息
     * 
     * @param id 工厂基础信息ID
     * @return 结果
     */
    @Override
    public int deleteMicroFactoryById(Long id)
    {
        return microFactoryMapper.deleteMicroFactoryById(id);
    }

    /**
     * 加载工厂车间产线树
     *
     * @author cosmo-hhim-open Team
     * @param factoryParams
     * @param isLazy
     * @return 工厂列表
     **/
    @Override
    public List<MicroFactory> selectMicroFactoryList(MicroFactory factoryParams, boolean isLazy) {
        List<MicroFactory> factoryList = this.selectMicroFactoryList(factoryParams);
        if (CollectionUtils.isNotEmpty(factoryList)){
            if (!isLazy){
                factoryList.stream().forEach(factory -> loadWorkShops4Factory(factory));
            }
            return factoryList;
        }
        return Collections.emptyList();
    }

    /**
     * 加载工厂下的车间列表
     *
     * @author cosmo-hhim-open Team
     * @param factory
     **/
    private void loadWorkShops4Factory(MicroFactory factory) {
        MicroWorkShop workShopParams = new MicroWorkShop();
        workShopParams.setTenantCode(factory.getTenantCode());
        List<MicroWorkShop> workShops = microWorkShopMapper.selectMicroWorkshopList(workShopParams);
        if (CollectionUtils.isNotEmpty(workShops)){
            workShops.stream().forEach(workShop -> loadManufactureLines4WorkShop(workShop));
        }
        factory.setWorkShopList(workShops);
    }

    /**
     * 加载车间下的产线列表
     *
     * @author cosmo-hhim-open Team
     * @param workShop
     **/
    private void loadManufactureLines4WorkShop(MicroWorkShop workShop) {
        MicroManufactureLine lineParams = new MicroManufactureLine();
        lineParams.setWshopCode(workShop.getWshopCode());
        lineParams.setTenantCode(workShop.getTenantCode());
        List<MicroManufactureLine> microManufactureLines = microManufactureLineMapper.selectMicroManufactureLineList(lineParams);
        workShop.setManufactureLineList(microManufactureLines);
    }
}

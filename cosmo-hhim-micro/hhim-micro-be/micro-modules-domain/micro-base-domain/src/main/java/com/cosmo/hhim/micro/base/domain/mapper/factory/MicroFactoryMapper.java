/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.factory;

import com.cosmo.hhim.micro.base.domain.entity.factory.MicroFactory;

import java.util.List;

/**
 * 工厂基础信息Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface MicroFactoryMapper 
{
    /**
     * 查询工厂基础信息
     * 
     * @param id 工厂基础信息ID
     * @return 工厂基础信息
     */
    MicroFactory selectMicroFactoryById(Long id);

    /**
     * 查询工厂基础信息列表
     * 
     * @param microFactory 工厂基础信息
     * @return 工厂基础信息集合
     */
    List<MicroFactory> selectMicroFactoryList(MicroFactory microFactory);

    /**
     * 新增工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    int insertMicroFactory(MicroFactory microFactory);

    /**
     * 修改工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    int updateMicroFactory(MicroFactory microFactory);

    /**
     * 删除工厂基础信息
     * 
     * @param id 工厂基础信息ID
     * @return 结果
     */
    int deleteMicroFactoryById(Long id);

    /**
     * 批量删除工厂基础信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroFactoryByIds(Long[] ids);
}

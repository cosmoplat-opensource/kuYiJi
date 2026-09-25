/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroFactoryDTO;

import java.util.List;

/**
 * 工厂基础信息Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface IMicroFactoryFacadeService
{
    /**
     * 查询工厂基础信息
     * 
     * @param id 工厂基础信息ID
     * @return 工厂基础信息
     */
    MicroFactoryDTO selectMicroFactoryById(Long id);

    /**
     * 查询工厂基础信息列表
     * 
     * @param condition 筛选条件
     * @return 工厂基础信息集合
     */
    List<MicroFactoryDTO> selectMicroFactoryList(MicroFactoryDTO condition,boolean isLazy); 

    /**
     * 新增工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    int insertMicroFactory(MicroFactoryDTO microFactory);

    /**
     * 修改工厂基础信息
     * 
     * @param microFactory 工厂基础信息
     * @return 结果
     */
    int updateMicroFactory(MicroFactoryDTO microFactory);

    /**
     * 批量删除工厂基础信息
     * 
     * @param ids 需要删除的工厂基础信息ID
     * @return 结果
     */
    int deleteMicroFactoryByIds(Long[] ids);
}

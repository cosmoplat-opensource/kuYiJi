/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.physicaModel.impl;

import java.util.List;

import com.cosmo.hhim.thirdplat.api.operation.domain.PhysicalModel;
import com.cosmo.hhim.thirdplat.web.mapper.PhysicalModelMapper;
import com.cosmo.hhim.thirdplat.web.service.physicaModel.IPhysicalModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * iot 物模型Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2024-01-29
 */
@Service
public class PhysicalModelServiceImpl implements IPhysicalModelService
{
    @Autowired
    private PhysicalModelMapper physicalModelMapper;

    /**
     * 查询iot 物模型
     * 
     * @param deviceCode iot 物模型ID
     * @return iot 物模型
     */
    @Override
    public PhysicalModel selectPhysicalModelById(String deviceCode)
    {
        return physicalModelMapper.selectPhysicalModelById(deviceCode);
    }

    /**
     * 查询iot 物模型列表
     * 
     * @param physicalModel iot 物模型
     * @return iot 物模型
     */
    @Override
    public List<PhysicalModel> selectPhysicalModelList(PhysicalModel physicalModel)
    {
        return physicalModelMapper.selectPhysicalModelList(physicalModel);
    }

    /**
     * 新增iot 物模型
     * 
     * @param physicalModel iot 物模型
     * @return 结果
     */
    @Override
    public int insertPhysicalModel(PhysicalModel physicalModel)
    {
        return physicalModelMapper.insertPhysicalModel(physicalModel);
    }

    /**
     * 修改iot 物模型
     * 
     * @param physicalModel iot 物模型
     * @return 结果
     */
    @Override
    public int updatePhysicalModel(PhysicalModel physicalModel)
    {
        return physicalModelMapper.updatePhysicalModel(physicalModel);
    }

    /**
     * 批量删除iot 物模型
     * 
     * @param deviceCodes 需要删除的iot 物模型ID
     * @return 结果
     */
    @Override
    public int deletePhysicalModelByIds(String[] deviceCodes)
    {
        return physicalModelMapper.deletePhysicalModelByIds(deviceCodes);
    }

    /**
     * 删除iot 物模型信息
     * 
     * @param deviceCode iot 物模型ID
     * @return 结果
     */
    @Override
    public int deletePhysicalModelById(String deviceCode)
    {
        return physicalModelMapper.deletePhysicalModelById(deviceCode);
    }
}

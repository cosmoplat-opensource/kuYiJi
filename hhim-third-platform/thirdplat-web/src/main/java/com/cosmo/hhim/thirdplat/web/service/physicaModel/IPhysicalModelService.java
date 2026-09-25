/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.physicaModel;

import com.cosmo.hhim.thirdplat.api.operation.domain.PhysicalModel;

import java.util.List;

/**
 * iot 物模型Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2024-01-29
 */
public interface IPhysicalModelService 
{
    /**
     * 查询iot 物模型
     * 
     * @param deviceCode iot 物模型ID
     * @return iot 物模型
     */
    public PhysicalModel selectPhysicalModelById(String deviceCode);

    /**
     * 查询iot 物模型列表
     * 
     * @param physicalModel iot 物模型
     * @return iot 物模型集合
     */
    public List<PhysicalModel> selectPhysicalModelList(PhysicalModel physicalModel);

    /**
     * 新增iot 物模型
     * 
     * @param physicalModel iot 物模型
     * @return 结果
     */
    public int insertPhysicalModel(PhysicalModel physicalModel);

    /**
     * 修改iot 物模型
     * 
     * @param physicalModel iot 物模型
     * @return 结果
     */
    public int updatePhysicalModel(PhysicalModel physicalModel);

    /**
     * 批量删除iot 物模型
     * 
     * @param deviceCodes 需要删除的iot 物模型ID
     * @return 结果
     */
    public int deletePhysicalModelByIds(String[] deviceCodes);

    /**
     * 删除iot 物模型信息
     * 
     * @param deviceCode iot 物模型ID
     * @return 结果
     */
    public int deletePhysicalModelById(String deviceCode);
}

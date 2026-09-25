/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.manufacture;

import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;

import java.util.List;
import java.util.Map;

/**
 * 投料单/退料单Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-10
 */
public interface IMicroPickMaterialsService 
{
    /**
     * 查询投料单/退料单
     *
     * @param id 投料单/退料单ID
     * @return 投料单/退料单
     */
    MicroPickMaterials selectMicroPickMaterialsById(Long id);

    /**
     * 查询投料单/退料单列表
     *
     * @param microPickMaterials 投料单/退料单
     * @return 投料单/退料单集合
     */
    List<MicroPickMaterials> selectMicroPickMaterialsList(MicroPickMaterials microPickMaterials);

    /**
     * 新增投料单/退料单
     *
     * @param microPickMaterials 投料单/退料单
     * @return 结果
     */
    int insertMicroPickMaterials(MicroPickMaterials microPickMaterials);

    /**
     * 修改投料单/退料单
     *
     * @param microPickMaterials 投料单/退料单
     * @return 结果
     */
    int updateMicroPickMaterials(MicroPickMaterials microPickMaterials);

    /**
     * 批量删除投料单/退料单
     *
     * @param ids 需要删除的投料单/退料单ID
     * @return 结果
     */
    int deleteMicroPickMaterialsByIds(Long[] ids);

    /**
     * 删除投料单/退料单信息
     *
     * @param id 投料单/退料单ID
     * @return 结果
     */
    int deleteMicroPickMaterialsById(Long id);

    /**
     * 查询投料单/退料单列表
     *
     * @param microPickMaterials 投料单/退料单
     * @return 投料单/退料单
     */
    List<MicroPickMaterials> getListByWorkOrder(MicroPickMaterials microPickMaterials);

    /**
     * 根据工单+物料获取对应投退料数量总和
     *
     * @param materialDemandList
     * @return
     */
    Map<String, MicroPickMaterials> selectMaterialShortageInfo(List<MicroManufactureWorkOrderMaterialEntity> materialDemandList);
}

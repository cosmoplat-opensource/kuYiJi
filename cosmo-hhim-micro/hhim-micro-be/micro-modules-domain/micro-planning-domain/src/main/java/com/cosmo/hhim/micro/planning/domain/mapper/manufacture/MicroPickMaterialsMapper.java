/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.mapper.manufacture;

import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投料单/退料单Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-10
 */
public interface MicroPickMaterialsMapper 
{
    /**
     * 查询投料单/退料单
     * 
     * @param id 投料单/退料单ID
     * @return 投料单/退料单
     */
    public MicroPickMaterials selectMicroPickMaterialsById(Long id);

    /**
     * 查询投料单/退料单列表
     * 
     * @param microPickMaterials 投料单/退料单
     * @return 投料单/退料单集合
     */
    public List<MicroPickMaterials> selectMicroPickMaterialsList(MicroPickMaterials microPickMaterials);

    /**
     * 新增投料单/退料单
     * 
     * @param microPickMaterials 投料单/退料单
     * @return 结果
     */
    public int insertMicroPickMaterials(MicroPickMaterials microPickMaterials);

    /**
     * 修改投料单/退料单
     * 
     * @param microPickMaterials 投料单/退料单
     * @return 结果
     */
    public int updateMicroPickMaterials(MicroPickMaterials microPickMaterials);

    /**
     * 删除投料单/退料单
     * 
     * @param id 投料单/退料单ID
     * @return 结果
     */
    public int deleteMicroPickMaterialsById(Long id);

    /**
     * 批量删除投料单/退料单
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroPickMaterialsByIds(Long[] ids);

    /**
     * 批量保存投/退料单
     * @param list
     * @return
     */
    int insertList(List<MicroPickMaterials> list);

    /**
     * 查询工单下的投/退料单
     *
     * @param query
     */
    List<MicroPickMaterials> getMicroPickMaterialsList(MicroPickMaterials query); 

    /**
     * 根据工单号和物料序列码,获取对应的投料数量
     *
     * @param materialDemandList
     * @return
     */
    List<MicroPickMaterials> selectMaterialShortageInfo(@Param("list") List<MicroManufactureWorkOrderMaterialEntity> materialDemandList);
}

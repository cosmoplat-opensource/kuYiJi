/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.factory;

import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 生产线基础信息Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface MicroManufactureLineMapper 
{
    /**
     * 查询生产线基础信息
     * 
     * @param id 生产线基础信息ID
     * @return 生产线基础信息
     */
    MicroManufactureLine selectMicroManufactureLineById(Long id);

    /**
     * 查询生产线基础信息列表
     * 
     * @param microManufactureLine 生产线基础信息
     * @return 生产线基础信息集合
     */
    List<MicroManufactureLine> selectMicroManufactureLineList(MicroManufactureLine microManufactureLine);

    /**
     * 新增生产线基础信息
     * 
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    int insertMicroManufactureLine(MicroManufactureLine microManufactureLine);

    /**
     * 修改生产线基础信息
     * 
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    int updateMicroManufactureLine(MicroManufactureLine microManufactureLine);

    /**
     * 删除生产线基础信息
     * 
     * @param id 生产线基础信息ID
     * @return 结果
     */
    int deleteMicroManufactureLineById(Long id);

    /**
     * 批量删除生产线基础信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroManufactureLineByIds(Long[] ids);

    /**
     * @author cosmo-hhim-open Team
     * @description 查询生产线基础信息列表(精准查询)
     * @date 2023/3/15 14:42
     * @param param
     * @return java.util.List<com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine>
     **/
    List<MicroManufactureLine> selectMicroManufactureLineListWithoutLike(MicroManufactureLine param); 

    /**
     * 根据车间产线查询生产线信息表
     * @param wshopCode
     * @param mlineCode
     * @return
     */
    MicroManufactureLine selectMicroManufactureLineByWshopCodeAndLineCode(@Param("wshopCode") String wshopCode, @Param("mlineCode") String mlineCode);
}

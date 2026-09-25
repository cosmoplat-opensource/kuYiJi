/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.factory;

import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;

import java.util.List;

/**
 * 生产线基础信息Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface IMicroManufactureLineService 
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
     * 批量删除生产线基础信息
     * 
     * @param ids 需要删除的生产线基础信息ID
     * @return 结果
     */
    int deleteMicroManufactureLineByIds(Long[] ids);

    /**
     * 删除生产线基础信息信息
     * 
     * @param id 生产线基础信息ID
     * @return 结果
     */
    int deleteMicroManufactureLineById(Long id);

    /**
     * @author cosmo-hhim-open Team
     * @description 校验产线名在同一车间下唯一性
     * @date 2023/3/15 14:34
     * @param workShopCode
     * @param mLineName
     * @param id
     * @return boolean
     **/
    boolean isUniqueLineNameByWorkShop(String workShopCode,String mLineName,Long id); 
}

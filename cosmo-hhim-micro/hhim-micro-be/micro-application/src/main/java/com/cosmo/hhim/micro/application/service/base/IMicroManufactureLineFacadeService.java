/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO;

import java.util.List;

/**
 * 生产线基础信息Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface IMicroManufactureLineFacadeService
{

    /**
     * 查询生产线基础信息
     *
     * @param id 生产线基础信息ID
     * @return 生产线基础信息
     */
    MicroManufactureLineDTO selectMicroManufactureLineById(Long id);

    /**
     * 查询生产线基础信息列表
     *
     * @param condition 生产线基础信息
     * @return 生产线基础信息集合
     */
    List<MicroManufactureLineDTO> selectMicroManufactureLineList(MicroManufactureLineDTO condition);

    /**
     * 新增生产线基础信息
     *
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    int insertMicroManufactureLine(MicroManufactureLineDTO microManufactureLine);

    /**
     * 修改生产线基础信息
     *
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    int updateMicroManufactureLine(MicroManufactureLineDTO microManufactureLine);

    /**
     * 批量删除生产线基础信息
     *
     * @param ids 需要删除的生产线基础信息ID
     * @return 结果
     */
    int deleteMicroManufactureLineByIds(Long[] ids);
}

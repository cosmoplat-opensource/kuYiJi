/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.factory;

import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;

import java.util.List;

/**
 * 生产车间Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface MicroWorkShopMapper
{
    /**
     * 查询生产车间
     * 
     * @param id 生产车间ID
     * @return 生产车间
     */
    MicroWorkShop selectMicroWorkshopById(Long id);

    /**
     * 查询生产车间列表
     * 
     * @param microWorkshop 生产车间
     * @return 生产车间集合
     */
    List<MicroWorkShop> selectMicroWorkshopList(MicroWorkShop microWorkshop);

    /**
     * 新增生产车间
     * 
     * @param microWorkshop 生产车间
     * @return 结果
     */
    int insertMicroWorkshop(MicroWorkShop microWorkshop);

    /**
     * 修改生产车间
     * 
     * @param microWorkshop 生产车间
     * @return 结果
     */
    int updateMicroWorkshop(MicroWorkShop microWorkshop);

    /**
     * 删除生产车间
     * 
     * @param id 生产车间ID
     * @return 结果
     */
    int deleteMicroWorkshopById(Long id);

    /**
     * 批量删除生产车间
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroWorkshopByIds(Long[] ids);

    /**
     * 根据车间编码查询车间信息
     * @param wshopCode
     * @return
     */
    MicroWorkShop selectMicroWorkshopByCode(String wshopCode);
}

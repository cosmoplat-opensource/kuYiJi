/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;


import com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO;

import java.util.List;

/**
 * 生产车间Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface IMicroWorkShopFacadeService
{
    /**
     * 查询生产车间
     *
     * @param id 生产车间ID
     * @return 生产车间
     */
    MicroWorkShopDTO selectMicroWorkshopById(Long id);

    /**
     * 查询生产车间列表
     *
     * @param microWorkshop 生产车间
     * @return 生产车间集合
     */
    List<MicroWorkShopDTO> selectMicroWorkshopList(MicroWorkShopDTO microWorkshop);

    /**
     * 新增生产车间
     *
     * @param microWorkshop 生产车间
     * @return 结果
     */
    int insertMicroWorkshop(MicroWorkShopDTO microWorkshop);

    /**
     * 修改生产车间
     *
     * @param microWorkshop 生产车间
     * @return 结果
     */
    int updateMicroWorkshop(MicroWorkShopDTO microWorkshop);

    /**
     * 批量删除生产车间
     *
     * @param ids 需要删除的生产车间ID
     * @return 结果
     */
    int deleteMicroWorkshopByIds(Long[] ids);
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.factory.impl;

import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroManufactureLineMapper;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroWorkShopMapper;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroWorkShopService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 生产车间Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Service
public class MicroWorkShopServiceImpl implements IMicroWorkShopService
{
    @Autowired
    private MicroWorkShopMapper microWorkshopMapper;

    @Autowired
    private MicroManufactureLineMapper microManufactureLineMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询生产车间
     * 
     * @param id 生产车间ID
     * @return 生产车间
     */
    @Override
    public MicroWorkShop selectMicroWorkshopById(Long id)
    {
        return microWorkshopMapper.selectMicroWorkshopById(id);
    }

    /**
     * 查询生产车间列表
     * 
     * @param microWorkshop 生产车间
     * @return 生产车间
     */
    @Override
    public List<MicroWorkShop> selectMicroWorkshopList(MicroWorkShop microWorkshop)
    {
        return microWorkshopMapper.selectMicroWorkshopList(microWorkshop);
    }

    @Override
    public List<MicroWorkShop> selectMicroWorkshopList(MicroWorkShop microWorkshop, boolean isLazy) {
        List<MicroWorkShop> workShopList = this.selectMicroWorkshopList(microWorkshop);
        if (CollectionUtils.isNotEmpty(workShopList)){
            if (!isLazy){
                workShopList.stream().forEach(workShop -> loadLinesByWorkShop(workShop));
            }
        }
        return workShopList;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 加载车间的产线
     * @date 2023/3/20 10:17
     * @param workShop
     * @return void
     **/
    private void loadLinesByWorkShop(MicroWorkShop workShop) {
        MicroManufactureLine param = new MicroManufactureLine();
        param.setWshopCode(workShop.getWshopCode());
        param.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        workShop.setManufactureLineList(microManufactureLineMapper.selectMicroManufactureLineListWithoutLike(param));
    }

    /**
     * 新增生产车间
     * 
     * @param microWorkshop 生产车间
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMicroWorkshop(MicroWorkShop microWorkshop)
    {
        generateWShopCode(microWorkshop);
        return microWorkshopMapper.insertMicroWorkshop(microWorkshop);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 生成车间编码
     * @date 2023/3/14 13:48
     * @param microWorkshop
     * @return void
     **/
    private void generateWShopCode(MicroWorkShop microWorkshop) { 
        String wShopCode = redisCache.incrCodeByDay(CommonConstants.MANUFACTURE_WORKSHOP_GENERATE_PREFIX,1,4);
        microWorkshop.setWshopCode(wShopCode);
    }

    /**
     * 修改生产车间
     * 
     * @param microWorkshop 生产车间
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroWorkshop(MicroWorkShop microWorkshop)
    {
        return microWorkshopMapper.updateMicroWorkshop(microWorkshop);
    }

    /**
     * 批量删除生产车间
     * 
     * @param ids 需要删除的生产车间ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMicroWorkshopByIds(Long[] ids)
    {
        return microWorkshopMapper.deleteMicroWorkshopByIds(ids);
    }

    /**
     * 删除生产车间信息
     * 
     * @param id 生产车间ID
     * @return 结果
     */
    @Override
    public int deleteMicroWorkshopById(Long id)
    {
        return microWorkshopMapper.deleteMicroWorkshopById(id);
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.factory.impl;

import cn.afterturn.easypoi.cache.manager.IFileLoader;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroManufactureLineMapper;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroManufactureLineService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 生产线基础信息Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Service
public class MicroManufactureLineServiceImpl implements IMicroManufactureLineService
{
    @Autowired
    private MicroManufactureLineMapper microManufactureLineMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询生产线基础信息
     * 
     * @param id 生产线基础信息ID
     * @return 生产线基础信息
     */
    @Override
    public MicroManufactureLine selectMicroManufactureLineById(Long id)
    {
        return microManufactureLineMapper.selectMicroManufactureLineById(id);
    }

    /**
     * 查询生产线基础信息列表
     * 
     * @param microManufactureLine 生产线基础信息
     * @return 生产线基础信息
     */
    @Override
    public List<MicroManufactureLine> selectMicroManufactureLineList(MicroManufactureLine microManufactureLine)
    {
        return microManufactureLineMapper.selectMicroManufactureLineList(microManufactureLine);
    }

    /**
     * 新增生产线基础信息
     * 
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMicroManufactureLine(MicroManufactureLine microManufactureLine)
    {
        if (isUniqueLineNameByWorkShop(microManufactureLine.getWshopCode(),microManufactureLine.getMlineName(),microManufactureLine.getId())){
            generateManufactureLineCode(microManufactureLine);
            return microManufactureLineMapper.insertMicroManufactureLine(microManufactureLine);
        }
        return 0;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 校验产线唯一性
     * @date 2023/3/15 14:30
     * @param workShopCode
     * @param mLineName
     * @return boolean
     **/
    @Override
    public boolean isUniqueLineNameByWorkShop(String workShopCode,String mLineName,Long id) {
        MicroManufactureLine param = new MicroManufactureLine();
        param.setWshopCode(workShopCode);
        param.setMlineName(mLineName);
        param.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        List<MicroManufactureLine> lines = microManufactureLineMapper.selectMicroManufactureLineListWithoutLike(param);
        //去重ID不为空 且重名产线数量=1 且为修改的产线自身 则返回true   其余的情况根据集合是否为空来判断
        if (ObjectUtil.isNotNull(id)
                &&CollectionUtil.isNotEmpty(lines)
                &&lines.size()==1
                &&id.equals(lines.get(0).getId())){
            return true;
        }
        return CollectionUtils.isEmpty(lines);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 构造产线编码
     * @date 2023/3/15 14:30
     * @param microManufactureLine
     * @return void
     **/
    private void generateManufactureLineCode(MicroManufactureLine microManufactureLine) {
        String manufactureLineCode = redisCache.incrCodeByDay(CommonConstants.MANUFACTURE_LINE_GENERATE_PREFIX,1,4);
        microManufactureLine.setMlineCode(manufactureLineCode);
    }

    /**
     * 修改生产线基础信息
     * 
     * @param microManufactureLine 生产线基础信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroManufactureLine(MicroManufactureLine microManufactureLine)
    {
        return microManufactureLineMapper.updateMicroManufactureLine(microManufactureLine);
    }

    /**
     * 批量删除生产线基础信息
     * 
     * @param ids 需要删除的生产线基础信息ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMicroManufactureLineByIds(Long[] ids)
    {
        return microManufactureLineMapper.deleteMicroManufactureLineByIds(ids);
    }

    /**
     * 删除生产线基础信息信息
     * 
     * @param id 生产线基础信息ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureLineById(Long id)
    {
        return microManufactureLineMapper.deleteMicroManufactureLineById(id);
    }
}

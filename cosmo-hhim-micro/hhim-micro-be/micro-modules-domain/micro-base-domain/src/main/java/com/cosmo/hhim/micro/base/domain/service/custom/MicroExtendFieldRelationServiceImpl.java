/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.custom;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation;
import com.cosmo.hhim.micro.base.domain.mapper.custom.MicroExtendFieldRelationMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExtendFieldRelationService;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展字段关系Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-27
 */
@Service
public class MicroExtendFieldRelationServiceImpl implements IMicroExtendFieldRelationService {
    @Autowired
    private MicroExtendFieldRelationMapper microExtendFieldRelationMapper;

    /**
     * 查询扩展字段关系
     * 
     * @param id 扩展字段关系ID
     * @return 扩展字段关系
     */
    @Override
    public MicroExtendFieldRelation selectMicroExtendFieldRelationById(Long id)
    {
        return microExtendFieldRelationMapper.selectMicroExtendFieldRelationById(id);
    }

    /**
     * 查询扩展字段关系列表
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 扩展字段关系
     */
    @Override
    public List<MicroExtendFieldRelation> selectMicroExtendFieldRelationList(MicroExtendFieldRelation microExtendFieldRelation)
    {
        return microExtendFieldRelationMapper.selectMicroExtendFieldRelationList(microExtendFieldRelation);
    }

    /**
     * 新增扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMicroExtendFieldRelation(MicroExtendFieldRelation microExtendFieldRelation)
    {
        return microExtendFieldRelationMapper.insertMicroExtendFieldRelation(microExtendFieldRelation);
    }

    /**
     * 修改扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroExtendFieldRelation(MicroExtendFieldRelation microExtendFieldRelation)
    {
        return microExtendFieldRelationMapper.updateMicroExtendFieldRelation(microExtendFieldRelation);
    }

    /**
     * 批量删除扩展字段关系
     * 
     * @param ids 需要删除的扩展字段关系ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldRelationByIds(Long[] ids)
    {
        return microExtendFieldRelationMapper.deleteMicroExtendFieldRelationByIds(ids);
    }

    /**
     * 删除扩展字段关系信息
     * 
     * @param id 扩展字段关系ID
     * @return 结果
     */
    @Override
    public int deleteMicroExtendFieldRelationById(Long id)
    {
        return microExtendFieldRelationMapper.deleteMicroExtendFieldRelationById(id);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 批量绑定自定义字段关系
     * @date 2023/3/27 13:51
     * @param extendFieldRelations
     * @return int
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<MicroExtendFieldRelation> extendFieldRelations) {
        if (CollectionUtil.isNotEmpty(extendFieldRelations)){
            MicroExtendFieldRelation param = new MicroExtendFieldRelation();
            param.setBusinessCode(extendFieldRelations.get(0).getBusinessCode());
            param.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
            List<MicroExtendFieldRelation> microExtendFieldRelations = microExtendFieldRelationMapper.selectMicroExtendFieldRelationList(param);
            //去重 防止重复添加
            List<MicroExtendFieldRelation> addExtendFieldRelations = extendFieldRelations.stream().filter(
                    obj1 -> !microExtendFieldRelations.stream().map(
                            obj2 -> obj2.getExtFieldId()).collect(Collectors.toList())
                            .contains(obj1.getExtFieldId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(addExtendFieldRelations)){
                return microExtendFieldRelationMapper.batchInsertExtendFieldRelations(addExtendFieldRelations);
            }
        }
        return 0;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendFieldRelation;

import java.util.List;

/**
 * 扩展字段关系Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-27
 */
public interface IMicroExtendFieldRelationService 
{
    /**
     * 查询扩展字段关系
     * 
     * @param id 扩展字段关系ID
     * @return 扩展字段关系
     */
    MicroExtendFieldRelation selectMicroExtendFieldRelationById(Long id);

    /**
     * 查询扩展字段关系列表
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 扩展字段关系集合
     */
    List<MicroExtendFieldRelation> selectMicroExtendFieldRelationList(MicroExtendFieldRelation microExtendFieldRelation);

    /**
     * 新增扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    int insertMicroExtendFieldRelation(MicroExtendFieldRelation microExtendFieldRelation);

    /**
     * 修改扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    int updateMicroExtendFieldRelation(MicroExtendFieldRelation microExtendFieldRelation);

    /**
     * 批量删除扩展字段关系
     * 
     * @param ids 需要删除的扩展字段关系ID
     * @return 结果
     */
    int deleteMicroExtendFieldRelationByIds(Long[] ids);

    /**
     * 删除扩展字段关系信息
     * 
     * @param id 扩展字段关系ID
     * @return 结果
     */
    int deleteMicroExtendFieldRelationById(Long id);

    int batchInsert(List<MicroExtendFieldRelation> extendFieldRelations); 
}

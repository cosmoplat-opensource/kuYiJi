/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO;

import java.util.List;

/**
 * 扩展字段关系Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-27
 */
public interface IMicroExtendFieldRelationFacadeService
{
    /**
     * 查询扩展字段关系
     * 
     * @param id 扩展字段关系ID
     * @return 扩展字段关系
     */
    MicroExtendFieldRelationDTO selectMicroExtendFieldRelationById(Long id);

    /**
     * 查询扩展字段关系列表
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 扩展字段关系集合
     */
    List<MicroExtendFieldRelationDTO> selectMicroExtendFieldRelationList(MicroExtendFieldRelationDTO microExtendFieldRelation);

    /**
     * 新增扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    int insertMicroExtendFieldRelation(MicroExtendFieldRelationDTO microExtendFieldRelation);

    /**
     * 修改扩展字段关系
     * 
     * @param microExtendFieldRelation 扩展字段关系
     * @return 结果
     */
    int updateMicroExtendFieldRelation(MicroExtendFieldRelationDTO microExtendFieldRelation);

    /**
     * 批量删除扩展字段关系
     * 
     * @param ids 需要删除的扩展字段关系ID
     * @return 结果
     */
    int deleteMicroExtendFieldRelationByIds(Long[] ids);

    List<MicroExtendFieldRelationDTO> selectMicroExtendFieldRelationListByCondition(MicroExtendFieldRelationDTO microExtendFieldRelation); 
}

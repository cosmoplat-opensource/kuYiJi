/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.custom;

import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField;
import com.cosmo.hhim.micro.base.domain.entity.custom.SimpleExtendFieldInfo;

import java.util.List;

/**
 * 扩展字段Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface IMicroExtendFieldService 
{
    /**
     * 查询扩展字段
     * 
     * @param id 扩展字段ID
     * @return 扩展字段
     */
    MicroExtendField selectMicroExtendFieldById(Long id);

    /**
     * 查询扩展字段列表
     * 
     * @param microExtendField 扩展字段
     * @return 扩展字段集合
     */
    List<MicroExtendField> selectMicroExtendFieldList(MicroExtendField microExtendField);

    /**
     * 新增扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    int insertMicroExtendField(MicroExtendField microExtendField);

    /**
     * 修改扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    int updateMicroExtendField(MicroExtendField microExtendField);

    /**
     * 批量删除扩展字段
     * 
     * @param ids 需要删除的扩展字段ID
     * @return 结果
     */
    int deleteMicroExtendFieldByIds(Long[] ids);

    /**
     * 删除扩展字段信息
     * 
     * @param id 扩展字段ID
     * @return 结果
     */
    int deleteMicroExtendFieldById(Long id);

    /**
     * @author cosmo-hhim-open Team
     * @description 保存自定义字段
     * @date 2023/3/22 18:02
     * @param microExtendField
     * @return com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField
     **/
    MicroExtendField saveMicroExtendField(MicroExtendField microExtendField); 

    /**
     * 返回自定义字段的基础信息
     *
     * @param extendContent
     * @return
     */
    List<SimpleExtendFieldInfo> selectSimpleExtendFieldInfoList(String extendContent);
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO;
import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField;

import java.util.List;


/**
 * 扩展字段Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface IMicroExtendFieldFacadeService
{
    /**
     * 查询扩展字段
     * 
     * @param id 扩展字段ID
     * @return 扩展字段
     */
    MicroExtendFieldDTO selectMicroExtendFieldById(Long id);

    /**
     * 查询扩展字段列表
     * 
     * @param microExtendField 扩展字段
     * @return 扩展字段集合
     */
    List<MicroExtendFieldDTO> selectMicroExtendFieldList(MicroExtendFieldDTO microExtendField);

    /**
     * 新增扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    MicroExtendFieldDTO insertMicroExtendField(MicroExtendFieldDTO microExtendField);

    /**
     * 修改扩展字段
     * 
     * @param microExtendField 扩展字段
     * @return 结果
     */
    int updateMicroExtendField(MicroExtendFieldDTO microExtendField);

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
     * @description 查询扩展字段列表
     * @date 2023/3/22 16:36
     * @param microExtendField
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO>
     **/
    List<MicroExtendFieldDTO> selectMicroExtendFieldListByCondition(MicroExtendFieldDTO microExtendField); 
}

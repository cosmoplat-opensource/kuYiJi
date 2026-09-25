/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.custom;

import com.cosmo.hhim.micro.base.domain.entity.custom.MicroExtendField;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 扩展字段Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface MicroExtendFieldMapper 
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
     * 根据扩展字段code查询扩展字段信息
     *
     * @param extFieldList
     * @return
     */
    List<MicroExtendField> selectMicroExtendFieldListByExtFieldList(@Param("extFieldList") Set<String> extFieldList);

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
     * 删除扩展字段
     * 
     * @param id 扩展字段ID
     * @return 结果
     */
    int deleteMicroExtendFieldById(Long id);

    /**
     * 批量删除扩展字段
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroExtendFieldByIds(Long[] ids);

    /**
     * @author cosmo-hhim-open Team
     * @description 计算业务领域下当前自定义字段最大编号
     * @date 2023/3/24 11:23
     **/
    int selectMaxExtField(); 
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomJob;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-12-06
 */
public interface HyzzCustomJobMapper 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    public HyzzCustomJob selectHyzzCustomJobById(Long id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param hyzzCustomJob 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<HyzzCustomJob> selectHyzzCustomJobList(HyzzCustomJob hyzzCustomJob);

    /**
     * 新增【请填写功能名称】
     * 
     * @param hyzzCustomJob 【请填写功能名称】
     * @return 结果
     */
    public int insertHyzzCustomJob(HyzzCustomJob hyzzCustomJob);

    /**
     * 修改【请填写功能名称】
     * 
     * @param hyzzCustomJob 【请填写功能名称】
     * @return 结果
     */
    public int updateHyzzCustomJob(HyzzCustomJob hyzzCustomJob);

    /**
     * 删除【请填写功能名称】
     * 
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    public int deleteHyzzCustomJobById(Long id);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteHyzzCustomJobByIds(Long[] ids);
}

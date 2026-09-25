/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;



import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzTenantJob;

import java.text.ParseException;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-12-13
 */
public interface IHyzzTenantJobService 
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    public HyzzTenantJob selectHyzzTenantJobById(Long id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param hyzzTenantJob 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<HyzzTenantJob> selectHyzzTenantJobList(HyzzTenantJob hyzzTenantJob);

    /**
     * 新增【请填写功能名称】
     * 
     * @param hyzzTenantJob 【请填写功能名称】
     * @return 结果
     */
    public int insertHyzzTenantJob(HyzzTenantJob hyzzTenantJob) throws ParseException;

    /**
     * 修改【请填写功能名称】
     * 
     * @param hyzzTenantJob 【请填写功能名称】
     * @return 结果
     */
    public int updateHyzzTenantJob(HyzzTenantJob hyzzTenantJob) throws ParseException;

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】ID
     * @return 结果
     */
    public int deleteHyzzTenantJobByIds(Long[] ids);

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    public int deleteHyzzTenantJobById(Long id);
}

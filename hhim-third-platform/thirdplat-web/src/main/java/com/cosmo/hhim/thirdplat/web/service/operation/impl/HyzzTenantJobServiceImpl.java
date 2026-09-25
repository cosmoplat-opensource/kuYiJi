/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzTenantJob;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzTenantJobMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzTenantJobService;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-12-13
 */
@Service
public class HyzzTenantJobServiceImpl implements IHyzzTenantJobService
{
    @Autowired
    private HyzzTenantJobMapper hyzzTenantJobMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    @Override
    public HyzzTenantJob selectHyzzTenantJobById(Long id)
    {
        return hyzzTenantJobMapper.selectHyzzTenantJobById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param hyzzTenantJob 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<HyzzTenantJob> selectHyzzTenantJobList(HyzzTenantJob hyzzTenantJob)
    {
        return hyzzTenantJobMapper.selectHyzzTenantJobList(hyzzTenantJob);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param hyzzTenantJob 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertHyzzTenantJob(HyzzTenantJob hyzzTenantJob) throws ParseException {
        hyzzTenantJob.setCreateTime(DateUtils.getNowDate());
        hyzzTenantJob.setTaskNextTime(getTime(hyzzTenantJob.getCron(),System.currentTimeMillis()));
        return hyzzTenantJobMapper.insertHyzzTenantJob(hyzzTenantJob);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param hyzzTenantJob 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateHyzzTenantJob(HyzzTenantJob hyzzTenantJob) throws ParseException {
        hyzzTenantJob.setUpdateTime(DateUtils.getNowDate());
        hyzzTenantJob.setTaskNextTime(getTime(hyzzTenantJob.getCron(),System.currentTimeMillis()));
        return hyzzTenantJobMapper.updateHyzzTenantJob(hyzzTenantJob);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】ID
     * @return 结果
     */
    @Override
    public int deleteHyzzTenantJobByIds(Long[] ids)
    {
        return hyzzTenantJobMapper.deleteHyzzTenantJobByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    @Override
    public int deleteHyzzTenantJobById(Long id)
    {
        return hyzzTenantJobMapper.deleteHyzzTenantJobById(id);
    }

    private Long getTime(String time, long nowTime) throws ParseException {
        CronExpression cronExpression = new CronExpression(time);
        Date dateTime = cronExpression.getNextValidTimeAfter(new Date(nowTime));
        return dateTime.getTime();
    }
}

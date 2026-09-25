/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomJob;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzCustomJobMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzCustomJobService;
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
 * @date 2021-12-06
 */
@Service
public class HyzzCustomJobServiceImpl implements IHyzzCustomJobService
{
    @Autowired
    private HyzzCustomJobMapper hyzzCustomJobMapper;

    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    @Override
    public HyzzCustomJob selectHyzzCustomJobById(Long id)
    {
        return hyzzCustomJobMapper.selectHyzzCustomJobById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     *
     * @param hyzzCustomJob 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<HyzzCustomJob> selectHyzzCustomJobList(HyzzCustomJob hyzzCustomJob)
    {
        return hyzzCustomJobMapper.selectHyzzCustomJobList(hyzzCustomJob);
    }

    /**
     * 新增【请填写功能名称】
     *
     * @param hyzzCustomJob 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertHyzzCustomJob(HyzzCustomJob hyzzCustomJob) throws ParseException {
        hyzzCustomJob.setTaskNextTime(getTime(hyzzCustomJob.getCron(),new Date(System.currentTimeMillis())));
        hyzzCustomJob.setCreateTime(DateUtils.getNowDate());
        return hyzzCustomJobMapper.insertHyzzCustomJob(hyzzCustomJob);
    }

    /**
     * 修改【请填写功能名称】
     *
     * @param hyzzCustomJob 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateHyzzCustomJob(HyzzCustomJob hyzzCustomJob) throws ParseException {
        //
        if(hyzzCustomJob.getTaskNextTime()==0 && StringUtils.isNotEmpty(hyzzCustomJob.getCron())){
            hyzzCustomJob.setTaskNextTime(getTime(hyzzCustomJob.getCron(),hyzzCustomJob.getPlanStartTime()));
        }
        hyzzCustomJob.setUpdateTime(DateUtils.getNowDate());
        return hyzzCustomJobMapper.updateHyzzCustomJob(hyzzCustomJob);
    }

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的【请填写功能名称】ID
     * @return 结果
     */
    @Override
    public int deleteHyzzCustomJobByIds(Long[] ids)
    {
        return hyzzCustomJobMapper.deleteHyzzCustomJobByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     *
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    @Override
    public int deleteHyzzCustomJobById(Long id)
    {
        return hyzzCustomJobMapper.deleteHyzzCustomJobById(id);
    }

    @Override
    public int updateHyzzCustomJob(List<HyzzCustomJob> hyzzCustomJob) throws ParseException {
        int num=0;
        for(HyzzCustomJob job:hyzzCustomJob){
            if(StringUtils.isNotEmpty(job.getCron())){
                job.setTaskNextTime(getTime(job.getCron(),job.getPlanStartTime()));
            }
            job.setUpdateTime(DateUtils.getNowDate());
            int a= hyzzCustomJobMapper.updateHyzzCustomJob(job);
            num=++a;
        }
        return num;
    }


    /**
     * cron表达式转时间戳
     *
     * @return
     * @throws ParseException
     */
    private Long getTime(String time,Date planStartTime) throws ParseException {
        CronExpression cronExpression = new CronExpression(time);
        Date dateTime = cronExpression.getNextValidTimeAfter(planStartTime);
        return dateTime.getTime();
    }




}

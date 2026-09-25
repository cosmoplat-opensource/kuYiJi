/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestionLog;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzPortalSuggestionLogMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzPortalSuggestionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 意见反馈日志Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 */
@Service
public class HyzzPortalSuggestionLogServiceImpl implements IHyzzPortalSuggestionLogService
{
    @Autowired
    private HyzzPortalSuggestionLogMapper hyzzPortalSuggestionLogMapper;

    /**
     * 查询意见反馈日志
     * 
     * @param id 意见反馈日志ID
     * @return 意见反馈日志
     */
    @Override
    public HyzzPortalSuggestionLog selectHyzzPortalSuggestionLogById(Long id)
    {
        return hyzzPortalSuggestionLogMapper.selectHyzzPortalSuggestionLogById(id);
    }

    /**
     * 查询意见反馈日志列表
     * 
     * @param hyzzPortalSuggestionLog 意见反馈日志
     * @return 意见反馈日志
     */
    @Override
    public List<HyzzPortalSuggestionLog> selectHyzzPortalSuggestionLogList(HyzzPortalSuggestionLog hyzzPortalSuggestionLog)
    {
        return hyzzPortalSuggestionLogMapper.selectHyzzPortalSuggestionLogList(hyzzPortalSuggestionLog);
    }

    /**
     * 新增意见反馈日志
     * 
     * @param hyzzPortalSuggestionLog 意见反馈日志
     * @return 结果
     */
    @Override
    public int insertHyzzPortalSuggestionLog(HyzzPortalSuggestionLog hyzzPortalSuggestionLog)
    {
        hyzzPortalSuggestionLog.setCreateTime(DateUtils.getNowDate());
        return hyzzPortalSuggestionLogMapper.insertHyzzPortalSuggestionLog(hyzzPortalSuggestionLog);
    }

    /**
     * 修改意见反馈日志
     * 
     * @param hyzzPortalSuggestionLog 意见反馈日志
     * @return 结果
     */
    @Override
    public int updateHyzzPortalSuggestionLog(HyzzPortalSuggestionLog hyzzPortalSuggestionLog)
    {
        return hyzzPortalSuggestionLogMapper.updateHyzzPortalSuggestionLog(hyzzPortalSuggestionLog);
    }

    /**
     * 批量删除意见反馈日志
     * 
     * @param ids 需要删除的意见反馈日志ID
     * @return 结果
     */
    @Override
    public int deleteHyzzPortalSuggestionLogByIds(Long[] ids)
    {
        return hyzzPortalSuggestionLogMapper.deleteHyzzPortalSuggestionLogByIds(ids);
    }

    /**
     * 删除意见反馈日志信息
     * 
     * @param id 意见反馈日志ID
     * @return 结果
     */
    @Override
    public int deleteHyzzPortalSuggestionLogById(Long id)
    {
        return hyzzPortalSuggestionLogMapper.deleteHyzzPortalSuggestionLogById(id);
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestionLog;

import java.util.List;

/**
 * 意见反馈日志Mapper接口
 * 
 * @author cosmo-hhim-open Team
 */
public interface HyzzPortalSuggestionLogMapper 
{
    /**
     * 查询意见反馈日志
     * 
     * @param id 意见反馈日志ID
     * @return 意见反馈日志
     */
    public HyzzPortalSuggestionLog selectHyzzPortalSuggestionLogById(Long id);

    /**
     * 查询意见反馈日志列表
     * 
     * @param hyzzPortalSuggestionLog 意见反馈日志
     * @return 意见反馈日志集合
     */
    public List<HyzzPortalSuggestionLog> selectHyzzPortalSuggestionLogList(HyzzPortalSuggestionLog hyzzPortalSuggestionLog);

    /**
     * 新增意见反馈日志
     * 
     * @param hyzzPortalSuggestionLog 意见反馈日志
     * @return 结果
     */
    public int insertHyzzPortalSuggestionLog(HyzzPortalSuggestionLog hyzzPortalSuggestionLog);

    /**
     * 修改意见反馈日志
     * 
     * @param hyzzPortalSuggestionLog 意见反馈日志
     * @return 结果
     */
    public int updateHyzzPortalSuggestionLog(HyzzPortalSuggestionLog hyzzPortalSuggestionLog);

    /**
     * 删除意见反馈日志
     * 
     * @param id 意见反馈日志ID
     * @return 结果
     */
    public int deleteHyzzPortalSuggestionLogById(Long id);

    /**
     * 批量删除意见反馈日志
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteHyzzPortalSuggestionLogByIds(Long[] ids);
}

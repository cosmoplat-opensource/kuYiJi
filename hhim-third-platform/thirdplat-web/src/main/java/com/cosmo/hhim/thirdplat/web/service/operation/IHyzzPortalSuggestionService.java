/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqHistoryEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestion;

import java.util.List;

/**
 * 门户的意见反馈Service接口
 *
 * @author cosmo-hhim-open Team
 */
public interface IHyzzPortalSuggestionService
{
    /**
     * 查询门户的意见反馈
     *
     * @param id 门户的意见反馈ID
     * @return 门户的意见反馈
     */
    public HyzzPortalSuggestion selectHyzzPortalSuggestionById(Long id);

    /**
     * 查询门户的意见反馈列表
     *
     * @param hyzzPortalSuggestion 门户的意见反馈
     * @return 门户的意见反馈集合
     */
    public List<HyzzPortalSuggestion> selectHyzzPortalSuggestionList(HyzzPortalSuggestion hyzzPortalSuggestion);

    /**
     * 新增门户的意见反馈
     *
     * @param hyzzPortalSuggestion 门户的意见反馈
     * @return 结果
     */
    public int insertHyzzPortalSuggestion(HyzzPortalSuggestion hyzzPortalSuggestion);

    /**
     * 修改门户的意见反馈
     *
     * @param hyzzPortalSuggestion 门户的意见反馈
     * @return 结果
     */
    public int updateHyzzPortalSuggestion(HyzzPortalSuggestion hyzzPortalSuggestion);

    /**
     * 批量删除门户的意见反馈
     *
     * @param ids 需要删除的门户的意见反馈ID
     * @return 结果
     */
    public int deleteHyzzPortalSuggestionByIds(Long[] ids);

    /**
     * 删除门户的意见反馈信息
     *
     * @param id 门户的意见反馈ID
     * @return 结果
     */
    public int deleteHyzzPortalSuggestionById(Long id);

    /**
     * 新增意见反馈
     *
     * @param hyzzPortalSuggestion
     * @return
     */
    int addHyzzPortalSuggestion(HyzzPortalSuggestion hyzzPortalSuggestion);

    /**
     * 根据条件获取问答列表
     *
     * @param hyzzPortalSuggestion
     * @return
     */
    List<HyzzFaqHistoryEntity> selectChatHistory(HyzzPortalSuggestion hyzzPortalSuggestion);
}

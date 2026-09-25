/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration;

import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;

import java.util.List;

/**
 * 智能问答
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroFAQFacadeService { 
    /**
     * 猜你喜欢
     *
     * @param keyword 用户点击关键词,根据关键词中答案列表过滤
     * @return
     */
    List<HyzzFaqPairRecordEntity> guessULike(String keyword); 

    /**
     * 获取当前用户下拥有的关键词
     *
     * @return
     */
    List<String> keyword();

    /**
     * 根据预设问题ID获取答案
     *
     * @param qId
     * @return
     */
    HyzzFaqPairRecordEntity getFixedAnswer(Long qId);

    /**
     * 根据用户输入的内容获取最相近的答案
     *
     * @param content
     * @return
     */
    HyzzFaqPairRecordEntity getMostSimilarAnswer(String content);

    /**
     * 根据用户输入的内容分词
     *
     * @param content
     * @return
     */
    List<HyzzFaqPairRecordEntity> getSegmentWords(String content);

    /**
     * 获取聊天内容
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    TableDataInfo getChatHistory(Integer pageNum, Integer pageSize);
}

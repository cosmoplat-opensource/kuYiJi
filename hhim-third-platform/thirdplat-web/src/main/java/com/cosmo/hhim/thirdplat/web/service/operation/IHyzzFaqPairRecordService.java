/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqQueryEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzInverseIndexEntity;

import java.util.List;

/**
 * 智能客服问答对Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
public interface IHyzzFaqPairRecordService {

    /**
     * 查询智能客服问答对列表
     *
     * @param hyzzFaqPairRecordEntity 智能客服问答对
     * @return 智能客服问答对集合
     */
    List<HyzzFaqPairRecordEntity> selectHyzzFaqPairRecordEntityList(HyzzFaqQueryEntity hyzzFaqPairRecordEntity);

    /**
     * 根据角色获取所有索引
     *
     * @param roleCodeList
     * @return
     */
    List<String> selectIndexByRoleList(HyzzFaqQueryEntity roleCodeList);

    /**
     * 根据问题ID获取对应答案
     *
     * @param qId
     * @return
     */
    HyzzFaqPairRecordEntity selectHyzzFaqPairRecordEntityByQuestionId(Long qId);

    /**
     * 获取最相近的答案
     *
     * @param entity
     * @return
     */
    HyzzFaqPairRecordEntity getMostSimilarAnswer(HyzzFaqQueryEntity entity);

    /**
     * 对用户输入的问题内容进行分词,匹配分词与索引内容获取最相近的答案
     *
     * @param entity
     * @return
     */
    List<HyzzFaqPairRecordEntity> segmentContent(HyzzFaqQueryEntity entity);

    /**
     * 获取所有索引
     *
     * @return
     */
    List<HyzzInverseIndexEntity> findAllIndex();
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.thirdplat.api.operation.constants.BusinessConstants;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqQueryEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzInverseIndexEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestion;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzFaqPairRecordMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzFaqPairRecordService;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzPortalSuggestionService;
import com.cosmo.hhim.thirdplat.web.utils.SimilarityUtil;
import com.cosmo.hhim.thirdplat.web.utils.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 智能客服问答对Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
@Slf4j
@Service
public class HyzzFaqPairRecordServiceImpl implements IHyzzFaqPairRecordService {
    @Autowired
    private HyzzFaqPairRecordMapper recordMapper;
    @Autowired
    private IHyzzPortalSuggestionService suggestionService;

    /**
     * 查询智能客服问答对列表
     *
     * @param entity 智能客服问答对
     * @return 智能客服问答对
     */
    @Override
    public List<HyzzFaqPairRecordEntity> selectHyzzFaqPairRecordEntityList(HyzzFaqQueryEntity entity) {
        return recordMapper.selectFaqListByRoleAndIndex(entity);
    }

    /**
     * 根据角色获取对应的索引集合
     *
     * @param entity
     * @return
     */
    @Override
    public List<String> selectIndexByRoleList(HyzzFaqQueryEntity entity) {
        List<HyzzFaqPairRecordEntity> list = recordMapper.selectFaqListByRoleAndIndex(entity);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        Set<String> result = new HashSet<>();
        List<String> indexList;
        for (HyzzFaqPairRecordEntity record : list) {
            indexList = record.getIndexList();
            if (!CollectionUtils.isEmpty(indexList)) {
                result.addAll(indexList);
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 根据问题ID获取对应答案
     *
     * @param qId
     * @return
     */
    @Override
    public HyzzFaqPairRecordEntity selectHyzzFaqPairRecordEntityByQuestionId(Long qId) {
        return recordMapper.selectHyzzFaqPairRecordEntityById(qId);
    }

    /**
     * 根据用户输入的问题内容+角色+indexSource=获取最相近的答案
     *
     * @param entity
     * @return
     */
    @Override
    public HyzzFaqPairRecordEntity getMostSimilarAnswer(HyzzFaqQueryEntity entity) {
        String content = entity.getQuestionContent();
        List<HyzzFaqPairRecordEntity> list = recordMapper.selectFaqListByRoleAndIndex(entity);
        HyzzFaqPairRecordEntity recordEntity = this.calcSimilarity(content, list);
        if (recordEntity == null) {
            try {
                //如果没有相似度很高的数据,进行分词获取相似的问题
                List<String> segmentList = TextUtils.wordSegmentation(content);
                entity.setIndexList(segmentList);
                list = recordMapper.selectFaqListByRoleAndIndex(entity);
                if (!CollectionUtils.isEmpty(list)) {
                    recordEntity = list.stream().max(Comparator.comparing(l -> l.getIndexList().size())).orElse(null);
                }
            } catch (Exception e) {
                log.warn("Failed to similar question:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
        }
        HyzzPortalSuggestion suggestion = new HyzzPortalSuggestion();
        suggestion.setType("0");
        suggestion.setSourcePlat("MICRO");
        suggestion.setAutoDeal(1);
        suggestion.setContent(content);
        suggestion.setTenantCode((String) ThreadLocalCache.getCache(Constant.TENANT_CODE));
        suggestion.setCreateBy(entity.getUserName());
        suggestion.setCreateByName(entity.getUserName());
        suggestion.setCreateTime(new Date());
        suggestion.setUpdateTime(new Date());
        suggestionService.addHyzzPortalSuggestion(suggestion);
        return recordEntity;
    }

    /**
     * 计算相似度
     *
     * @param target
     * @param recordList
     * @return
     */
    private HyzzFaqPairRecordEntity calcSimilarity(String target, List<HyzzFaqPairRecordEntity> recordList) {
        PriorityQueue<HyzzFaqPairRecordEntity> maxQueue =
                new PriorityQueue<>((o1, o2) -> o2.getSimilarity().compareTo(o1.getSimilarity()));
        double radio;
        for (HyzzFaqPairRecordEntity r : recordList) {
            String question = r.getQuestion();
            radio = SimilarityUtil.getSimilarityRatio(question, target);
            if (radio >= 0.5) {
                r.setSimilarity(radio);
                maxQueue.add(r);
            }
        }
        return maxQueue.isEmpty() ? null : maxQueue.poll();
    }

    /**
     * 对用户输入的问题内容进行分词,匹配分词与索引内容匹配
     *
     * @param entity
     * @return
     */
    @Override
    public List<HyzzFaqPairRecordEntity> segmentContent(HyzzFaqQueryEntity entity) {
        String content = entity.getQuestionContent();
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        try {
            result = TextUtils.wordSegmentation(content);
        } catch (Exception e) {
            log.warn("Failed to segment question:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
        entity.setIndexList(result);
        return recordMapper.selectFaqListByRoleAndIndex(entity);
    }

    @Override
    public List<HyzzInverseIndexEntity> findAllIndex() {
        HyzzFaqQueryEntity queryEntity = new HyzzFaqQueryEntity();
        queryEntity.setIndexSource(BusinessConstants.FaqIndexSourceEnum.FAQ.getKey());
        return recordMapper.filterFaqIndex(queryEntity);
    }


}

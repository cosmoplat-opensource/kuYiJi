/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.tech;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;
import com.cosmo.hhim.micro.base.domain.entity.submit.FirstOrLastProcess;
import com.cosmo.hhim.micro.base.domain.entity.tech.*;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroStandardEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工艺链定义Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
public interface IMicroTechnologyService {
    /**
     * 查询工艺链定义
     *
     * @param id 工艺链定义ID
     * @return 工艺链定义
     */
    MicroTechnology selectMicroTechnologyById(Long id);

    /**
     * 查询工艺链定义列表
     *
     * @param microTechnology 工艺链定义
     * @return 工艺链定义集合
     */
    List<MicroTechnology> selectMicroTechnologyList(MicroTechnology microTechnology);

    List<MicroProcessChainBindEntity> selectMicroTechChain(MicroTechnology microTechnology); 

    List<MicroProcessChainBindEntity> selectMicroTechChainByTechType(MicroTechnology microTechnology, String techType); 

    List<MicroProcessChainBindEntity> selectMicroTechChainByProductIdsOrSeqs(List<Long> productIds, List<String> productSeqs, String techType); 


    List<MicroSingleTechChainEntity> selectSingleChainByProduct(List<Long> productIds, List<String> productSeqs); 

    /**
     * 批量删除工艺链定义
     *
     * @param ids 需要删除的工艺链定义ID
     * @return 结果
     */
    int deleteMicroTechnologyByIds(Long[] ids);

    /**
     * 删除工艺链定义信息
     *
     * @param id 工艺链定义ID
     * @return 结果
     */
    int deleteMicroTechnologyById(Long id);

    /**
     * 根据某个工艺绑定工艺链
     *
     * @param technology
     * @param chainList
     * @param condition
     * @return
     */
    MicroTechnologyBindResult bindTechAndChain(MicroTechnology technology, List<MicroProcessChain> chainList, MicroTechBindCondition condition);

    /**
     * 保存工序与工艺链(多个工艺绑定同一个工序链)
     *
     * @param singleTechChain
     * @return
     */
    boolean saveSingleTechAndChain(MicroSingleTechChainEntity singleTechChain); 

    /**
     * 保存工艺和工艺链
     *
     * @param param
     */
    void saveTechAndChain(MicroSingleTechChainEntity param);

    List<MicroStandardEntity> selectStandardPro(String key); 

    /**
     * 覆盖或相似工艺链处理
     *
     * @param technologyList
     * @param chainList
     * @return
     */
    int coverOrSimilar(List<MicroTechnology> technologyList, List<MicroProcessChain> chainList);

    List<MicroTechnology> judgeStandardOrProduct(String productCode, String productSeq, Long productId); 

    /**
     * 根据标准工艺链表获取工艺信息
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    FirstOrLastProcess getFirstOrLastProcessFlagByCraftTech(String productSeq, String processSeq);

    Map<String, Set<MicroSelectEntity>> recommendByProductOrProcess(String productSeq); 

    boolean appandTailTech(MicroSingleTechChainEntity chainEntity); 

    /**
     * 查询工艺链 可能同时有草稿和标准工艺，如果有标准工艺则使用标准工艺
     *
     * @param productIds
     * @param productSeqs
     * @param techType
     * @return
     */
    List<MicroProcessChainBindEntity> selectSingleMicroChainList(List<Long> productIds, List<String> productSeqs, String techType);

    Set<String> judgeStandardProductByList(List<Long> productIds, List<String> productSeqs); 

    List<MicroProductProcessEntity> getPreProcessByStandardProductAndProcess(List<MicroProductProcessEntity> collect); 

    Map<String, List<MicroSelectEntity>> recommendOperateProcessByProductList(List<String> productList); 

    List<MicroProcessChainBindEntity> sortChainByParallelProcess(List<MicroProcessChainBindEntity> toSortList); 

    List<String> selectAllProductByTech(MicroTechnology technology, List<Integer> patternList); 

    /**
     * 校验产品是否有标准工艺，如果有标准工艺，工序是否符合标准工艺路线
     *
     * @param productSeq
     * @param processSeq
     * @param preProcessSeq
     * @return
     */
    boolean validSubmitRecordTechInfo(String productSeq, String processSeq, String preProcessSeq);

    /**
     * 根据产品或产品+现工序取得标准/草稿工艺中的工序范围列表
     *
     * @param productSeq
     * @param operateProcessSeq
     * @return
     */
    MicroProcessMixedResultEntity selectRangeProcessInTechByProductOrOperateProcess(String productSeq, String operateProcessSeq);

    int removeTechByProduct(List<Long> productIds); 
}

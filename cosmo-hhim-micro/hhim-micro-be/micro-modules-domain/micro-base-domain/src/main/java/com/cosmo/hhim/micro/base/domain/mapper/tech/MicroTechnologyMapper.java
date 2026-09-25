/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.tech;

import com.cosmo.hhim.micro.base.domain.entity.tech.*;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroStandardEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 工艺链定义Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
public interface MicroTechnologyMapper {
    /**
     * 查询工艺链定义
     *
     * @param id 工艺链定义ID
     * @return 工艺链定义
     */
    public MicroTechnology selectMicroTechnologyById(Long id);

    /**
     * 查询工艺链定义列表
     *
     * @param microTechnology 工艺链定义
     * @return 工艺链定义集合
     */
    public List<MicroTechnology> selectMicroTechnologyList(MicroTechnology microTechnology);

    /**
     * 新增工艺链定义
     *
     * @param microTechnology 工艺链定义
     * @return 结果
     */
    public int insertMicroTechnology(MicroTechnology microTechnology);

    public int insertMicroTechnologyBatch(@Param("dataList") List<MicroTechnology> technologyList); 

    /**
     * 修改工艺链定义
     *
     * @param microTechnology 工艺链定义
     * @return 结果
     */
    public int updateMicroTechnology(MicroTechnology microTechnology);

    /**
     * 删除工艺链定义
     *
     * @param id 工艺链定义ID
     * @return 结果
     */
    public int deleteMicroTechnologyById(Long id);

    /**
     * 批量删除工艺链定义
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroTechnologyByIds(Long[] ids);

    List<MicroStandardEntity> selectStandardProByKey(String key); 

    List<MicroTechnology> judgeStandardProduct(@Param("productCode") String productCode, @Param("productSeq") String productSeq, @Param("productId") Long productId); 

    List<MicroTechDiffDomain> selectTechAllChainForCompare(); 

    List<Long> selectTechIdsByProductIdAndSeq(@Param("dataList") List<MicroTechnology> dataList, @Param("techType") String techType); 

    List<MicroTechSimilarDomain> selectNonStandardProductTechChain(); 

    List<Long> selectProcessByProduct(@Param("key") String key, @Param("productSeq") String productSeq); 

    /**
     * 保存工艺历史
     *
     * @param ids
     * @return
     */
    int saveTechnologyHistory(Long[] ids);

    List<MicroProcessChainBindEntity> selectMicroTechChainByProductIdsOrSeqs(@Param("productIds") List<Long> productIds, @Param("productSeqs") List<String> productSeqs, @Param("techType") String techType); 

    List<MicroTechnology> selectMicroTechnologyByIds(Long[] ids); 

    List<MicroSingleTechChainEntity> selectSingleTechChainByProductIdsOrSeqs(@Param("productIds") List<Long> productIds, @Param("productSeqs") List<String> productSeqs); 

    List<MicroSelectEntity> recommendByProcess(@Param("productCode") String productCode, @Param("operateProcessCode") String operateProcessCode, @Param("preProcessCode") String preProcessCode); 

    List<MicroProcessChainRecommendEntity> recommendByProduct(@Param("productSeq") String productSeq); 

    List<MicroProcessChainRecommendEntity> recommendOperateProcessByProduct(@Param("list") List<String> productSeqList); 

    Set<String> judgeStandardProductByList(@Param("productIds") List<Long> productIds, @Param("productSeqs") List<String> productSeqs); 

    List<String> selectAllProductByTech(@Param("entity") MicroTechnology technology, @Param("patternList") List<Integer> patternList); 
}

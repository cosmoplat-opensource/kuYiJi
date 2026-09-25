/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process;

import com.cosmo.hhim.micro.base.domain.entity.storage.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 库存Service接口
 *
 * @date 2022-10-11
 */
public interface IMicroProcessStorageService {
    /**
     * 查询库存
     *
     * @param id 库存ID
     * @return 库存
     */
    MicroProcessStorage selectMicroProcessStorageById(Long id);

    /**
     * 查询库存列表
     *
     * @param microProcessStorageDto 库存
     * @return 库存集合
     */
    List<MicroProcessStorage> selectMicroProcessStorageList(MicroProcessStorageDto microProcessStorageDto);

    /**
     * 修改库存
     *
     * @param modifyList 库存
     * @return 结果
     */
    int updateMicroProcessStorage(List<MicroProcessStorageModify> modifyList); 

    /**
     * 批量删除库存
     *
     * @param ids 需要删除的库存ID
     * @return 结果
     */
    int deleteMicroProcessStorageByIds(Long[] ids);

    /**
     * 删除库存信息
     *
     * @param id 库存ID
     * @return 结果
     */
    int deleteMicroProcessStorageById(Long id);

    MicroProcessStorage selectMicroProcessStorageByInfo(MicroProcessStorageDto microProcessStorageDto); 

    Map<String, List<? extends MicroSelectEntity>> selectProcessStorageCondition(String key); 

    List<MicroProcessStorage> selectMicroProcessStorageListBySeq(String seqKey, String productSeq, String processSeq); 

    /**
     * 在制品查询 - （产品维度）
     *
     * @param productNameOrCode
     * @param productSeq
     * @return
     */
    List<MicroProcessStorage> selectMicroProcessStorageListByProduct(String productNameOrCode, String productSeq);

    /**
     * 在制品查询: 产品 + 工序
     *
     * @param microProcessStorageDto
     * @return
     */
    List<MicroProcessStorage> selectMicroProcessStorageListByCondition(MicroProcessStorageDto microProcessStorageDto);

    List<MicroProcessStorage> negativeStockList(); 

    List<MicroProcessStorage> ngList(); 

    Map<String, Object> storageHealth(); 

    /**
     * 负库存分析
     *
     * @param productSeq
     * @param processSeq
     * @param startDate
     * @param endDate
     * @return
     */
    NegativeStockAnalysisTip negativeStockAnalysis(String productSeq, String processSeq, Date startDate, Date endDate);

    /**
     * 变更库存 -> 库存变动历史记录
     *
     * @param microWorkSubmit
     * @param dealType
     * @return
     */
    void changeStorage(MicroWorkSubmit microWorkSubmit, String dealType);

    /**
     * 查询库存导出模版所需信息列表
     *
     * @return
     */
    List<MicroProcessStorageExportModelResult> selectAllMicroProcessStorageList();

    /**
     * 根据产品id集合删除空库存
     *
     * @param productIds
     * @return
     */
    int removeZeroProcessStorageByProduct(List<Long> productIds);

    /**
     * 根据工序id集合删除空库存
     *
     * @param processIds
     * @return
     */
    int removeZeroProcessStorageByProcess(List<Long> processIds);
}

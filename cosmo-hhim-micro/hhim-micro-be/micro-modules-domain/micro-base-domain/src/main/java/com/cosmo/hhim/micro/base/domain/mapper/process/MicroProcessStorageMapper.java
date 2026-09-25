/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.process;

import com.cosmo.hhim.micro.base.domain.entity.storage.*;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 库存Mapper接口
 *
 * @date 2022-10-11
 */
public interface MicroProcessStorageMapper {
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
     * 查询库存导出模版所需信息列表
     *
     * @return
     */
    List<MicroProcessStorageExportModelResult> selectAllMicroProcessStorageList();

    /**
     * 新增库存
     *
     * @param microProcessStorage 库存
     * @return 结果
     */
    int insertMicroProcessStorage(MicroProcessStorage microProcessStorage);

    /**
     * 批量新增库存
     *
     * @param microProcessStorageList
     * @return
     */
    int insertMicroProductBatch(@Param("list") List<MicroProcessStorage> microProcessStorageList);

    /**
     * 修改库存
     *
     * @param microProcessStorage 库存
     * @return 结果
     */
    int updateMicroProcessStorage(MicroProcessStorage microProcessStorage);

    /**
     * 批量更新库存记录
     *
     * @param microProcessStorageList
     * @return
     */
    int updateMicroProcessStorageBatch(@Param("list") List<MicroProcessStorage> microProcessStorageList);

    /**
     * 删除库存
     *
     * @param id 库存ID
     * @return 结果
     */
    int deleteMicroProcessStorageById(Long id);

    /**
     * 批量删除库存
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroProcessStorageByIds(Long[] ids);

    /**
     * 清理数据
     *
     * @return
     */
    int deleteAllMicroProcessStorage();

    List<MicroProcessStorage> selectMicroProcessStorageListBySeq(@Param("seqKey") String seqKey, 
                                                                 @Param("productSeq") String productSeq,
                                                                 @Param("processSeq") String processSeq);

    /**
     * 在制品查询 - （产品维度）
     *
     * @param productNameOrCode
     * @param productSeq
     * @return
     */
    List<MicroProcessStorage> selectMicroProcessStorageListByProduct(@Param("productNameOrCode") String productNameOrCode, @Param("productSeq") String productSeq);

    @MapKey("SEQ_KEY")
    Map<String, Map<String, Object>> getProductAndProcessWarningMetrics(); 

    List<MicroProcessStorage> selectNegativeStockList(); 

    List<MicroProcessStorage> selectNgList(); 

    /**
     * 根据库存表查询返回 Map结果
     * <product + process, Object>
     *
     * @return
     */
    @MapKey("seqKey")
    Map<String, StorageForProductAndProcess> selectStorageByProductAndProcess();

    Map<String, Object> selectStorageHealth(); 

    List<MicroProcessStorage> selectStorageByProductAndProcessList(@Param("list") Set<MicroProcessStorageModify> keys); 

    /**
     * 根据产品+工序更新库存良品数量
     *
     * @return
     */
    int updateProcessStorageByProductAndProcess(MicroProcessStorage microProcessStorage); 

    /**
     * 根据产品+工序查询库存信息
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    MicroProcessStorage selectProcessStorageByProductAndProcess(@Param("productSeq") String productSeq, @Param("processSeq") String processSeq);

    /**
     * 根据产品ids查询关联的产品数据
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProductByProductIds(Long[] ids);


    List<Long> selectReStorageByProductIds(Long[] ids); 

    List<Long> selectReStorageHistByProductIds(Long[] ids); 

    /**
     * 根据工序ID获取库存记录
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProcessByProcessIds(Long[] ids);

    List<Long> selectReStorageByProcessIds(Long[] ids); 

    List<Long> selectReStorageHistByProcessIds(Long[] ids); 
}

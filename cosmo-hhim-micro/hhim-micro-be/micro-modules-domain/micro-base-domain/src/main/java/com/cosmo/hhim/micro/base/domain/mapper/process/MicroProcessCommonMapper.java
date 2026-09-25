/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.process;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessSelectEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.SimpleProcessResult;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * processMapper接口
 *
 * @date 2022-10-11
 */
public interface MicroProcessCommonMapper {
    /**
     * 查询process
     *
     * @param id processID
     * @return process
     */
    MicroProcessCommon selectMicroProcessCommonById(Long id);

    /**
     * 根据工序唯一码查询工序基本信息
     *
     * @param processSeq
     * @return
     */
    MicroProcessCommon selectMicroProcessCommonByProcessSeq(String processSeq);

    /**
     * 根据工序编码查询工序信息
     * @param processCode
     * @return
     */
    MicroProcessCommon selectMicroProcessCommonByProcessCode(String processCode);

    /**
     * 根据工序名称查询工序信息
     * @param processName
     * @return
     */
    MicroProcessCommon selectMicroProcessCommonByProcessName(String processName);

    /**
     * 查询process列表
     *
     * @param microProcessCommon process
     * @return process集合
     */
    List<MicroProcessCommon> selectMicroProcessCommonList(MicroProcessCommon microProcessCommon);

    /**
     * 新增process
     *
     * @param microProcessCommon process
     * @return 结果
     */
    int insertMicroProcessCommon(MicroProcessCommon microProcessCommon);

    /**
     * 修改process
     *
     * @param microProcessCommon process
     * @return 结果
     */
    int updateMicroProcessCommon(MicroProcessCommon microProcessCommon);

    /**
     * 删除process
     *
     * @param id processID
     * @return 结果
     */
    int deleteMicroProcessCommonById(Long id);

    /**
     * 批量删除process
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroProcessCommonByIds(Long[] ids);

    List<MicroSelectEntity> selectMicroProcessByName(String key); 

    /**
     * 批量新增process
     *
     * @param microProcessCommons
     * @return
     */
    int insertMicroProcessCommonBatch(@Param("list") List<MicroProcessCommon> microProcessCommons);

    List<String> selectProcessSeqAndCode(); 

    List<MicroProcessSelectEntity> selectMicroProcessStockByName(@Param("key") String key, @Param("productSeq") String productSeq, @Param("tenantCode") String tenantCode); 

    List<MicroProcessCommon> selectExistMicroProcessCommonList(MicroProcessCommon verifyProcess); 

    List<MicroProcessCommon> selectExistByNames(List<String> list); 

    Long selectTotalProcessCount(); 

    List<MicroProcessCommon> selectMasterProcessWarnList(@Param("list") Set<String> collect); 

    List<String> selectFilterProcessBySeqList(@Param("list") List<String> verifyList); 

    List<String> selectProcessByCodeList(@Param("list") List<String> verifyList); 

    List<MicroProcessSelectEntity> selectMicroProcessStockByIds(@Param("ids") List<Long> processIds, @Param("productSeq") String productSeq, @Param("tenantCode") String tenantCode); 

    /**
     * 根据processSeqList来查询最基本的工序信息
     *
     * @param processSeqList
     * @return
     */
    List<SimpleProcessResult> selectSimpleProcessBySeqList(@Param("processSeqList") String processSeqList);

    /**
     * 下拉工序附带库存查询实体
     *
     * @param productSeq
     * @param processList
     * @param tenantCode
     * @return
     */
    List<MicroProcessSelectEntity> selectMicroProcessStock(@Param("productSeq") String productSeq, @Param("processList") List<String> processList, @Param("tenantCode") String tenantCode); 

    @MapKey("processSeq")
    HashMap<String, MicroProcessCommon> selectMicroProcessBySeqs(@Param("processList") Set<String> headProcess); 
}

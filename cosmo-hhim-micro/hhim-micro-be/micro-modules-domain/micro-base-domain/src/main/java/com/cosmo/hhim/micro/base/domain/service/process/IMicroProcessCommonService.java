/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessSelectEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.SimpleProcessResult;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.util.List;
import java.util.Map;

/**
 * processService接口
 *
 * @date 2022-10-11
 */
public interface IMicroProcessCommonService {
    /**
     * 查询process
     *
     * @param id processID
     * @return process
     */
    MicroProcessCommon selectMicroProcessCommonById(Long id);

    /**
     * 根据工序名称查询工序信息
     *
     * @param processName
     * @return
     */
    MicroProcessCommon selectMicroProcessCommonByName(String processName);


    /**
     * 根据工序编码查询工序信息
     *
     * @param processCode
     * @return
     */
    MicroProcessCommon selectMicroProcessCommonByCode(String processCode);

    /**
     * 查询process列表
     *
     * @param microProcessCommon process
     * @return process集合
     */
    List<MicroProcessCommon> selectMicroProcessCommonList(MicroProcessCommon microProcessCommon);

    /**
     * 查询所有工序列表
     *
     * @return
     */
    List<MicroProcessCommon> selectMicroAllProcessCommonList();

    /**
     * 新增process
     *
     * @param microProcessCommon process
     * @return 结果
     */
    MicroProcessCommon insertMicroProcessCommon(MicroProcessCommon microProcessCommon);

    /**
     * 修改process
     *
     * @param microProcessCommon process
     * @return 结果
     */
    int updateMicroProcessCommon(MicroProcessCommon microProcessCommon);

    /**
     * 批量删除process
     *
     * @param ids 需要删除的processID
     * @return 结果
     */
    int deleteMicroProcessCommonByIds(Long[] ids);

    /**
     * 删除process信息
     *
     * @param id processID
     * @return 结果
     */
    int deleteMicroProcessCommonById(Long id);

    /**
     * 生成新的工序
     *
     * @param processName
     * @return
     */
    MicroProcessCommon createNewProcess(String processName);

    /**
     * 根据模糊查询key值/产品序列码/是否圈定范围来查询工序下拉列表
     * 优先级productSeq > key
     *
     * @param key
     * @param productSeq
     * @param range      代表是否要在工艺链范围内获取工序数据
     * @return
     */
    List<MicroProcessSelectEntity> selectMicroProcessByName(String key, String productSeq, boolean range);

    /**
     * 是否有相似的工序名称
     *
     * @param processName
     * @return
     */
    MicroProcessCommon isOrNotHaveProcess(String processName);

    int insertExperienceProcess(List<String> processList); 

    /**
     * 根据产品seq获取工序列表(报工通过审核的数据)
     *
     * @param key
     * @param productSeq
     * @return
     */
    List<MicroSelectEntity> selectByChain(String key, String productSeq);

    /**
     * 针对有工艺的产品，根据输入的前工序或者当前工序的信息找出相对应的当前工序或者前工序的信息
     *
     * @param bindEntityList
     * @param operateProcessSeq
     * @return
     */
    List<SimpleProcessResult> defaultRecommendByStandardTech(List<MicroProcessChainBindEntity> bindEntityList, String operateProcessSeq); 

    /**
     * 过滤有效的产品和工序
     *
     * @param recommendResult
     * @return
     */
    Map<String, Object> filterInvalidProductAndProcess(Map<String, Object> recommendResult);
}

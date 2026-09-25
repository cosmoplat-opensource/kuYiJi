/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.mapper;

import java.util.List;

import com.cosmo.hhim.micro.ng.domain.entity.*;
import org.apache.ibatis.annotations.Param;

/**
 * 质检记录Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
public interface MicroQualityControlRecordMapper 
{
    /**
     * 查询质检记录
     * 
     * @param id 质检记录ID
     * @return 质检记录
     */
    MicroQualityControlRecord selectMicroQualityControlRecordById(Long id);

    /**
     * 查询质检记录列表
     * 
     * @param microQualityControlRecord 质检记录
     * @return 质检记录集合
     */
    List<MicroQualityControlRecord> selectMicroQualityControlRecordList(MicroQualityControlRecord microQualityControlRecord);

    /**
     * 根据报工记录idList 批量查询质检信息
     *
     * @param submitIdList
     * @return
     */
    List<MicroQualityControlRecord> selectMicroQualityControlRecordBySubmitIdList(@Param("list") List<Long> submitIdList);

    /**
     * 新增质检记录
     * 
     * @param microQualityControlRecord 质检记录
     * @return 结果
     */
    int insertMicroQualityControlRecord(MicroQualityControlRecord microQualityControlRecord);

    /**
     * 批量插入质检记录信息
     *
     * @param microQualityControlRecordList
     * @return
     */
    int batchInsertMicroQualityControlRecord(List<MicroQualityControlRecord> microQualityControlRecordList);

    /**
     * 修改质检记录
     * 
     * @param microQualityControlRecord 质检记录
     * @return 结果
     */
    int updateMicroQualityControlRecord(MicroQualityControlRecord microQualityControlRecord);

    /**
     * 删除质检记录
     * 
     * @param id 质检记录ID
     * @return 结果
     */
    int deleteMicroQualityControlRecordById(Long id);

    /**
     * 批量删除质检记录
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroQualityControlRecordByIds(Long[] ids);

    /**
     * 获取不良品类型 (根据已有质检记录)
     *
     * @param ngTypeQueryParam
     * @return
     */
    List<String> selectNgTypeList(NgTypeQueryParam ngTypeQueryParam);

    /**
     * 按照质检记录号查询质检记录信息
     *
     * @param qualityControlRecordNoList
     * @return
     */
    List<MicroQualityControlRecord> selectMicroQualityControlRecordListByQualityControlRecordNos(@Param("list") List<String> qualityControlRecordNoList);

    /**
     * 按照质检记录号汇总质检记录历史信息
     *
     * @param qualityControlHistoryQueryParam
     * @return
     */
    List<QualityControlHistoryInfo> selectQualityControlHistoryInfoList(QualityControlHistoryQueryParam qualityControlHistoryQueryParam);

    /**
     * 不良类型统计分析
     *
     * @param ngTypeStatisticQueryParam
     * @return
     */
    List<NgTypeStatisticInfo> selectDifferentNgType(NgTypeStatisticQueryParam ngTypeStatisticQueryParam);
}

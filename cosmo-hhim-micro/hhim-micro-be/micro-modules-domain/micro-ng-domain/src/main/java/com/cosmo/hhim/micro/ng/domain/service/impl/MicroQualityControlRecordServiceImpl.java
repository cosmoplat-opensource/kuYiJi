/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.ng.domain.entity.*;
import com.cosmo.hhim.micro.ng.domain.mapper.MicroQualityControlRecordMapper;
import com.cosmo.hhim.micro.ng.domain.service.IMicroQualityControlRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 质检记录Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
@Slf4j
@Service
public class MicroQualityControlRecordServiceImpl implements IMicroQualityControlRecordService
{
    @Autowired
    private MicroQualityControlRecordMapper microQualityControlRecordMapper;

    /**
     * 查询质检记录
     * 
     * @param id 质检记录ID
     * @return 质检记录
     */
    @Override
    public MicroQualityControlRecord selectMicroQualityControlRecordById(Long id)
    {
        return microQualityControlRecordMapper.selectMicroQualityControlRecordById(id);
    }

    /**
     * 查询质检记录列表
     * 
     * @param microQualityControlRecord 质检记录
     * @return 质检记录
     */
    @Override
    public List<MicroQualityControlRecord> selectMicroQualityControlRecordList(MicroQualityControlRecord microQualityControlRecord)
    {
        return microQualityControlRecordMapper.selectMicroQualityControlRecordList(microQualityControlRecord);
    }

    /**
     * 新增质检记录
     * 
     * @param microQualityControlRecord 质检记录
     * @return 结果
     */
    @Override
    public int insertMicroQualityControlRecord(MicroQualityControlRecord microQualityControlRecord)
    {
        return microQualityControlRecordMapper.insertMicroQualityControlRecord(microQualityControlRecord);
    }

    @Override
    public int batchInsertMicroQualityControlRecord(List<MicroQualityControlRecord> microQualityControlRecordList) {

//        if (CollectionUtils.isEmpty(microQualityControlRecordList)) {
//            throw new CustomException("质检记录信息为空");
//        }

        return microQualityControlRecordMapper.batchInsertMicroQualityControlRecord(microQualityControlRecordList);
    }

    /**
     * 修改质检记录
     * 
     * @param microQualityControlRecord 质检记录
     * @return 结果
     */
    @Override
    public int updateMicroQualityControlRecord(MicroQualityControlRecord microQualityControlRecord)
    {
        return microQualityControlRecordMapper.updateMicroQualityControlRecord(microQualityControlRecord);
    }

    /**
     * 批量删除质检记录
     * 
     * @param ids 需要删除的质检记录ID
     * @return 结果
     */
    @Override
    public int deleteMicroQualityControlRecordByIds(Long[] ids)
    {
        return microQualityControlRecordMapper.deleteMicroQualityControlRecordByIds(ids);
    }

    /**
     * 删除质检记录信息
     * 
     * @param id 质检记录ID
     * @return 结果
     */
    @Override
    public int deleteMicroQualityControlRecordById(Long id)
    {
        return microQualityControlRecordMapper.deleteMicroQualityControlRecordById(id);
    }

    /**
     * 获取不良品类型 (根据已有质检记录)
     *
     * @param ngTypeQueryParam
     * @return
     */
    @Override
    public List<String> selectNgTypeList(NgTypeQueryParam ngTypeQueryParam) {
        List<String> ngTypeList = microQualityControlRecordMapper.selectNgTypeList(ngTypeQueryParam);
        if (!CollectionUtils.isEmpty(ngTypeList)) {
            ngTypeList = ngTypeList.stream().filter(obj -> obj != null).collect(Collectors.toList());
        }
        return ngTypeList;
    }

    /**
     * 根据报工id组装质检信息
     *
     * @param submitIdList
     * @return
     */
    @Override
    public List<QualityControlInfoWhenRepair> selectDetailQualityControlInfo(List<Long> submitIdList) {
        List<MicroQualityControlRecord> microQualityControlRecords = microQualityControlRecordMapper.selectMicroQualityControlRecordBySubmitIdList(submitIdList);
        List<QualityControlInfoWhenRepair> qualityControlInfoWhenRepairList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(microQualityControlRecords)) {
            // 防止一个报工记录对应多个质检记录信息，根据报工记录号作区分
            Map<String, List<MicroQualityControlRecord>> map = microQualityControlRecords.stream().collect(Collectors.groupingBy(obj -> obj.getSubmitId() + "-" + obj.getQualityControlRecordNo()));
            for (Map.Entry<String, List<MicroQualityControlRecord>> entry : map.entrySet()) {
                QualityControlInfoWhenRepair qualityControlInfoWhenRepairTemp = new QualityControlInfoWhenRepair();
                List<NgProductDetailInfo> ngProductDetailInfoList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(entry.getValue())) {
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        if (i == 0) {
                            qualityControlInfoWhenRepairTemp.setQcDate(entry.getValue().get(i).getCreatedDate());
                            qualityControlInfoWhenRepairTemp.setQcNickName(entry.getValue().get(i).getQcNickName());
                        }
                        NgProductDetailInfo temp = new NgProductDetailInfo();
                        temp.setNgNum(entry.getValue().get(i).getNgNum());
                        temp.setNgType(entry.getValue().get(i).getNgType());
                        ngProductDetailInfoList.add(temp);
                    }
                }
                qualityControlInfoWhenRepairTemp.setTotalNgNum(ngProductDetailInfoList.stream().map(NgProductDetailInfo::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add));
                qualityControlInfoWhenRepairTemp.setNgProductDetailInfoList(ngProductDetailInfoList);
                qualityControlInfoWhenRepairList.add(qualityControlInfoWhenRepairTemp);
            }
        }
        // 按照质检时间倒序排
        if (qualityControlInfoWhenRepairList.size() > 1) {
            qualityControlInfoWhenRepairList.sort(Comparator.comparing(QualityControlInfoWhenRepair::getQcDate).reversed());
        }
        return qualityControlInfoWhenRepairList;
    }

    /**
     * 查询质检记录历史
     *
     * @param qualityControlHistoryQueryParam
     * @return
     */
    @Override
    public List<QualityControlHistoryInfo> selectQualityControlHistoryInfoList(QualityControlHistoryQueryParam qualityControlHistoryQueryParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(qualityControlHistoryQueryParam));
        List<QualityControlHistoryInfo> qualityControlHistoryInfos = microQualityControlRecordMapper.selectQualityControlHistoryInfoList(qualityControlHistoryQueryParam);
        if (!qualityControlHistoryInfos.isEmpty()) {
            List<String> qualityControlRecordNoList = qualityControlHistoryInfos.stream().map(QualityControlHistoryInfo::getQualityControlRecordNo).collect(Collectors.toList());
            List<MicroQualityControlRecord> microQualityControlRecordList = microQualityControlRecordMapper.selectMicroQualityControlRecordListByQualityControlRecordNos(qualityControlRecordNoList);
            Map<String, List<MicroQualityControlRecord>> map = microQualityControlRecordList.stream().collect(Collectors.groupingBy(MicroQualityControlRecord::getQualityControlRecordNo));
            for (QualityControlHistoryInfo qualityControlHistoryInfo : qualityControlHistoryInfos) {
                List<MicroQualityControlRecord> microQualityControlRecords = map.get(qualityControlHistoryInfo.getQualityControlRecordNo());
                // 存储不良品类型及数量
                List<NgProductDetailInfo> ngProductDetailInfoList = new ArrayList<>();
                if (!microQualityControlRecords.isEmpty()) {
                    for (MicroQualityControlRecord microQualityControlRecord : microQualityControlRecords) {
                        NgProductDetailInfo temp = new NgProductDetailInfo();
                        temp.setNgNum(microQualityControlRecord.getNgNum());
                        temp.setNgType(microQualityControlRecord.getNgType());
                        ngProductDetailInfoList.add(temp);
                    }
                }
                qualityControlHistoryInfo.setNgProductDetailInfoList(ngProductDetailInfoList);
            }
        }
        return qualityControlHistoryInfos;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.submit;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.micro.application.dto.submit.MicroWorkSubmitMultiMixedDTO;
import com.cosmo.hhim.micro.application.dto.submit.MicroWorkSubmitRecordExportDto;
import com.cosmo.hhim.micro.application.dto.submit.SubmitInfoInDifferentStatusByUser;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementReportEntity;
import com.cosmo.hhim.micro.complete.domain.enums.MicroSettlementStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ExpiredRecordFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ng.CheckStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.ng.domain.entity.MicroQualityControlRecord;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description:
 * @date 2023/3/17 17:10
 */
public class MicroWorkSubmitAssembler {

    /**
     * 转报工记录DOMAIN
     *
     * @param microWorkSubmitDto
     * @return
     */
    public static MicroWorkSubmit toMicroWorkSubmitDomain(MicroWorkSubmitDto microWorkSubmitDto) {
        MicroWorkSubmit workSubmit = new MicroWorkSubmit();
        BeanUtils.copyProperties(microWorkSubmitDto, workSubmit);
        return workSubmit;
    }

    public static MicroManufactureTask toManufactureTaskDomain(MicroWorkSubmit microWorkSubmit) {
        MicroManufactureTask temp = new MicroManufactureTask();
        temp.setWorkOrderNo(microWorkSubmit.getWorkOrderNo());
        temp.setTaskNo(microWorkSubmit.getTaskNo());
        temp.setPassNum(microWorkSubmit.getCheckPassNum());
        temp.setNgNum(microWorkSubmit.getCheckNgNum());
        temp.setIsLastProcess(microWorkSubmit.getIsLastProcess());
        return temp;
    }

    public static List<MicroWorkSubmit> multiMixedToMicroWorkSubmitDomain(List<MicroWorkSubmitMultiMixedDTO> multiMixedList) {
        List<MicroWorkSubmit> result = new ArrayList<>();
        MicroWorkSubmit submit;
        for (MicroWorkSubmitMultiMixedDTO multiMixedDTO : multiMixedList) {
            submit = new MicroWorkSubmit();
            BeanUtils.copyProperties(multiMixedDTO, submit);
            result.add(submit);
        }
        return result;
    }

    public static List<MicroWorkSubmitRecordExportDto> toMicroWorkSubmitRecordExportDto(List<MicroWorkSubmit> microWorkSubmitList,
                                                                                        List<MicroQualityControlRecord> microQualityControlRecordList,
                                                                                        List<MicroSettlementReportEntity> microSettlementReportEntityList) {
        List<MicroWorkSubmitRecordExportDto> microWorkSubmitRecordExportDtoList = new ArrayList<>();
        Map<Long, MicroQualityControlRecord> qualityControlRecordMap = new HashMap<>(16);
        if (!CollectionUtils.isEmpty(microQualityControlRecordList)) {
            qualityControlRecordMap = microQualityControlRecordList.stream().collect(Collectors.groupingBy(MicroQualityControlRecord::getSubmitId,
                    Collectors.collectingAndThen(Collectors.toList(), value -> value.get(0))));
        }
        // 获取结算总数量和最终的结算时间
        Map<Long, List<MicroSettlementReportEntity>> settlementReportMap = new HashMap<>(16);
        if (!CollectionUtils.isEmpty(microSettlementReportEntityList)) {
            settlementReportMap = microSettlementReportEntityList.stream()
                    .filter(obj -> obj.getSettlementStatus().equals(MicroSettlementStatusEnum.SETTLED.getCode()))
                    .collect(Collectors.groupingBy(MicroSettlementReportEntity::getSubmitId));
        }
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
            MicroWorkSubmitRecordExportDto temp = new MicroWorkSubmitRecordExportDto();
            temp.setSubmitNickName(microWorkSubmit.getSubmitNickName());
            temp.setProductCode(microWorkSubmit.getProductCode());
            temp.setProductName(microWorkSubmit.getProductName());
            temp.setProcessCode(microWorkSubmit.getOperateProcessCode());
            temp.setProcessName(microWorkSubmit.getOperateProcessName());
            if (microWorkSubmit.getCheckPassNum() != null && microWorkSubmit.getCheckNgNum() != null) {
                temp.setPassNum(microWorkSubmit.getCheckPassNum().stripTrailingZeros().toPlainString());
                temp.setNgNum(microWorkSubmit.getCheckNgNum().stripTrailingZeros().toPlainString());
            } else {
                temp.setPassNum(microWorkSubmit.getPassNum().stripTrailingZeros().toPlainString());
                temp.setNgNum(microWorkSubmit.getNgNum().stripTrailingZeros().toPlainString());
            }
            temp.setRemark(microWorkSubmit.getRemark());
            // 如果是补录的设置补录时间
            if (ExpiredRecordFlagEnum.YES.getCode().equals(microWorkSubmit.getExpiredRecordFlag())) {
                temp.setSubmitDate(microWorkSubmit.getSubmitDay());
            } else {
                temp.setSubmitDate(microWorkSubmit.getCreatedDate());
            }
            // 审核相关信息
            temp.setSubmitStatus(SubmitStatusEnum.getEnumDesc(microWorkSubmit.getSubmitStatus()));
            temp.setCheckNickName(microWorkSubmit.getCheckNickName());
            temp.setCheckDate(microWorkSubmit.getCheckDate());
            // 质检相关信息
            temp.setCheckStatus(CheckStatusEnum.getEnumDesc(microWorkSubmit.getCheckStatus()));
            if (!qualityControlRecordMap.isEmpty()) {
                MicroQualityControlRecord microQualityControlRecord = qualityControlRecordMap.get(microWorkSubmit.getId());
                if (microQualityControlRecord != null) {
                    temp.setQcNickName(microQualityControlRecord.getQcNickName());
                }
            }
            temp.setQcDate(microWorkSubmit.getQcDate());
            // 返修数量
            temp.setRepairNum(microWorkSubmit.getRepairNum().stripTrailingZeros().toPlainString());
            // 结算数量和结算时间
            if (!settlementReportMap.isEmpty()) {
                List<MicroSettlementReportEntity> settlementReportEntityTempList = settlementReportMap.get(microWorkSubmit.getId());
                if (!CollectionUtils.isEmpty(settlementReportEntityTempList)) {
                    BigDecimal settledNum = settlementReportEntityTempList.stream().map(MicroSettlementReportEntity::getSettledNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                    Date settledDate = settlementReportEntityTempList.stream().max(Comparator.comparing(MicroSettlementReportEntity::getSettlementDay)).get().getSettlementDay();
                    temp.setSettledNum(settledNum.stripTrailingZeros().toPlainString());
                    temp.setSettledDate(settledDate);
                } else {
                    temp.setSettledNum(BigDecimal.ZERO.stripTrailingZeros().toPlainString());
                }
            } else {
                temp.setSettledNum(BigDecimal.ZERO.stripTrailingZeros().toPlainString());
            }
            microWorkSubmitRecordExportDtoList.add(temp);
        }
        return microWorkSubmitRecordExportDtoList;
    }

    /**
     * 转换为待审核或者驳回列表的实体信息
     *
     * @param microWorkSubmitList
     * @return
     */
    public static List<SubmitInfoInDifferentStatusByUser> microWorkSubmitDomain2SubmitInfoInDifferentStatusByUser(List<MicroWorkSubmit> microWorkSubmitList) {
        List<SubmitInfoInDifferentStatusByUser> submitInfoInDifferentStatusByUserList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(microWorkSubmitList)) {
            for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
                SubmitInfoInDifferentStatusByUser temp = new SubmitInfoInDifferentStatusByUser();
                temp.setId(microWorkSubmit.getId());
                temp.setProductCode(microWorkSubmit.getProductCode());
                temp.setProductName(microWorkSubmit.getProductName());
                temp.setProductSeq(microWorkSubmit.getProductSeq());
                temp.setProcessCode(microWorkSubmit.getOperateProcessCode());
                temp.setProcessName(microWorkSubmit.getOperateProcessName());
                temp.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
                temp.setPassNum(microWorkSubmit.getPassNum());
                temp.setNgNum(microWorkSubmit.getNgNum());
                temp.setTotalNum(temp.getPassNum().add(temp.getNgNum()));
                temp.setCreatedDate(microWorkSubmit.getCreatedDate());
                temp.setSubmitStatus(microWorkSubmit.getSubmitStatus());
                submitInfoInDifferentStatusByUserList.add(temp);
            }
            return MicroPageUtils.listToPage(microWorkSubmitList,  submitInfoInDifferentStatusByUserList);
        }
        return Collections.emptyList();
    }
}

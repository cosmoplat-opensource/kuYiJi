/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.complete;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementModifyDTO;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementQueryDTO;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementUserDetailExportDTO;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.complete.domain.entity.*;
import com.cosmo.hhim.micro.complete.domain.enums.MicroSettlementStatusEnum;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
public class MicroSettlementAssembler {
    public static MicroSettlementReportEntity toReportDTO(MicroSettlementModifyDTO modifyDTO) {
        MicroSettlementReportEntity reportEntity = new MicroSettlementReportEntity();
        reportEntity.setEmployeeId(modifyDTO.getEmployeeId());
        reportEntity.setAdjustedNum(modifyDTO.getAdjustedNum());
        reportEntity.setProductSeq(modifyDTO.getProductSeq());
        reportEntity.setOperateProcessSeq(modifyDTO.getOperateProcessSeq());
        reportEntity.setRemark(modifyDTO.getRemark());
        reportEntity.setCheckedNum(modifyDTO.getOldNum());
        reportEntity.setProductCode(modifyDTO.getProductCode());
        reportEntity.setProductName(modifyDTO.getProductName());
        reportEntity.setOperateProcessCode(modifyDTO.getOperateProcessCode());
        reportEntity.setOperateProcessName(modifyDTO.getOperateProcessName());
        return reportEntity;
    }


    public static List<MicroSettledDomain> completeToSettleDomain(List<MicroCompleteReportStatisticsProductResult> completeProducts) {
        List<MicroSettledDomain> result = new ArrayList<>();
        MicroSettledDomain settledDomain;
        for (MicroCompleteReportStatisticsProductResult completeProduct : completeProducts) {
            settledDomain = new MicroSettledDomain();
            String productSeq = completeProduct.getProductSeq();
            BigDecimal completeTotalNum = completeProduct.getCompleteTotalNum();
            settledDomain.setProductSeq(productSeq);
            settledDomain.setProductNum(completeTotalNum);
            settledDomain.setCompleteReportNoArrayStr(completeProduct.getReportNoArrayStr());
            result.add(settledDomain);
        }
        return result;
    }

    public static List<MicroSettlementGenerateDomain> submitToSettlement(List<MicroWorkSubmit> submitList) {
        List<MicroSettlementGenerateDomain> settlementList = new ArrayList<>();
        MicroSettlementGenerateDomain generateDomain;
        for (MicroWorkSubmit microWorkSubmit : submitList) {
            generateDomain = new MicroSettlementGenerateDomain();
            generateDomain.setProductSeq(microWorkSubmit.getProductSeq());
            generateDomain.setProductName(microWorkSubmit.getProductName());
            generateDomain.setProductCode(microWorkSubmit.getProductCode());
            generateDomain.setProductUnit(microWorkSubmit.getUnit());
            generateDomain.setOperateProcessSeq(microWorkSubmit.getOperateProcessSeq());
            generateDomain.setOperateProcessCode(microWorkSubmit.getOperateProcessCode());
            generateDomain.setOperateProcessName(microWorkSubmit.getOperateProcessName());
            generateDomain.setCheckedNum(microWorkSubmit.getCheckPassNum());
            generateDomain.setSubmitId(microWorkSubmit.getId());
            generateDomain.setEmployeeId(Long.valueOf(microWorkSubmit.getSubmitUser()));
            settlementList.add(generateDomain);
        }
        return settlementList;
    }

    public static List<MicroSettlementUserDetailExportDTO> toExportDetailDTO(List<MicroSettlementReportDomain> domainList, MicroSettlementStatusEnum statusEnum) {
        List<MicroSettlementUserDetailExportDTO> result = new ArrayList<>();
        MicroSettlementUserDetailExportDTO detailDTO;
        List<MicroSettlementDetailDomain> detailList;
        for (MicroSettlementReportDomain reportDomain : domainList) {
            detailList = reportDomain.getDetailList();
            if (!CollectionUtils.isEmpty(detailList)) {
                for (MicroSettlementDetailDomain domain : detailList) {
                    detailDTO = new MicroSettlementUserDetailExportDTO();
                    BeanUtil.copyProperties(domain, detailDTO);
                    if (statusEnum.equals(MicroSettlementStatusEnum.SETTLED)) {
                        detailDTO.setAdjustedNum(domain.getSettledNum());
                    }
                    detailDTO.setEmployeeUserName(reportDomain.getEmployeeUserName());
                    detailDTO.setEmployeeName(reportDomain.getEmployeeName());
                    result.add(detailDTO);
                }
            }
        }
        return result;
    }


    public static MicroSettlementReportEntity toReportEntity(MicroSettlementQueryDTO queryDTO) {
        MicroSettlementReportEntity entity = new MicroSettlementReportEntity();
        entity.setProductSeq(queryDTO.getProductSeq());
        entity.setEmployeeId(queryDTO.getUserId());
        entity.setOperateProcessSeq(queryDTO.getOperateProcessSeq());
        entity.setSettlementDay(queryDTO.getSearchDate());
        return entity;
    }

    /**
     * 组装结算生成的报工记录变动历史
     *
     * @param microWorkSubmitList
     * @param microSettlementReportEntityList
     * @return
     */
    public static List<MicroWorkSubmitHistory> settlementReportEntityAndWorkSubmitCovert2MicroWorkSubmitHistory(List<MicroWorkSubmit> microWorkSubmitList,
                                                                                                                List<MicroSettlementReportEntity> microSettlementReportEntityList) {
        String userId = SecurityUtils.getUserId().toString();
        Date currentDate = DateUtils.getNowDate();
        // 要插入的报工变动历史
        List<MicroWorkSubmitHistory> microWorkSubmitHistoryList = new ArrayList<>();
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
            List<MicroSettlementReportEntity> settlementReportEntityListTemp = microSettlementReportEntityList.stream()
                    .filter(obj -> obj.getSubmitId().equals(microWorkSubmit.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(settlementReportEntityListTemp)) {
                MicroWorkSubmitHistory temp = new MicroWorkSubmitHistory();
                BeanUtils.copyProperties(microWorkSubmit, temp);
                temp.setCreatedBy(userId);
                temp.setCreatedDate(currentDate);
                temp.setOperateNode(CommonConstants.SUBMIT_HISTORY_SETTLED);
                BigDecimal settledNum = settlementReportEntityListTemp
                        .stream()
                        .map(MicroSettlementReportEntity::getSettledNum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                temp.setSettledNum(settledNum);
                temp.setPassNum(BigDecimal.ZERO);
                temp.setNgNum(BigDecimal.ZERO);
                temp.setRepairNum(BigDecimal.ZERO);
                temp.setConcessionNum(BigDecimal.ZERO);
                temp.setAbandonedNum(BigDecimal.ZERO);
                microWorkSubmitHistoryList.add(temp);
            }
        }
        return microWorkSubmitHistoryList;
    }
}

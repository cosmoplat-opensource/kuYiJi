/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.ng;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.ng.DetailQualityControlInfo;
import com.cosmo.hhim.micro.application.dto.ng.QualityControlOfSubmitRecordInfo;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.ng.domain.entity.MicroQualityControlRecord;
import com.cosmo.hhim.micro.ng.domain.entity.NgProductDetailInfo;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检记录组装类
 * @date 2023/4/4 09:39
 */
public class MicroQualityControlRecordAssembler {

    /**
     * 组合质检明细信息
     *
     * @param microWorkSubmit
     * @param qcNickName
     * @param remark
     * @param qcDate
     * @param checkPassNum
     * @param ngProductDetailInfoList
     * @return
     */
    public static DetailQualityControlInfo covertDomain2DetailQualityControlInfo(MicroWorkSubmit microWorkSubmit,
                                                                                 String qcNickName,
                                                                                 String remark,
                                                                                 Date qcDate,
                                                                                 BigDecimal checkPassNum,
                                                                                 List<NgProductDetailInfo> ngProductDetailInfoList) {
        DetailQualityControlInfo result = new DetailQualityControlInfo();
        result.setProductCode(microWorkSubmit.getProductCode());
        result.setProductName(microWorkSubmit.getProductName());
        result.setProductUnit(microWorkSubmit.getUnit());
        result.setProcessName(microWorkSubmit.getOperateProcessName());
        result.setPreProcessName(microWorkSubmit.getPreProcessName());
        result.setPassNum(microWorkSubmit.getPassNum());
        result.setNgNum(microWorkSubmit.getNgNum());
        result.setSubmitNickName(microWorkSubmit.getSubmitNickName());
        result.setSubmitDate(microWorkSubmit.getCreatedDate());
        result.setSubmitRecordRemark(microWorkSubmit.getRemark());
        // 最新质检的数量信息(从质检记录中获取，不能从报工记录取，报工记录可能是审核的数量)
        if (!CollectionUtils.isEmpty(ngProductDetailInfoList)) {
            BigDecimal checkNgNum = ngProductDetailInfoList.stream().map(NgProductDetailInfo::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.setCheckNgNum(checkNgNum);
        }
        result.setCheckPassNum(checkPassNum);
        result.setRepairNum(microWorkSubmit.getRepairNum());
        result.setAbandonedNum(microWorkSubmit.getAbandonedNum());
        result.setQcNickName(qcNickName);
        result.setRemark(remark);
        result.setQcDate(qcDate);
        result.setNgProductDetailInfoList(ngProductDetailInfoList);
        return result;
    }

    /**
     * 组合质检列表返回信息
     *
     * @param microWorkSubmitList
     * @param map
     * @return
     */
    public static List<QualityControlOfSubmitRecordInfo> covertDomain2QualityControlOfSubmitRecordInfo(List<MicroWorkSubmit> microWorkSubmitList,
                                                                                                       Map<Long, List<MicroQualityControlRecord>> map) {
        List<QualityControlOfSubmitRecordInfo> result = new ArrayList<>();
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
            QualityControlOfSubmitRecordInfo temp = new QualityControlOfSubmitRecordInfo();
            temp.setId(microWorkSubmit.getId());
            temp.setProductCode(microWorkSubmit.getProductCode());
            temp.setProductSeq(microWorkSubmit.getProductSeq());
            temp.setProductName(microWorkSubmit.getProductName());
            temp.setProductUnit(microWorkSubmit.getUnit());
            temp.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
            temp.setProcessName(microWorkSubmit.getOperateProcessName());
            temp.setPreProcessName(microWorkSubmit.getPreProcessName());
            temp.setPreProcessSeq(microWorkSubmit.getPreProcessSeq());
            temp.setIsFirstProcess(microWorkSubmit.getIsFirstProcess());
            temp.setIsLastProcess(microWorkSubmit.getIsLastProcess());
            temp.setPassNum(microWorkSubmit.getPassNum());
            temp.setNgNum(microWorkSubmit.getNgNum());
            temp.setSubmitDate(microWorkSubmit.getCreatedDate());
            temp.setSubmitNickName(microWorkSubmit.getSubmitNickName());
            // 质检列表显示的是报工记录的备注信息
            temp.setRemark(microWorkSubmit.getRemark());
            temp.setCheckStatus(microWorkSubmit.getCheckStatus());
            // 如果map不为空，说明他是质检过的, 重新设置质检列表的备注、数量等信息
            if (!map.isEmpty()) {
                List<MicroQualityControlRecord> microQualityControlRecordListTemp = map.get(microWorkSubmit.getId());
                if (!CollectionUtils.isEmpty(microQualityControlRecordListTemp)) {
                    BigDecimal passNum = microQualityControlRecordListTemp.stream().map(MicroQualityControlRecord::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal ngNum = microQualityControlRecordListTemp.stream().map(MicroQualityControlRecord::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                    temp.setQcDate(microQualityControlRecordListTemp.get(0).getCreatedDate());
                    temp.setQcNickName(microQualityControlRecordListTemp.get(0).getQcNickName());
                    temp.setPassNum(passNum);
                    temp.setNgNum(ngNum);
                }
            }
            result.add(temp);
        }
        // 防止分页失效
        return MicroPageUtils.listToPage(microWorkSubmitList, result);
    }

    /**
     * 组装质检记录
     *
     * @param microWorkSubmit
     * @param ngProductDetailInfo
     * @return
     */
    public static MicroQualityControlRecord covertMicroWorkSubmitDomain2MicroQualityControlRecordDomain(MicroWorkSubmit microWorkSubmit, Date currentDate, String qualityControlRecordNo,  NgProductDetailInfo ngProductDetailInfo) {
        MicroQualityControlRecord temp = new MicroQualityControlRecord();
        temp.setQualityControlRecordNo(qualityControlRecordNo);
        temp.setSubmitId(microWorkSubmit.getId());
        temp.setProductSeq(microWorkSubmit.getProductSeq());
        temp.setPreProcessSeq(microWorkSubmit.getPreProcessSeq());
        temp.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
        if (ngProductDetailInfo != null) {
            temp.setNgType(ngProductDetailInfo.getNgType());
            temp.setNgNum(ngProductDetailInfo.getNgNum());
        } else {
            temp.setNgType(null);
            temp.setNgNum(null);
        }
        temp.setSubmitUser(microWorkSubmit.getSubmitUser());
        temp.setSubmitDate(microWorkSubmit.getCreatedDate());
        temp.setCreatedBy(SecurityUtils.getUserId());
        temp.setCreatedDate(currentDate);
        temp.setLastUpdBy(SecurityUtils.getUserId());
        temp.setLastUpdDate(currentDate);
        temp.setActiveFlag("1");
        temp.setTenantCode(ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
        return temp;
    }
}

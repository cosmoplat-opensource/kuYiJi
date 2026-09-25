/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.ng;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.ng.NgProductMailExportResult;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgInfoByProductAndProcessAndUser;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.util.DateUtil;
import com.cosmo.hhim.micro.ng.domain.entity.MicroRepairRecord;
import com.cosmo.hhim.micro.ng.domain.entity.RepairNumInfoByProductAndProcessAndUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description: 返修记录组装类
 * @date 2023/4/6 17:30
 */
@Data
public class MicroRepairRecordAssembler {

    /**
     * 组装质检记录
     *
     * @param microWorkSubmit
     * @param repairNo
     * @param repairUser
     * @param remark
     * @return
     */
    public static MicroRepairRecord microWorkSubmitDomain2MicroRepairRecordDomain(MicroWorkSubmit microWorkSubmit, String repairNo, Long repairUser, String remark) {
        MicroRepairRecord microRepairRecord = new MicroRepairRecord();
        microRepairRecord.setRepairNo(repairNo);
        // 返修记录中要记录维修人
        microRepairRecord.setRepairUser(repairUser);
        microRepairRecord.setSubmitId(microWorkSubmit.getId());
        microRepairRecord.setProductSeq(microWorkSubmit.getProductSeq());
        microRepairRecord.setProcessSeq(microWorkSubmit.getOperateProcessSeq());
        microRepairRecord.setPreProcessSeq(microWorkSubmit.getPreProcessSeq());
        microRepairRecord.setSubmitUser(Long.valueOf(microWorkSubmit.getSubmitUser()));
        // 初始化各种数量为0
        microRepairRecord.setRepairNum(BigDecimal.ZERO);
        microRepairRecord.setConcessionNum(BigDecimal.ZERO);
        microRepairRecord.setAbandonedNum(BigDecimal.ZERO);
        // 复核人
        microRepairRecord.setCreatedBy(SecurityUtils.getUserId());
        microRepairRecord.setCreatedDate(DateUtils.getNowDate());
        microRepairRecord.setRemark(remark);
        microRepairRecord.setActiveFlag("1");
        microRepairRecord.setTenantCode(ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
        return microRepairRecord;
    }

    /**
     * 组装用于库存加扣减的报工记录实体
     *
     * @param microWorkSubmits
     * @param microRepairRecords
     * @return
     */
    public static List<MicroWorkSubmit> microWorkSubmitAndMicroRepairRecordDomain2MicroWorkSubmitDomainForChangeStorage(List<MicroWorkSubmit> microWorkSubmits,
                                                                                                                        List<MicroRepairRecord> microRepairRecords) {
        List<MicroWorkSubmit> submitListOfChangeStorage = new ArrayList<>();
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmits) {
            // 查看是否已生成返修复核记录
            List<MicroRepairRecord> microRepairRecordTemp = microRepairRecords.stream().filter(obj -> obj.getSubmitId().equals(microWorkSubmit.getId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(microRepairRecordTemp)) {
                MicroWorkSubmit temp = new MicroWorkSubmit();
                // 复制对象
                BeanUtils.copyProperties(microWorkSubmit,temp);

                // 每条报工记录在一次返修时只会有一条质检记录
                MicroRepairRecord microRepairRecord = microRepairRecordTemp.get(0);
                // 报工的数量实际就是维修数量和让步接收数量
                temp.setCheckPassNum(microRepairRecord.getRepairNum().add(microRepairRecord.getConcessionNum()));
                // 良品数量设置为负数
                temp.setCheckNgNum(microRepairRecord.getRepairNum().add(microRepairRecord.getConcessionNum()).negate());
                // 报工人实际是返修人
                temp.setSubmitUser(microRepairRecord.getCreatedBy().toString());
                submitListOfChangeStorage.add(temp);
            }
        }
        return submitListOfChangeStorage;
    }

    /**
     * 组装用于更新报工记录维修数量的实体
     *
     * @param microWorkSubmits
     * @param microRepairRecords
     * @return
     */
    public static List<MicroWorkSubmit> microWorkSubmitAndMicroRepairRecordDomain2MicroWorkSubmitDomainForUpdate(List<MicroWorkSubmit> microWorkSubmits,
                                                                                                                 List<MicroRepairRecord> microRepairRecords) {
        List<MicroWorkSubmit> microWorkSubmitForUpdate = new ArrayList<>();
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmits) {
            // 查看是否已生成返修复核记录
            List<MicroRepairRecord> microRepairRecordTemp = microRepairRecords.stream().filter(obj -> obj.getSubmitId().equals(microWorkSubmit.getId())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(microRepairRecordTemp)) {
                MicroWorkSubmit temp = new MicroWorkSubmit();
                temp.setId(microWorkSubmit.getId());
                temp.setProductSeq(microWorkSubmit.getProductSeq());
                temp.setProductCode(microWorkSubmit.getProductCode());
                temp.setProductName(microWorkSubmit.getProductName());
                temp.setOperateProcessSeq(microWorkSubmit.getOperateProcessSeq());
                temp.setOperateProcessName(microWorkSubmit.getOperateProcessName());
                temp.setOperateProcessCode(microWorkSubmit.getOperateProcessCode());
                temp.setSubmitUser(microWorkSubmit.getSubmitUser());
                // 这里的repairNum是累计计算的
                temp.setRepairNum(microWorkSubmit.getRepairNum());
                temp.setAbandonedNum(microWorkSubmit.getAbandonedNum());
                temp.setLastUpdDate(DateUtils.getNowDate());
                temp.setLastUpdBy(microRepairRecordTemp.get(0).getCreateBy());

                // 用于计件结算的数量 (返修数量 = 只能从返修记录中取数量)
                BigDecimal repairNumTemp = microRepairRecordTemp.stream().map(MicroRepairRecord::getRepairNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal concessionNumTemp = microRepairRecordTemp.stream().map(MicroRepairRecord::getConcessionNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                temp.setCheckPassNum(repairNumTemp.add(concessionNumTemp));

                microWorkSubmitForUpdate.add(temp);
            }
        }
        return microWorkSubmitForUpdate;
    }

    /**
     * 组装不良品清单要导出的实体
     */
    public static NgProductMailExportResult covertNgProductMailExportResult(NgInfoByProductAndProcessAndUser ngInfoByProductAndProcessAndUser, List<RepairNumInfoByProductAndProcessAndUser> repairNumInfoByProductAndProcessAndUserList) {
        NgProductMailExportResult temp = new NgProductMailExportResult();
        BigDecimal repairNum = BigDecimal.ZERO;
        BigDecimal concessionNum = BigDecimal.ZERO;
        BigDecimal abandonedNum = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(repairNumInfoByProductAndProcessAndUserList)) {
            repairNum = repairNumInfoByProductAndProcessAndUserList.stream().map(RepairNumInfoByProductAndProcessAndUser::getRepairNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            concessionNum = repairNumInfoByProductAndProcessAndUserList.stream().map(RepairNumInfoByProductAndProcessAndUser::getConcessionNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            abandonedNum = repairNumInfoByProductAndProcessAndUserList.stream().map(RepairNumInfoByProductAndProcessAndUser::getAbandonedNum).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        temp.setProductCode(ngInfoByProductAndProcessAndUser.getProductCode());
        temp.setProductName(ngInfoByProductAndProcessAndUser.getProductName());
        temp.setProcessName(ngInfoByProductAndProcessAndUser.getProcessName());
        temp.setProcessCode(ngInfoByProductAndProcessAndUser.getProcessCode());
        temp.setSubmitNickName(ngInfoByProductAndProcessAndUser.getSubmitNickName());
        temp.setRepairNum(repairNum);
        temp.setConcessionNum(concessionNum);
        temp.setAbandonedNum(abandonedNum);
        temp.setWaitToDoNum(ngInfoByProductAndProcessAndUser.getNgNum());
        // 不良品数量 = 待处理数量（不良品清单的数量） + 返修数量 + 让步接收数量 + 报废数量
        temp.setTotalNgNum(repairNum.add(concessionNum).add(abandonedNum).add(ngInfoByProductAndProcessAndUser.getNgNum()));
        return temp;
    }

    /**
     * 组装报工历史变动记录 （返修、让步接收、报废）
     *
     * @param workSubmits
     * @param microRepairRecordList
     * @return
     */
    public static List<MicroWorkSubmitHistory> repairRecordAndMicroWorkSubmitConvert2MicroWorkSubmitHistoryForRepair(List<MicroWorkSubmit> workSubmits,
                                                                                                                     List<MicroRepairRecord> microRepairRecordList) {
        List<MicroWorkSubmitHistory> microWorkSubmitHistoryList = new ArrayList<>();
        String userId = SecurityUtils.getUserId().toString();
        Date currentDate = DateUtils.getNowDate();
        for (MicroWorkSubmit workSubmit : workSubmits) {
            // 查看是否已生成返修复核记录,设置数量
            List<MicroRepairRecord> microRepairRecordTemp = microRepairRecordList.stream().filter(obj -> obj.getSubmitId().equals(workSubmit.getId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(microRepairRecordTemp)) {
                /**
                 * 让步接收数量
                 */
                BigDecimal concessionNum = microRepairRecordTemp
                        .stream()
                        .map(MicroRepairRecord::getConcessionNum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                /**
                 * 维修数量
                 */
                BigDecimal repairNum = microRepairRecordTemp
                        .stream()
                        .map(MicroRepairRecord::getRepairNum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                /**
                 * 报废数量
                 */
                BigDecimal abandonedNum = microRepairRecordTemp
                        .stream()
                        .map(MicroRepairRecord::getAbandonedNum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                MicroWorkSubmitHistory temp = new MicroWorkSubmitHistory();
                BeanUtils.copyProperties(workSubmit, temp);
                temp.setCreatedBy(userId);
                temp.setCreatedDate(currentDate);
                // 不需要存储良品和不良品数量
                temp.setPassNum(BigDecimal.ZERO);
                temp.setNgNum(BigDecimal.ZERO);
                temp.setSettledNum(BigDecimal.ZERO);
                temp.setOperateNode(CommonConstants.SUBMIT_HISTORY_REPAIR);

                temp.setRepairNum(repairNum);
                temp.setConcessionNum(concessionNum);
                temp.setAbandonedNum(abandonedNum);
                microWorkSubmitHistoryList.add(temp);
            }
        }
        return microWorkSubmitHistoryList;
    }
}

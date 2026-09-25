/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.planning;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskDto;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskProcessInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.infrastructure.enums.IsFirstProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TaskSubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生产任务数据装配工具类
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
public class MicroManufactureTaskAssembler {

    /** 根工序（首序）的父工序标识 */
    private static final String ROOT_PARENT_PROCESS_SEQ = "0";

    public static MicroWorkSubmit convertDto2Domain(MicroManufactureTaskDto microManufactureTaskDto, MicroProduct microProduct, MicroProcessCommon microProcessCommon) {
        MicroWorkSubmit result = new MicroWorkSubmit();
        result.setProductSeq(microManufactureTaskDto.getProductSeq());
        result.setProductCode(microProduct.getProductCode());
        result.setProductName(microProduct.getProductName());
        result.setOperateProcessSeq(microManufactureTaskDto.getProcessSeq());
        result.setOperateProcessCode(microProcessCommon.getProcessCode());
        result.setOperateProcessName(microProcessCommon.getProcessName());
        result.setPassNum(microManufactureTaskDto.getPassNum());
        result.setNgNum(microManufactureTaskDto.getNgNum());
        result.setRemark(microManufactureTaskDto.getRemark());
        result.setIsLastProcess(microManufactureTaskDto.getIsLastProcess());
        result.setTaskNo(microManufactureTaskDto.getTaskNo());
        result.setWorkOrderNo(microManufactureTaskDto.getWorkOrderNo());
        result.setOrderNo(microManufactureTaskDto.getOrderNo());
        result.setSubmitPictures(microManufactureTaskDto.getSubmitPictures());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 装配查询参数
     * @date 2023/3/20 16:40
     * @param condition
     * @return com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask
     **/
    public static MicroManufactureTask convert2Condition(MicroManufactureTaskDto condition) {
        return BeanUtil.copyProperties(condition,MicroManufactureTask.class);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description DOMAINS --> DTOs 分页
     * @date 2023/3/21 17:44
     * @param taskList
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskDto>
     **/
    public static List<MicroManufactureTaskDto> convertDomains2DTOs4Page(List<MicroManufactureTask> taskList) { 
        if (CollectionUtil.isNotEmpty(taskList)){
            List<MicroManufactureTaskDto> targetList = convertDomains2DTOs(taskList);
            return MicroPageUtils.listToPage(taskList,targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @description DOMAINS --> DTOs
     * @date 2023/3/21 17:44
     * @param taskList
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskDto>
     **/
    public static List<MicroManufactureTaskDto> convertDomains2DTOs(List<MicroManufactureTask> taskList) { 
        if (CollectionUtil.isNotEmpty(taskList)){
            return taskList.stream().map(task->convertDomain2DTO(task)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 任务进度，结合报工记录
     *
     * @param submitListTemp
     * @param microManufactureTask
     * @return
     */
    public static MicroManufactureTaskProcessInfo covertMicroManufactureTaskProcessInfo(List<MicroWorkSubmit> submitListTemp,
                                                                                        MicroManufactureTask microManufactureTask) {
        MicroManufactureTaskProcessInfo temp = new MicroManufactureTaskProcessInfo();
        BigDecimal passNum = BigDecimal.ZERO;
        BigDecimal ngNum = BigDecimal.ZERO;
        BigDecimal checkNgNum = BigDecimal.ZERO;
        BigDecimal checkPassNum = BigDecimal.ZERO;
        BigDecimal completionRate = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(submitListTemp)) {
            passNum = submitListTemp.stream()
                    .filter(obj -> obj.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode()))
                    .map(obj -> obj.getPassNum())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ngNum = submitListTemp.stream()
                    .filter(obj -> obj.getSubmitStatus().equals(SubmitStatusEnum.UN_APPROVE.getCode()))
                    .map(obj -> obj.getNgNum())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            checkPassNum = submitListTemp.stream()
                    .filter(obj -> obj.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode()))
                    .map(obj -> obj.getCheckPassNum())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            checkNgNum = submitListTemp.stream()
                    .filter(obj -> obj.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode()))
                    .map(obj -> obj.getCheckNgNum())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // 进度信息
            completionRate = passNum.add(checkPassNum).divide(microManufactureTask.getTaskPlanNum(), 2, RoundingMode.HALF_UP);
        }

        temp.setProcessName(microManufactureTask.getProcessName());
        temp.setPassNum(passNum);
        temp.setNgNum(ngNum);
        temp.setCheckNgNum(checkNgNum);
        temp.setCheckPassNum(checkPassNum);
        temp.setCompletionRate(completionRate);
        temp.setTaskPlanNum(microManufactureTask.getTaskPlanNum());
        // 首尾序可以从任务单上面获取,为了应对产品工艺变化导致首尾序发生变化的情况
        temp.setIsFirstProcessFlag(microManufactureTask.getIsFirstProcess());
        temp.setIsLastProcessFlag(microManufactureTask.getIsLastProcess());

        return temp;
    }

    private static MicroManufactureTaskDto convertDomain2DTO(MicroManufactureTask inParam) {
        MicroManufactureTaskDto result = new MicroManufactureTaskDto();
        result.setActiveFlag(inParam.getActiveFlag());
        result.setBeforeStatus(inParam.getBeforeStatus());
        result.setCompletionRate(inParam.getCompletionRate());
        result.setCreatedBy(inParam.getCreatedBy());
        result.setCreatedDate(inParam.getCreatedDate());
        result.setId(inParam.getId());
        result.setIsLastProcess(inParam.getIsLastProcess());
        result.setLastUpdBy(inParam.getLastUpdBy());
        result.setLastUpdDate(inParam.getLastUpdDate());
        result.setMlineCode(inParam.getMlineCode());
        result.setNgNum(inParam.getNgNum());
        result.setOrderNo(inParam.getOrderNo());
        result.setPassNum(inParam.getPassNum());
        result.setPendNgNum(inParam.getPendNgNum());
        result.setProcessSeq(inParam.getProcessSeq());
        result.setProcessName(inParam.getProcessName());
        result.setProcessCode(inParam.getProcessCode());
        result.setProductSeq(inParam.getProductSeq());
        result.setSubmitableNum(inParam.getSubmitableNum());
        result.setSubmitStatus(inParam.getSubmitStatus());
        result.setTaskNo(inParam.getTaskNo());
        result.setTaskPlanNum(inParam.getTaskPlanNum());
        result.setTechType(inParam.getTechType());
        result.setWorkOrderNo(inParam.getWorkOrderNo());
        result.setWshopCode(inParam.getWshopCode());
        return result;
    }

    /**
     * 生产订单下发时 转换工序任务
     *
     * @param microManufactureWorkOrder
     * @param microProcessChainBindEntity
     * @param taskNo
     * @return
     */
    public static MicroManufactureTask cover2DomainByWorkOrderAndTech(MicroManufactureWorkOrder microManufactureWorkOrder,
                                                                       MicroProcessChainBindEntity microProcessChainBindEntity,
                                                                       String taskNo,
                                                                       String userId,
                                                                       String tenantCode) {
        MicroManufactureTask temp = new MicroManufactureTask();
        temp.setOrderNo(microManufactureWorkOrder.getOrderNo());
        temp.setWorkOrderNo(microManufactureWorkOrder.getWorkOrderNo());
        temp.setProductSeq(microManufactureWorkOrder.getProductSeq());
        temp.setProcessSeq(microProcessChainBindEntity.getProcessSeq());
        // 跟工单的数量一致
        temp.setTaskPlanNum(microManufactureWorkOrder.getWorkOrderNum());
        temp.setWshopCode(microManufactureWorkOrder.getWshopCode());
        temp.setMlineCode(microManufactureWorkOrder.getMlineCode());
        temp.setTechType(microProcessChainBindEntity.getTechType());
        // 任务号
        temp.setTaskNo(taskNo);
        temp.setSubmitStatus(TaskSubmitStatusEnum.TO_BE_REPORTED.getCode());
        temp.setCreatedBy(userId);
        temp.setLastUpdBy(userId);
        temp.setTenantCode(tenantCode);
        temp.setSort(microProcessChainBindEntity.getSort());
        temp.setIsLastProcess(microProcessChainBindEntity.getIsLastProcess());
        if (microProcessChainBindEntity.getParentProcessSeq().equals(ROOT_PARENT_PROCESS_SEQ)) {
            temp.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
        } else {
            temp.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
        }
        return temp;
    }

}

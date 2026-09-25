/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.planning;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.micro.application.dto.planning.DetailManufactureWorkOrderInfo;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.SimpleWorkOrderInfo;
import com.cosmo.hhim.micro.application.dto.planning.WorkOrderInfoFromDetailOrder;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team

 * @author cosmo-hhim-open Team
 * @version 1.0
 */
public class MicroManufactureWorkOrderAssembler {

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder
     **/
    public static MicroManufactureWorkOrder convertDto2Domain4Operate(MicroManufactureWorkOrderDto inParam, Long userId) {
        MicroManufactureWorkOrder result = convertDto2Domain(inParam);
        if (Objects.nonNull(userId)){
            result.setLastUpdBy(String.valueOf(userId));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder
     **/
    public static MicroManufactureWorkOrder convertDto2Domain(MicroManufactureWorkOrderDto inParam) {
        MicroManufactureWorkOrder result = new MicroManufactureWorkOrder();
        result.setId(inParam.getId());
        result.setWorkOrderStatus(inParam.getWorkOrderStatus());
        result.setBeforeStatus(inParam.getBeforeStatus());
        result.setMainFlag(inParam.getMainFlag());
        result.setOrderNo(inParam.getOrderNo());
        result.setCloseReason(inParam.getCloseReason());
        result.setDeliveryDate(inParam.getDeliveryDate());
        result.setFinishNum(inParam.getFinishNum());
        result.setInboundDate(inParam.getInboundDate());
        result.setMlineCode(inParam.getMlineCode());
        result.setNgNum(inParam.getNgNum());
        result.setPlanEndDate(inParam.getPlanEndDate());
        result.setPlanStartDate(inParam.getPlanStartDate());
        result.setProduceEndDate(inParam.getProduceEndDate());
        result.setProduceStartDate(inParam.getProduceStartDate());
        result.setProductSeq(inParam.getProductSeq());
        result.setProductName(inParam.getProductName());
        result.setProductCode(inParam.getProductCode());
        result.setWordOrderType(inParam.getWordOrderType());
        result.setWshopCode(inParam.getWshopCode());
        result.setTenantCode(inParam.getTenantCode());
        result.setExtendContent(inParam.getExtendContent());
        result.setWorkOrderNum(inParam.getWorkOrderNum());
        result.setWorkOrderNo(inParam.getWorkOrderNo());
        result.setMlineName(inParam.getMlineName());
        result.setWshopName(inParam.getWshopName());
        return result;

    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder
     **/
    public static MicroManufactureWorkOrder convertDto2Domain4Insert(MicroManufactureWorkOrderDto inParam, Long userId, String tenantCode) {
        MicroManufactureWorkOrder result = convertDto2Domain(inParam);
        if (Objects.nonNull(userId)){
            result.setCreatedBy(String.valueOf(userId));
            result.setLastUpdBy(String.valueOf(userId));
        }
        result.setTenantCode(tenantCode);
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder
     **/
    public static MicroManufactureWorkOrder convertDto2Domain4Update(MicroManufactureWorkOrderDto inParam, Long userId) {
        MicroManufactureWorkOrder result = convertDto2Domain(inParam);
        if (Objects.nonNull(userId)){
            result.setLastUpdBy(String.valueOf(userId));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param workOrderList
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto>
     **/
    public static List<MicroManufactureWorkOrderDto> convertDomains2DTOS4Page(List<MicroManufactureWorkOrder> workOrderList) { 
        if (CollectionUtils.isNotEmpty(workOrderList)){
            List<MicroManufactureWorkOrderDto> targetList = convertDomains2DTOS(workOrderList);
            return MicroPageUtils.listToPage(workOrderList,targetList);
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param workOrderList
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto>
     **/
    public static List<MicroManufactureWorkOrderDto> convertDomains2DTOS(List<MicroManufactureWorkOrder> workOrderList) { 
        if (CollectionUtils.isNotEmpty(workOrderList)){
            return workOrderList.stream().map(workOrder->convertDomain2DTO(workOrder)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto
     **/
    public static MicroManufactureWorkOrderDto convertDomain2DTO(MicroManufactureWorkOrder inParam) {
        MicroManufactureWorkOrderDto result =  BeanUtil.copyProperties(inParam,MicroManufactureWorkOrderDto.class);
        if (CollectionUtil.isNotEmpty(inParam.getTaskList())){
            result.setTaskDtoList(MicroManufactureTaskAssembler.convertDomains2DTOs(inParam.getTaskList()));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder
     **/
    public static MicroManufactureWorkOrder convert2Condition(MicroManufactureWorkOrderDto inParam) {
        return BeanUtil.copyProperties(inParam,MicroManufactureWorkOrder.class);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    public static List<MicroManufactureWorkOrder> covertDTOs2Domains4Insert(List<MicroManufactureWorkOrderDto> inParam, Long userId,String tenantCode) { 
        if (CollectionUtils.isNotEmpty(inParam)){
            return inParam.stream().map(workOrder->convertDto2Domain4Insert(workOrder,userId,tenantCode)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    public static List<MicroManufactureWorkOrder> covertDTOs2Domains4Update(List<MicroManufactureWorkOrderDto> inParam, Long userId) { 
        if (CollectionUtils.isNotEmpty(inParam)){
            return inParam.stream().map(workOrder->convertDto2Domain4Update(workOrder,userId)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 转换为工单详情对象
     *
     * @param microManufactureWorkOrder 生产工单
     * @return 工单详情对象
     */
    public static DetailManufactureWorkOrderInfo covert2DetailManufactureWorkOrderInfo(MicroManufactureWorkOrder microManufactureWorkOrder) {
        DetailManufactureWorkOrderInfo result = new DetailManufactureWorkOrderInfo();
        result.setId(microManufactureWorkOrder.getId());
        result.setOrderNo(microManufactureWorkOrder.getOrderNo());
        result.setWorkOrderNo(microManufactureWorkOrder.getWorkOrderNo());
        result.setProductCode(microManufactureWorkOrder.getProductCode());
        result.setProductName(microManufactureWorkOrder.getProductName());
        result.setProductUnit(microManufactureWorkOrder.getUnit());
        result.setWorkOrderStatus(microManufactureWorkOrder.getWorkOrderStatus());
        result.setPlanNum(microManufactureWorkOrder.getWorkOrderNum());
        result.setWshopName(microManufactureWorkOrder.getWshopName());
        result.setMlineName(microManufactureWorkOrder.getMlineName());
        result.setPlanStartDate(microManufactureWorkOrder.getPlanStartDate());
        result.setPlanEndDate(microManufactureWorkOrder.getPlanEndDate());
        result.setExtendContent(microManufactureWorkOrder.getExtendContent());
        result.setBeforeStatus(microManufactureWorkOrder.getBeforeStatus());
        result.setMainFlag(microManufactureWorkOrder.getMainFlag());
        result.setIsComplete(microManufactureWorkOrder.getIsComplete());
        result.setWorkOrderWarnFlag(microManufactureWorkOrder.getWorkOrderWarnFlag());
        return result;
    }

    public static List<WorkOrderInfoFromDetailOrder> covert2WorkOrderInfoFromDetailOrder(List<MicroManufactureWorkOrder> microManufactureWorkOrderList) {

        List<WorkOrderInfoFromDetailOrder> workOrderList = new ArrayList<>();
        for (MicroManufactureWorkOrder workOrder : microManufactureWorkOrderList) {
            WorkOrderInfoFromDetailOrder temp = new WorkOrderInfoFromDetailOrder();
            temp.setId(workOrder.getId());
            temp.setWorkOrderNo(workOrder.getWorkOrderNo());
            temp.setProductSeq(workOrder.getProductSeq());
            temp.setProductCode(workOrder.getProductCode());
            temp.setProductName(workOrder.getProductName());
            temp.setUnit(workOrder.getUnit());
            temp.setPlanNum(workOrder.getWorkOrderNum());
            temp.setFinishNum(workOrder.getFinishNum());
            temp.setWaitProduceNum(workOrder.getWaitProduceNum());
            temp.setWorkOrderStatus(workOrder.getWorkOrderStatus());
            temp.setPlanStartDate(workOrder.getPlanStartDate());
            temp.setPlanEndDate(workOrder.getPlanEndDate());
            temp.setMainFlag(workOrder.getMainFlag());
            temp.setIsComplete(workOrder.getIsComplete());
            temp.setWorkOrderWarnFlag(workOrder.getWorkOrderWarnFlag());
            workOrderList.add(temp);
        }
        return workOrderList;
    }

    public static List<SimpleWorkOrderInfo> workOrderDomain2SimpleWorkOrderInfo(List<String> workOrderNoList, List<MicroManufactureWorkOrder> microManufactureWorkOrderList) {
        List<SimpleWorkOrderInfo> result = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(microManufactureWorkOrderList)) {
            for (MicroManufactureWorkOrder workOrder : microManufactureWorkOrderList) {
                SimpleWorkOrderInfo temp = new SimpleWorkOrderInfo();
                temp.setWorkOrderNo(workOrder.getWorkOrderNo());
                temp.setProductSeq(workOrder.getProductSeq());
                temp.setProductCode(workOrder.getProductCode());
                temp.setProductName(workOrder.getProductName());
                temp.setPlanNum(workOrder.getWorkOrderNum());
                temp.setPlanStartDate(workOrder.getPlanStartDate());
                temp.setPlanEndDate(workOrder.getPlanEndDate());
                result.add(temp);
            }
            return MicroPageUtils.listToPage(workOrderNoList, result);
        }
        return Collections.emptyList();
    }
}

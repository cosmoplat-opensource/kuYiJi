/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.planning;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.application.dto.planning.DetailMicroManufactureOrderInfo;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.OrderProgressParam;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team

 * @author cosmo-hhim-open Team
 * @version 1.0
 */
public class MicroManufactureOrderAssembler {


    /**
     * @author cosmo-hhim-open Team
     * @return com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder
     **/
    public static MicroManufactureOrder convertDto2Domain(MicroManufactureOrderDto inParam) {
        MicroManufactureOrder result = new MicroManufactureOrder();
        result.setBeforeStatus(inParam.getBeforeStatus());
        result.setCloseReason(inParam.getCloseReason());
        result.setDeliveryDate(inParam.getDeliveryDate());
        result.setEndDate(inParam.getEndDate());
        result.setExtendContent(inParam.getExtendContent());
        result.setFinishNum(inParam.getFinishNum());
        result.setId(inParam.getId());
        result.setNgNum(inParam.getNgNum());
        result.setOrderNo(inParam.getOrderNo());
        result.setOrderStatus(inParam.getOrderStatus());
        result.setOwCode(inParam.getOwCode());
        result.setPlanNum(inParam.getPlanNum());
        result.setProductSeq(inParam.getProductSeq());
        result.setStartDate(inParam.getStartDate());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder
     **/
    public static MicroManufactureOrder convertDto2Domain4Insert(MicroManufactureOrderDto inParam, Long userId, String tenantCode) {
        MicroManufactureOrder result = convertDto2Domain(inParam);
        result.setTenantCode(tenantCode);
        if (Objects.nonNull(userId)){
            result.setCreatedBy(String.valueOf(userId));
            result.setLastUpdBy(String.valueOf(userId));
        }
        result.setCreatedDate(DateUtils.getNowDate());
        result.setLastUpdDate(DateUtils.getNowDate());

        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam, userId, tenantCode
     * @return com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder
     **/
    public static MicroManufactureOrder convertDto2Domain4Update(MicroManufactureOrderDto inParam, Long userId) {
        MicroManufactureOrder result = convertDto2Domain(inParam);
        if (Objects.nonNull(userId)){
            result.setLastUpdBy(String.valueOf(userId));
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder
     **/
    public static MicroManufactureOrder convert2Condition(MicroManufactureOrderDto inParam) {
        MicroManufactureOrder result = new MicroManufactureOrder();
        result.setOrderNo(inParam.getOrderNo());
        result.setOrderStatus(inParam.getOrderStatus());
        result.setProductName(inParam.getProductName());
        result.setOwName(inParam.getOwName());
        result.setStartDate(inParam.getStartDate());
        result.setEndDate(inParam.getEndDate());
        result.setAvailableFlag(inParam.getAvailableFlag());
        result.setOrderWarnQueryFlag(inParam.getOrderWarnQueryFlag());
        result.setProductSeq(inParam.getProductSeq());
        result.setOwCode(inParam.getOwCode());
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto>
     **/
    public static List<MicroManufactureOrderDto> convertDomains2DTOs4Page(List<MicroManufactureOrder> orderList) { 
        if (CollectionUtil.isNotEmpty(orderList)){
            List<MicroManufactureOrderDto> targetList = convertDomains2DTOs(orderList);
            return MicroPageUtils.listToPage(orderList,targetList);
        }
        return Collections.emptyList();
    }


    /**
     * @author cosmo-hhim-open Team
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto>
     **/
    public static List<MicroManufactureOrderDto> convertDomains2DTOs(List<MicroManufactureOrder> orderList) { 
        return CollectionUtil.isEmpty(orderList)?Collections.emptyList():BeanUtil.copyToList(orderList,MicroManufactureOrderDto.class);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param inParam
     * @return com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto
     **/
    public static MicroManufactureOrderDto convertDomain2DTO(MicroManufactureOrder inParam) {
        return BeanUtil.copyProperties(inParam,MicroManufactureOrderDto.class);
    }

    public static List<MicroManufactureOrder> convertDTOs2Domains4Insert(List<MicroManufactureOrderDto> microManufactureOrders, Long userId, String tenantCode) { 
        if (CollectionUtils.isNotEmpty(microManufactureOrders)){
            return microManufactureOrders.stream().map(order->convertDto2Domain4Insert(order,userId,tenantCode)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 转换为生产订单详情实体信息
     * @param microManufactureOrder 生产订单
     * @return 生产订单详情
     */
    public static DetailMicroManufactureOrderInfo covertDetailMicroManufactureOrderInfo(MicroManufactureOrder microManufactureOrder) {
        DetailMicroManufactureOrderInfo result = new DetailMicroManufactureOrderInfo();
        result.setId(microManufactureOrder.getId());
        result.setOrderNo(microManufactureOrder.getOrderNo());
        result.setProductCode(microManufactureOrder.getProductCode());
        result.setProductSeq(microManufactureOrder.getProductSeq());
        result.setProductName(microManufactureOrder.getProductName());
        result.setUnit(microManufactureOrder.getUnit());
        result.setCustomerName(microManufactureOrder.getOwName());
        result.setDeliverDate(microManufactureOrder.getDeliveryDate());
        result.setPlanNum(microManufactureOrder.getPlanNum());
        result.setOrderStatus(microManufactureOrder.getOrderStatus());
        result.setBeforeStatus(microManufactureOrder.getBeforeStatus());
        result.setOrderWarnFlag(microManufactureOrder.getOrderWarnFlag());
        return result;
    }

    /**
     * 订单进度追踪入参转换
     *
     */
    public static MicroManufactureOrder covertOrderProgressParam2MicroManufactureOrderDomain(OrderProgressParam orderProgressParam) {
        MicroManufactureOrder microManufactureOrder = new MicroManufactureOrder();
        microManufactureOrder.setOrderNo(orderProgressParam.getOrderNo());
        microManufactureOrder.setProductSeq(orderProgressParam.getProductSeq());
        microManufactureOrder.setOrderStatusList(orderProgressParam.getOrderStatusList());
        microManufactureOrder.setOwCode(orderProgressParam.getOwCode());
        microManufactureOrder.setStartDate(orderProgressParam.getStartDate());
        microManufactureOrder.setEndDate(orderProgressParam.getEndDate());
        return microManufactureOrder;
    }
}

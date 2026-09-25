/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.manufacture.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsCompleteEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderOperateEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderWarnFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.WorkOrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroManufactureOrderMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.planning.domain.service.manufacture.IMicroManufactureOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 生产订单Service业务层处理
 * 
 * @date 2023-03-06
 */
@Service
public class MicroManufactureOrderServiceImpl implements IMicroManufactureOrderService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Autowired
    private MicroManufactureOrderMapper microManufactureOrderMapper;
    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;

    @Autowired
    private MicroSupportUtil microSupportUtil;

    @Override
    public MicroManufactureOrder selectMicroManufactureOrderById(Long id) {
        MicroManufactureOrder manufactureOrder = microManufactureOrderMapper.selectMicroManufactureOrderById(id);
        calculateNum(manufactureOrder);
        return manufactureOrder;
    }

    @Override
    public List<MicroManufactureOrder> selectMicroManufactureOrderList(MicroManufactureOrder microManufactureOrder) {
        return microManufactureOrderMapper.selectMicroManufactureOrderList(microManufactureOrder);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY,rollbackFor = Exception.class)
    public int insertMicroManufactureOrder(MicroManufactureOrder manufactureOrder) {
        //订单号方案：1.用户手动填写 2.自动生成
        if (StringUtils.isEmpty(manufactureOrder.getOrderNo())){
            // 获取已存在的工单号
            List<String> allExistOrderNoList = microManufactureOrderMapper.getAllExistOrderNoListInCurrentDate();
            // 生成订单编号
            generateManufactureOrderCode(manufactureOrder, allExistOrderNoList);
        }
        return microManufactureOrderMapper.insertMicroManufactureOrder(manufactureOrder);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 校验订单编码唯一性
     * @date 2023/3/24 14:43
     * @param orderNo
     * @return boolean
     **/
    @Override
    public boolean isUniqueOrder(String orderNo) {
        if (StringUtils.isEmpty(orderNo)){
            return true;
        }
        MicroManufactureOrder params = new MicroManufactureOrder();
        params.setOrderNo(orderNo);
        return CollectionUtil.isEmpty(microManufactureOrderMapper.selectMicroManufactureOrderList(params));
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 生成订单编码  生成规则 SC+YYMMDD+000001
     * @date 2023/3/10 14:27
     * @param manufactureOrder
     * @param alreadyExistOrderNoList
     * @return void
     **/
    private void generateManufactureOrderCode(MicroManufactureOrder manufactureOrder, List<String> alreadyExistOrderNoList) {
        String code = microSupportUtil.getOrderNo(alreadyExistOrderNoList);
        manufactureOrder.setOrderNo(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroManufactureOrder(MicroManufactureOrder microManufactureOrder) {
        return microManufactureOrderMapper.updateMicroManufactureOrder(microManufactureOrder);
    }

    @Override
    public int deleteMicroManufactureOrderByIds(Long[] ids) {
        return microManufactureOrderMapper.deleteMicroManufactureOrderByIds(ids);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY,rollbackFor = Exception.class)
    public int operate(MicroManufactureOrder manufactureOrder,String operateFlag) {
        OrderOperateEnum operateEnum = OrderOperateEnum.getEnum(operateFlag);
        try {
            switch (operateEnum){
                case ORDER_OPERATE_CLOSE:return closeOrder(manufactureOrder);
                case ORDER_OPERATE_REBOOT:return reBootOrder(manufactureOrder);
                default:return 0;
            }
        }catch (Exception e){
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public List<MicroManufactureOrder> selectMicroManufactureOrderListByCondition(MicroManufactureOrder condition) {
        List<MicroManufactureOrder> orders = microManufactureOrderMapper.selectMicroManufactureOrderListByCondition(condition);
        if (CollectionUtils.isNotEmpty(orders)){
            orders.forEach(order->{
                //是否展示工单列表
                getShowWorkOrderList(order);
                // 计算订单的数量相关数据
                calculateNum(order);
                // 设置生产订单的警示标示
                calculateOrderWarnFlag(order);
            });
        }
        return orders;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 是否展示工单列表
     * @date 2023/3/20 14:40
     * @param order
     * @return void
     **/
    private void getShowWorkOrderList(MicroManufactureOrder order) {
        MicroManufactureWorkOrder param = new MicroManufactureWorkOrder();
        param.setQueryNo(order.getOrderNo());
        order.setShowWorkOrderList(microManufactureWorkOrderMapper.countWorkOrderByCondition(param)>0);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 计算订单的数量相关数据
     * @date 2023/3/17 18:05
     * @param order
     * @return void
     **/
    private void calculateNum(MicroManufactureOrder order) {
        BigDecimal multiplicand = new BigDecimal(100);
        String completionRate = "0";
        BigDecimal waitProduceNum = BigDecimal.ZERO;
        //如果订单计划数量==0 完成率和待产数默认为0
        if (order.getPlanNum().equals(BigDecimal.ZERO)){
            order.setWaitProduceNum(waitProduceNum);
            order.setCompletionRate(completionRate);
            return;
        }
        //计算待产数量和完成率 如果计划数-完成数<0 且 完成数>=0
        if (order.getPlanNum().compareTo(order.getFinishNum())>0&&BigDecimal.ZERO.compareTo(order.getFinishNum())<=0){
            waitProduceNum = order.getPlanNum().subtract(order.getFinishNum());
            completionRate = order.getFinishNum().divide(order.getPlanNum(),2, RoundingMode.HALF_UP).multiply(multiplicand).stripTrailingZeros().toPlainString();
        }
        order.setWaitProduceNum(waitProduceNum);
        order.setCompletionRate(completionRate);
    }

    /**
     * 设置生产订单的警示标识
     */
    private void calculateOrderWarnFlag(MicroManufactureOrder order) {
        List<String> normalOrderStatus = Arrays.asList(OrderStatusEnum.WAITING_PERIOD.getCode(),
                OrderStatusEnum.TO_BE_PRODCED.getCode(), OrderStatusEnum.IN_PRODUCTION.getCode());
        // 符合这三种订单状态的才能设置警示标示
        if (normalOrderStatus.contains(order.getOrderStatus())) {
            Date currentDate = DateUtils.getNowDate();
            if (DateUtil.compare(DateUtil.offsetDay(order.getDeliveryDate(),-1), currentDate, DATE_PATTERN) == 0) {
                order.setOrderWarnFlag(OrderWarnFlagEnum.DELIVERY_WARN.getCode());
            }

            if (DateUtil.compare(order.getDeliveryDate(), currentDate, DATE_PATTERN) <= 0) {
                order.setOrderWarnFlag(OrderWarnFlagEnum.OVER_DATE_WARN.getCode());
            }
        }
    }

    @Override
    public List<MicroManufactureOrder> selectWorkStatusCount(MicroManufactureOrder condition) {
        BaseEnum[] orderStatusEnums = OrderStatusEnum.values();
        if (ArrayUtils.isNotEmpty(orderStatusEnums)){
            List<MicroManufactureOrder> statusCountList = new ArrayList<>(orderStatusEnums.length);
            for (BaseEnum baseEnum:orderStatusEnums){
                MicroManufactureOrder order = new MicroManufactureOrder();
                statusCountList.add(order);
                order.setOrderStatus((String)baseEnum.getCode());
                condition.setOrderStatus(order.getOrderStatus());
                order.setOrderStatusCount(microManufactureOrderMapper.countOrderByCondition(condition));
            }
            return statusCountList;
        }
        return Collections.emptyList();
    }

    @Override
    public int updateWithFiledNull(MicroManufactureOrder manufactureOrder) {
        return microManufactureOrderMapper.updateWithFiledNull(manufactureOrder);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 重启单子
     * @date 2023/3/10 18:05
     * @param manufactureOrder
     * @return int
     **/
    private int reBootOrder(MicroManufactureOrder manufactureOrder) {
        int flag;
        //1.重启订单
        //orderStatus和beforeStatus在关闭工单和重启工单当中的字段赋值是有序的，不能改变顺序
        manufactureOrder.setOrderStatus(manufactureOrder.getBeforeStatus());
        manufactureOrder.setBeforeStatus(null);
        flag = microManufactureOrderMapper.updateMicroManufactureOrder(manufactureOrder);
        MicroManufactureWorkOrder param = new MicroManufactureWorkOrder();
        param.setOrderNo(manufactureOrder.getOrderNo());
        param.setIsComplete(IsCompleteEnum.NO.getCode()); // add by zyh 2023/04/26 重启只能重启未完工入库的工单 
        List<MicroManufactureWorkOrder> workOrderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderList(param);
        if (!CollectionUtils.isEmpty(workOrderList)){
            //2.重启订单下的工单
            workOrderList.forEach(workOrder->{
                if (!workOrder.getWorkOrderStatus().equals(WorkOrderStatusEnum.FINISHED.getCode())) {
                    workOrder.setLastUpdBy(manufactureOrder.getLastUpdBy());
                    workOrder.setWorkOrderStatus(workOrder.getBeforeStatus());
                    workOrder.setBeforeStatus(null);
                }
            });
            flag = microManufactureWorkOrderMapper.batchUpdateManufactureWorkOrder(workOrderList);
        }
        return flag;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 关单操作
     * @date 2023/3/10 18:03
     * @param manufactureOrder
     * @return int
     **/
    private int closeOrder(MicroManufactureOrder manufactureOrder) {
        int flag;
        //1.关闭订单
        //orderStatus和beforeStatus在关闭工单和重启工单当中的字段赋值是有序的，不能改变顺序
        manufactureOrder.setBeforeStatus(manufactureOrder.getOrderStatus());
        manufactureOrder.setOrderStatus(OrderStatusEnum.CLOSED.getCode());
        if ((flag = microManufactureOrderMapper.updateMicroManufactureOrder4Status(manufactureOrder))>0){
            MicroManufactureWorkOrder param = new MicroManufactureWorkOrder();
            param.setOrderNo(manufactureOrder.getOrderNo());
            List<MicroManufactureWorkOrder> workOrderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderList(param);
            if (!CollectionUtils.isEmpty(workOrderList)){
                //2.关闭订单下的工单
                workOrderList.forEach(workOrder->{
                    if (!workOrder.getWorkOrderStatus().equals(WorkOrderStatusEnum.FINISHED.getCode())) {
                        workOrder.setLastUpdBy(manufactureOrder.getLastUpdBy());
                        workOrder.setBeforeStatus(workOrder.getWorkOrderStatus());
                        workOrder.setWorkOrderStatus(WorkOrderStatusEnum.CLOSED.getCode());
                    }
                });
                flag = microManufactureWorkOrderMapper.batchUpdateManufactureWorkOrder(workOrderList);
            }
        }
        return flag;
    }

    /**
     * 获取生产订单信息
     */
    @Override
    public MicroManufactureOrder selectMicroManufactureOrderByOrderNo(String orderNo) {
        MicroManufactureOrder microManufactureOrder = microManufactureOrderMapper.selectMicroManufactureOrderByOrderNo(orderNo);
        if (microManufactureOrder != null) {
            this.calculateOrderWarnFlag(microManufactureOrder);
        }
        return microManufactureOrder;
    }
}

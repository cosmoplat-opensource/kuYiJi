/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.work.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsCompleteEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderOperateEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderWarnFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.WorkOrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroManufactureOrderMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.planning.domain.service.manufacture.IMicroPickMaterialsService;
import com.cosmo.hhim.micro.planning.domain.service.task.IMicroManufactureTaskService;
import com.cosmo.hhim.micro.planning.domain.service.work.IMicroManufactureWorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产工单Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-06
 */
@Slf4j
@Service
public class MicroManufactureWorkOrderServiceImpl implements IMicroManufactureWorkOrderService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;

    @Autowired
    private MicroManufactureTaskMapper microManufactureTaskMapper;

    @Autowired
    private MicroManufactureOrderMapper manufactureOrderMapper;

    @Autowired
    private IMicroPickMaterialsService pickMaterialsService;

    @Autowired
    private IMicroManufactureTaskService microManufactureTaskService;

    @Autowired
    private MicroSupportUtil microSupportUtil;

    /**
     * 查询生产工单
     *
     * @param id 生产工单ID
     * @return 生产工单
     */
    @Override
    public MicroManufactureWorkOrder selectMicroManufactureWorkOrderById(Long id)
    {
        return microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderById(id);
    }

    /**
     * 查询生产工单列表
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 生产工单
     */
    @Override
    public List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderList(MicroManufactureWorkOrder microManufactureWorkOrder)
    {
        return microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderList(microManufactureWorkOrder);
    }

    /**
     * 新增生产工单
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY,rollbackFor = Exception.class)
    public int insertMicroManufactureWorkOrder(MicroManufactureWorkOrder microManufactureWorkOrder)
    {
        List<String> allExistWorkOrderNoList = microManufactureWorkOrderMapper.getAllExistWorkOrderNoListInCurrentDate();
        return saveWorkOrder(microManufactureWorkOrder, allExistWorkOrderNoList);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 保存单个工单
     * @date 2023/4/7 9:57
     * @param microManufactureWorkOrder
     * @param alreadyExistWorkOrderNoList
     * @return int
     **/
    private int saveWorkOrder(MicroManufactureWorkOrder microManufactureWorkOrder, List<String> alreadyExistWorkOrderNoList) {
        if (microManufactureWorkOrder.getPlanStartDate()==null){
            microManufactureWorkOrder.setPlanStartDate(DateUtils.getNowDate());
        }
        if (microManufactureWorkOrder.getPlanEndDate()==null){
            microManufactureWorkOrder.setPlanEndDate(DateUtils.getNowDate());
        }
        if (StringUtils.isEmpty(microManufactureWorkOrder.getWorkOrderNo())){
            generateWorkOrderNo(microManufactureWorkOrder, alreadyExistWorkOrderNoList);
        }
        return microManufactureWorkOrderMapper.insertMicroManufactureWorkOrder(microManufactureWorkOrder);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 生成工单编码
     * @date 2023/4/7 9:57
     * @param microManufactureWorkOrder
     * @param alreadyExistWorkOrderNoList
     * @return void
     **/
    private void generateWorkOrderNo(MicroManufactureWorkOrder microManufactureWorkOrder, List<String> alreadyExistWorkOrderNoList) {
        String workOrderNo = microSupportUtil.getWorkOderNo(alreadyExistWorkOrderNoList);
        microManufactureWorkOrder.setWorkOrderNo(workOrderNo);
    }

    /**
     * 修改生产工单
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroManufactureWorkOrder(MicroManufactureWorkOrder microManufactureWorkOrder)
    {
        return microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder(microManufactureWorkOrder);
    }

    /**
     * 批量删除生产工单
     * 
     * @param ids 需要删除的生产工单ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureWorkOrderByIds(Long[] ids)
    {
        return microManufactureWorkOrderMapper.deleteMicroManufactureWorkOrderByIds(ids);
    }

    /**
     * 删除生产工单信息
     * 
     * @param id 生产工单ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureWorkOrderById(Long id)
    {
        return microManufactureWorkOrderMapper.deleteMicroManufactureWorkOrderById(id);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 对工单进行 关单 重启 完工等操作
     * @date 2023/4/7 9:57
     * @param workOrder
     * @param operateFlag
     * @return int
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int operate(MicroManufactureWorkOrder workOrder, String operateFlag) {
        OrderOperateEnum operateEnum = OrderOperateEnum.getEnum(operateFlag);
        try {
            switch (operateEnum){
                case ORDER_OPERATE_CLOSE:return closeWorkOrder(workOrder);
                case ORDER_OPERATE_REBOOT:return reBootWorkOrder(workOrder);
                case ORDER_OPERATE_COMPLETE:return completeWorkOrder(workOrder);
                default:return 0;
            }
        }catch (Exception e){
            log.error(operateEnum.getDesc()+"操作出现异常，异常原因为:{}",e);
            throw new CustomException(e.getMessage());
        }
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 校验工单号是否唯一
     * @date 2023/3/14 15:09
     * @param workOrderNo
     * @return boolean true 唯一  ；false 不唯一
     **/
    @Override
    public boolean isUniqueWorkOrderNo(String workOrderNo) {
        MicroManufactureWorkOrder condition = new MicroManufactureWorkOrder();
        condition.setWorkOrderNo(workOrderNo);
        return microManufactureWorkOrderMapper.countWorkOrderByCondition(condition)==0;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 根据条件查询各状态下工单数量
     * @date 2023/4/7 9:55
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    @Override
    public List<MicroManufactureWorkOrder> selectWorkStatusCount(MicroManufactureWorkOrder condition) {
        BaseEnum[] workOrderStatusEnums = WorkOrderStatusEnum.values();
        if (ArrayUtils.isNotEmpty(workOrderStatusEnums)){
            List<MicroManufactureWorkOrder> statusCountList = new ArrayList<>(workOrderStatusEnums.length);
            for (BaseEnum baseEnum:workOrderStatusEnums){
                MicroManufactureWorkOrder workOrder = new MicroManufactureWorkOrder();
                statusCountList.add(workOrder);
                workOrder.setWorkOrderStatus((String)baseEnum.getCode());
                condition.setWorkOrderStatus(workOrder.getWorkOrderStatus());
                workOrder.setWorkOrderStatusCount(microManufactureWorkOrderMapper.countWorkOrderByCondition(condition));
            }
            return statusCountList;
        }
        return Collections.emptyList();
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 根据条件查询工单列表
     * @date 2023/4/7 9:55
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    @Override
    public List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderListByCondition(MicroManufactureWorkOrder condition) {
        List<MicroManufactureWorkOrder> orderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderListByCondition(condition);
        if (CollectionUtils.isNotEmpty(orderList)){
            orderList.forEach(order->{
                //计算工单中的数量
                calculateNum4WorkOrder(order);
                //是否展示任务列表
//                getShowTaskList(order);
                // 设置工单的警示标示
                calculateWorkOrderWarnFlag(order);
            });
        }
        return orderList;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 获取是否展示任务列表
     * @date 2023/3/20 16:54
     * @param order
     * @return void
     **/
    private void getShowTaskList(MicroManufactureWorkOrder order) {
        MicroManufactureTask param = new MicroManufactureTask();
        param.setWorkOrderNo(order.getWorkOrderNo());
        order.setShowTaskList(CollectionUtils.isNotEmpty(microManufactureTaskMapper.selectMicroManufactureTaskList(param)));
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 计算工单中的数量
     * @date 2023/3/17 17:03
     * @param order
     * @return void
     **/
    private void calculateNum4WorkOrder(MicroManufactureWorkOrder order) {
        BigDecimal multiplicand = new BigDecimal(100);
        String completionRate = "0";
        BigDecimal waitProduceNum = BigDecimal.ZERO;
        //如果订单计划数量==0 完成率和待产数默认为0
        if (order.getWorkOrderNum().equals(BigDecimal.ZERO)){
            order.setWaitProduceNum(waitProduceNum);
            order.setCompletionRate(completionRate);
            return;
        }
        //计算待产数量和完成率 如果计划数-完成数<0 且 完成数>=0
        if (order.getWorkOrderNum().compareTo(order.getFinishNum())>0&&BigDecimal.ZERO.compareTo(order.getFinishNum())<=0){
            waitProduceNum = order.getWorkOrderNum().subtract(order.getFinishNum());
            completionRate = order.getFinishNum().divide(order.getWorkOrderNum(),2, RoundingMode.HALF_UP).multiply(multiplicand).stripTrailingZeros().toPlainString();
        }
        order.setWaitProduceNum(waitProduceNum);
        order.setCompletionRate(completionRate);
    }

    /**
     * 设置工单的警示标示
     */
    private void calculateWorkOrderWarnFlag(MicroManufactureWorkOrder workOrder) {
        List<String> normalWorkOrderStatusList = Arrays.asList(WorkOrderStatusEnum.TO_BE_PRODCED.getCode(), WorkOrderStatusEnum.IN_PRODUCTION.getCode());
        if (normalWorkOrderStatusList.contains(workOrder.getWorkOrderStatus())) {
            Date currentDate = DateUtils.getNowDate();
            if (DateUtil.compare(workOrder.getPlanEndDate(),currentDate,DATE_PATTERN) < 0) {
                workOrder.setWorkOrderWarnFlag(OrderWarnFlagEnum.OVER_DATE_WARN.getCode());
            }
        }
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 完工
     *              1.校验是否为最后一个完工工单，如果是则完成订单
     *              2.完成当前工单
     * @date 2023/3/13 16:38
     * @param workOrder
     * @return int
     **/
    public int completeWorkOrder(MicroManufactureWorkOrder workOrder) {
        //校验是否为最后一个完工工单，如果是则关闭订单
        completeOrder(workOrder.getOrderNo(),workOrder.getWorkOrderNo());

        //完成当前工单
        workOrder.setProduceEndDate(workOrder.getLastWorkSubmitDate());
        workOrder.setWorkOrderStatus(WorkOrderStatusEnum.FINISHED.getCode());
        workOrder.setLastUpdBy(String.valueOf(SecurityUtils.getUserId()));
        workOrder.setLastUpdDate(DateUtils.getNowDate());
        return microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder4Status(workOrder);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 关闭或完工时，校验是否连带完成工单
     * @date 2023/3/29 10:48
     * @param orderNo
     * @param workOrderNo
     * @return void
     **/
    public void completeOrder(String orderNo,String workOrderNo) {
        MicroManufactureWorkOrder param = new MicroManufactureWorkOrder();
        param.setOrderNo(orderNo);
        List<MicroManufactureWorkOrder> workOrderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderList4NotComplete(param);
        //没有未完工（除完工、关单外）的工单或者只有一条且是当前操作的工单 --> 完成订单
        boolean noWorkOrdersOrOnlyCurrent = CollectionUtil.isEmpty(workOrderList)
                || (workOrderList.size() == 1 && workOrderList.get(0).getWorkOrderNo().equals(workOrderNo));
        if (noWorkOrdersOrOnlyCurrent) {
            MicroManufactureOrder order = new MicroManufactureOrder();
            order.setOrderNo(orderNo);
            order.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
            manufactureOrderMapper.updateMicroManufactureOrder4Status(order);
        }
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 关闭的工单重启
     *              1.校验是否所有工单已经完成（关闭、完工），如果是则重启订单
     *              2.重启工单
     * @date 2023/3/21 10:05
     * @param workOrder
     * @return int
     **/
    public int reBootWorkOrder(MicroManufactureWorkOrder workOrder) {
        // 已完工入库后不允许重启
        MicroManufactureWorkOrder workOrderInfo = microManufactureWorkOrderMapper.selectNormalMicroManufactureWorkOrderById(workOrder.getId());
        if(null != workOrderInfo && IsCompleteEnum.YES == IsCompleteEnum.getEnum(workOrderInfo.getIsComplete())) {
            throw new CustomException("工单已完工入库，无法重启！");
        }

        reBootOrder(workOrder.getOrderNo(),workOrder.getWorkOrderNo());
        workOrder.setWorkOrderStatus(workOrder.getBeforeStatus());
        workOrder.setBeforeStatus(null);
        return microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder4Status(workOrder);
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 重启工单时，校验是否连带重启订单
     * @date 2023/3/29 11:05
     * @param orderNo
     * @param workOrderNo
     * @return void
     **/
    public void reBootOrder(String orderNo, String workOrderNo) {
        MicroManufactureWorkOrder param = new MicroManufactureWorkOrder();
        param.setOrderNo(orderNo);
        List<MicroManufactureWorkOrder> workOrderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderList4NotComplete(param);
        //没有未完工（除完工、关单外）的工单或者只有一条且是当前操作的工单 --> 完成订单
        if (CollectionUtil.isEmpty(workOrderList)){
            MicroManufactureOrder order = new MicroManufactureOrder();
            order.setOrderNo(orderNo);
            order.setOrderStatus(OrderStatusEnum.IN_PRODUCTION.getCode());
            manufactureOrderMapper.updateMicroManufactureOrder4Status(order);
        }
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 关闭工单
     *              1.校验是否为最后一个关单工单，如果是则完成订单
     *      *       2.关闭当前工单
     * @date 2023/3/21 10:05
     * @param workOrder
     * @return int
     **/
    public int closeWorkOrder(MicroManufactureWorkOrder workOrder) {
        //1.校验是否为最后一个关单工单，如果是则完成工单
        completeOrder(workOrder.getOrderNo(),workOrder.getWorkOrderNo());
        //2.关闭当前工单
//        workOrder.setProduceEndDate(workOrder.getLastWorkSubmitDate()); 
        workOrder.setBeforeStatus(workOrder.getWorkOrderStatus());
        workOrder.setWorkOrderStatus(WorkOrderStatusEnum.CLOSED.getCode());
        return microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder4Status(workOrder);
    }

    @Override
    public List<MicroManufactureWorkOrder> getStatusCount(MicroManufactureWorkOrder microManufactureWorkOrder){
        List<MicroManufactureWorkOrder> statusCountList = new ArrayList<>(microManufactureWorkOrder.getStatusList().size());
        for (String status : microManufactureWorkOrder.getStatusList()) {
            microManufactureWorkOrder.setWorkOrderStatus(status);
            int num = microManufactureWorkOrderMapper.getStatusCountByTask(microManufactureWorkOrder);
            MicroManufactureWorkOrder workOrder = new MicroManufactureWorkOrder();
            workOrder.setWorkOrderStatus(status);
            workOrder.setWorkOrderStatusCount(num);
            workOrder.setWorkOrderStatusName(WorkOrderStatusEnum.getEnumDesc(status));
            statusCountList.add(workOrder);
        }
        return statusCountList;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 批量保存工单
     * @date 2023/4/7 9:54
     * @param workOrderList
     * @return int
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveWorkOrders(List<MicroManufactureWorkOrder> workOrderList) {
        int result = 0;
        if (CollectionUtils.isNotEmpty(workOrderList)){
            List<String> allExistWorkOrderNoList = microManufactureWorkOrderMapper.getAllExistWorkOrderNoListInCurrentDate();
            // 本次手动要添加的工单号
            List<String> manualWorkOrderNoList = workOrderList.stream()
                    .filter(obj -> obj.getWorkOrderNo() != null)
                    .map(MicroManufactureWorkOrder::getWorkOrderNo)
                    .collect(Collectors.toList());
            allExistWorkOrderNoList.addAll(manualWorkOrderNoList);
            // 新增工单
            for (MicroManufactureWorkOrder workOrder:workOrderList){
                result += saveWorkOrder(workOrder, allExistWorkOrderNoList);
            }
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 批量修改工单
     * @date 2023/4/7 9:54
     * @param workOrderList
     * @return int
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWorkOrders(List<MicroManufactureWorkOrder> workOrderList) {
        int result = 0;
        if (CollectionUtils.isNotEmpty(workOrderList)){
            for (MicroManufactureWorkOrder workOrder:workOrderList){
                result+=microManufactureWorkOrderMapper.updateMicroManufactureWorkOrder(workOrder);
            }
        }
        return result;
    }

    /**
     * @author cosmo-hhim-open Team
     * @description 查询工单进度
     * @date 2023/4/7 9:54
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    @Override
    public List<MicroManufactureWorkOrder> selectWorkOrderProgressList(MicroManufactureWorkOrder condition) {
        List<MicroManufactureWorkOrder> orderList = microManufactureWorkOrderMapper.selectWorkOrderListWithCustomerAndProduct(condition);
        if (CollectionUtils.isNotEmpty(orderList)){
            MicroManufactureTask param = new MicroManufactureTask();
            orderList.forEach(order->{
                param.setWorkOrderNo(order.getWorkOrderNo());
                order.setTaskList(microManufactureTaskService.selectMicroManufactureTaskList(param));
            });
        }
        return orderList;
    }

    /**
     * 根据工单号查询工单相关信息-报工记录审产
     */
    @Override
    public List<MicroManufactureWorkOrder> getWorkOrderInfoBySubmit(List<String> workOrderNoList) {
        return microManufactureWorkOrderMapper.getWorkOrderInfoBySubmit(workOrderNoList);
    }

    /**
     * 根据条件获取范围内物料缺料信息
     */
    @Override
    public List<MicroManufactureWorkOrderMaterialEntity> selectWorkOrderShortageMaterialByStandardBom(MicroManufactureWorkOrder condition) {
        //取出条件范围内所有工单成品/半成品下一级物料的需求数量及对应工单号
        List<MicroManufactureWorkOrderMaterialEntity> materialDemandList = microManufactureWorkOrderMapper.selectMaterialDemandInfoByStandardBom(condition);
        materialDemandList = materialDemandList.stream().filter(m -> m.getProductId() != null).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(materialDemandList)) {
            return Collections.emptyList();
        }
        //根据工单号+物料序列码取出对应的实际需求数量
        Map<String, MicroPickMaterials> materialShortageMap = pickMaterialsService.selectMaterialShortageInfo(materialDemandList);
        HashMap<String, MicroManufactureWorkOrderMaterialEntity> materialMap = new HashMap<>(8);
        for (MicroManufactureWorkOrderMaterialEntity material : materialDemandList) {
            String groupKey = String.join("&", material.getWorkOrderNo(), material.getProductSeq());
            MicroPickMaterials pickMaterials;
            if (materialShortageMap.containsKey(groupKey)) {
                BigDecimal demandNum = material.getDemandNum();
                pickMaterials = materialShortageMap.get(groupKey);
//              实际需求数量 = 工单标准用量  -  实际已投, 实际需求数量<0 说明投料大于需求,不在缺料范围内,排除掉
                BigDecimal actualDemandNum = demandNum.subtract(pickMaterials.getPickingNumber());
                if (actualDemandNum.signum() <= 0) {
                    continue;
                }
                material.setDemandNum(actualDemandNum.setScale(4, RoundingMode.UP));
            }
            MicroManufactureWorkOrderMaterialEntity value = material;
//        根据物料序列码来汇总多个工单下各物料缺料信息
            if (materialMap.containsKey(material.getProductSeq())) {
                value = materialMap.get(material.getProductSeq());
                value.setDemandNum(value.getDemandNum().add(material.getDemandNum()));
            }
            materialMap.put(material.getProductSeq(), value);
        }
        return new ArrayList<>(materialMap.values());
    }

    /**
     * 根据条件范围获取物料缺料工单信息
     *
     */
    @Override
    public List<MicroManufactureWorkOrderMaterialEntity> selectShortageDetailList(MicroManufactureWorkOrder condition) {
        List<MicroManufactureWorkOrderMaterialEntity> materialDemandList = microManufactureWorkOrderMapper.selectMaterialShortageDetailByStandardBom(condition);
        //根据工单号+物料序列码取出对应的实际需求数量
        Map<String, MicroPickMaterials> materialShortageMap = pickMaterialsService.selectMaterialShortageInfo(materialDemandList);
        for (MicroManufactureWorkOrderMaterialEntity material : materialDemandList) {
            String groupKey = String.join("&", material.getWorkOrderNo(), material.getProductSeq());
            MicroPickMaterials pickMaterials;
            if (materialShortageMap.containsKey(groupKey)) {
                BigDecimal demandNum = material.getDemandNum();
                pickMaterials = materialShortageMap.get(groupKey);
//              实际需求数量 = 工单标准用量  -  实际已投, 实际需求数量<0 说明投料大于需求,不在缺料范围内,排除掉
                BigDecimal actualDemandNum = demandNum.subtract(pickMaterials.getPickingNumber());
                if (actualDemandNum.signum() <= 0) {
                    actualDemandNum = BigDecimal.ZERO;
                }
                material.setDemandNum(actualDemandNum.setScale(4, RoundingMode.UP));
            }
        }
        return materialDemandList;
    }
}

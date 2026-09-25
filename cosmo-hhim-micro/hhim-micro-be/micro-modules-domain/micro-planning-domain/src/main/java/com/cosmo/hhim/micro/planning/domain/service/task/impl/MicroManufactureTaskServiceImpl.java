/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.task.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroManufactureOrderMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.planning.domain.service.task.IMicroManufactureTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生产任务Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
@Service
public class MicroManufactureTaskServiceImpl implements IMicroManufactureTaskService
{
    @Autowired
    private MicroManufactureTaskMapper microManufactureTaskMapper;

    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;

    @Autowired
    private MicroManufactureOrderMapper microManufactureOrderMapper;

    /**
     * 查询生产任务
     * 
     * @param id 生产任务ID
     * @return 生产任务
     */
    @Override
    public MicroManufactureTask selectMicroManufactureTaskById(Long id)
    {
        return microManufactureTaskMapper.selectMicroManufactureTaskById(id);
    }

    /**
     * 查询生产任务列表
     * 
     * @param microManufactureTask 生产任务
     * @return 生产任务
     */
    @Override
    public List<MicroManufactureTask> selectMicroManufactureTaskList(MicroManufactureTask microManufactureTask)
    {
        List<MicroManufactureTask> tasks = microManufactureTaskMapper.selectMicroManufactureTaskListByCondition(microManufactureTask);
        if (CollectionUtil.isNotEmpty(tasks)){
            tasks.forEach(task->{
                //計算完成率
                calculateTaskNum(task);
            });
        }
        return tasks;
    }

    private void calculateTaskNum(MicroManufactureTask task) {
        BigDecimal multiplicand = new BigDecimal(100);
        String completionRate = "0";
        //如果订单计划数量==0 完成率和待产数默认为0
        if (task.getTaskPlanNum().equals(BigDecimal.ZERO)){
            task.setCompletionRate(completionRate);
            return;
        }
        //计算完成率 如果计划数-良品数<0 且 良品数>=0
        if (task.getTaskPlanNum().compareTo(task.getNgNum())>0&&BigDecimal.ZERO.compareTo(task.getNgNum())<=0){
            completionRate = task.getPassNum().divide(task.getTaskPlanNum(),2, RoundingMode.HALF_UP).multiply(multiplicand).stripTrailingZeros().toPlainString();
        }
        task.setCompletionRate(completionRate);
    }

    /**
     * 新增生产任务
     * 
     * @param microManufactureTask 生产任务
     * @return 结果
     */
    @Override
    public int insertMicroManufactureTask(MicroManufactureTask microManufactureTask)
    {
        return microManufactureTaskMapper.insertMicroManufactureTask(microManufactureTask);
    }

    /**
     * 修改生产任务
     * 
     * @param microManufactureTask 生产任务
     * @return 结果
     */
    @Override
    public int updateMicroManufactureTask(MicroManufactureTask microManufactureTask)
    {
        return microManufactureTaskMapper.updateMicroManufactureTask(microManufactureTask);
    }

    /**
     * 批量删除生产任务
     * 
     * @param ids 需要删除的生产任务ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureTaskByIds(Long[] ids)
    {
        return microManufactureTaskMapper.deleteMicroManufactureTaskByIds(ids);
    }

    /**
     * 删除生产任务信息
     * 
     * @param id 生产任务ID
     * @return 结果
     */
    @Override
    public int deleteMicroManufactureTaskById(Long id)
    {
        return microManufactureTaskMapper.deleteMicroManufactureTaskById(id);
    }

    /**
     * 报工记录变更
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void recordChange(Boolean operateFlag,List<MicroManufactureTask> microManufactureTaskList){
        Long userId= SecurityUtils.getUserId();
        List<MicroManufactureTask> updateList=new ArrayList<>();
        //根据操作类型变更生产任务完成数
        for(MicroManufactureTask microManufactureTask:microManufactureTaskList){
            if(!operateFlag){
                microManufactureTask.setPassNum(BigDecimal.ZERO.subtract(microManufactureTask.getPassNum()));
                microManufactureTask.setNgNum(BigDecimal.ZERO.subtract(microManufactureTask.getNgNum()));
            }
        }
        Map<String,List<MicroManufactureTask>> taskMap=microManufactureTaskList.stream().collect(Collectors.groupingBy(MicroManufactureTask::getTaskNo));
        Set<Map.Entry<String, List<MicroManufactureTask>>> taskEntries = taskMap.entrySet();
        Iterator<Map.Entry<String, List<MicroManufactureTask>>> taskIterator = taskEntries.iterator();
        while (taskIterator.hasNext()){
            Map.Entry<String, List<MicroManufactureTask>> tempEnetity = taskIterator.next();
            BigDecimal passNum=tempEnetity.getValue().stream().map(MicroManufactureTask::getPassNum).reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal ngNum=tempEnetity.getValue().stream().map(MicroManufactureTask::getNgNum).reduce(BigDecimal.ZERO,BigDecimal::add);
            MicroManufactureTask taskEntity=new MicroManufactureTask();
            taskEntity.setTaskNo(tempEnetity.getKey());
            taskEntity.setPassNum(passNum);
            taskEntity.setNgNum(ngNum);
            updateList.add(taskEntity);
        }
        microManufactureTaskMapper.addTaskFinishNum(String.valueOf(userId),updateList);
        //查询最后一道工序对应的工单号
        List<String> workOrderNoList=microManufactureTaskList.stream().filter(entity->IsLastProcessEnum.YES.getCode().equals(entity.getIsLastProcess())).map(MicroManufactureTask::getWorkOrderNo).collect(Collectors.toList());
        if(CollectionUtil.isNotEmpty(workOrderNoList)){
            //查询主工单
            List<MicroManufactureWorkOrder> workOrderList=microManufactureWorkOrderMapper.getOrderNoByNo(workOrderNoList);
            //最后一道工序修改工单的完成数量
            List<MicroManufactureWorkOrder> updateWorkOrderList=new ArrayList<>();
            Map<String,List<MicroManufactureTask>> workOrderMap=microManufactureTaskList.stream().filter(entity->IsLastProcessEnum.YES.getCode().equals(entity.getIsLastProcess())).collect(Collectors.groupingBy(MicroManufactureTask::getWorkOrderNo));
            Set<Map.Entry<String, List<MicroManufactureTask>>> tempEntries = workOrderMap.entrySet();
            Iterator<Map.Entry<String, List<MicroManufactureTask>>> tempIterator = tempEntries.iterator();
            while (tempIterator.hasNext()){
                Map.Entry<String, List<MicroManufactureTask>> tempEnetity = tempIterator.next();
                BigDecimal passNum=tempEnetity.getValue().stream().map(MicroManufactureTask::getPassNum).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal ngNum=tempEnetity.getValue().stream().map(MicroManufactureTask::getNgNum).reduce(BigDecimal.ZERO,BigDecimal::add);
                MicroManufactureWorkOrder workOrderEntity=new MicroManufactureWorkOrder();
                MicroManufactureWorkOrder temp=workOrderList.stream().filter(e->e.getWorkOrderNo().equals(tempEnetity.getKey())).findFirst().orElse(null);
                if(null !=temp){
                    workOrderEntity.setOrderNo(temp.getOrderNo());
                }
                workOrderEntity.setWorkOrderNo(tempEnetity.getKey());
                workOrderEntity.setFinishNum(passNum);
                workOrderEntity.setNgNum(ngNum);
                updateWorkOrderList.add(workOrderEntity);
            }
            if(CollectionUtil.isNotEmpty(updateWorkOrderList)){
                microManufactureWorkOrderMapper.addWorkOrderFinishNum(String.valueOf(userId),updateWorkOrderList);
                //最后一道工序修改主工单对应订单的完成数量
                List<MicroManufactureOrder> orderList=new ArrayList<>();
                List<String> orderNoList=updateWorkOrderList.stream().filter(e->!StringUtils.isEmpty(e.getOrderNo())).map(MicroManufactureWorkOrder::getOrderNo).collect(Collectors.toList());
                if(CollectionUtil.isNotEmpty(orderNoList)){
                    Map<String,List<MicroManufactureWorkOrder>> orderMap=updateWorkOrderList.stream().filter(e->!StringUtils.isEmpty(e.getOrderNo())).collect(Collectors.groupingBy(MicroManufactureWorkOrder::getOrderNo));
                    Set<Map.Entry<String, List<MicroManufactureWorkOrder>>> orderEntries = orderMap.entrySet();
                    Iterator<Map.Entry<String, List<MicroManufactureWorkOrder>>> orderIterator = orderEntries.iterator();
                    while (orderIterator.hasNext()){
                        Map.Entry<String, List<MicroManufactureWorkOrder>> tempEnetity = orderIterator.next();
                        if(orderNoList.contains(tempEnetity.getKey())){
                            BigDecimal passNum=tempEnetity.getValue().stream().map(MicroManufactureWorkOrder::getFinishNum).reduce(BigDecimal.ZERO,BigDecimal::add);
                            BigDecimal ngNum=tempEnetity.getValue().stream().map(MicroManufactureWorkOrder::getNgNum).reduce(BigDecimal.ZERO,BigDecimal::add);
                            MicroManufactureOrder orderEntity=new MicroManufactureOrder();
                            orderEntity.setOrderNo(tempEnetity.getKey());
                            orderEntity.setFinishNum(passNum);
                            orderEntity.setNgNum(ngNum);
                            orderList.add(orderEntity);
                        }
                    }
                    if(CollectionUtil.isNotEmpty(orderList)){
                        microManufactureOrderMapper.addOrderFinishNum(String.valueOf(userId),orderList);
                    }
                }
            }
        }
    }
}

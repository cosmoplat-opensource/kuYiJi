/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.complete.domain.mapper.MicroCompleteReportMapper;
import com.cosmo.hhim.micro.infrastructure.entity.MicroProductSelectEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.parallel.AsyncParallelExecutor;
import com.cosmo.hhim.micro.infrastructure.parallel.TaskRespEntity;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedProductStorageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroProductFacadeServiceImpl implements IMicroProductFacadeService {
    @Autowired
    private IMicroProductService productService;
    @Autowired
    private MicroProductMapper productMapper;
    @Autowired
    private IMicroTechnologyService technologyService;
    @Autowired
    private MicroFinishedProductStorageMapper finishedProductStorageMapper;
    @Autowired
    private MicroManufactureWorkOrderMapper workOrderMapper;
    @Autowired
    private MicroCompleteReportMapper completeReportMapper;
    @Autowired
    private MicroProcessStorageMapper processStorageMapper;
    @Autowired
    private MicroWorkSubmitMapper workSubmitMapper;

    @Override
    public List<MicroProductSelectEntity> selectMicroProductByName(String key, boolean mixed) {
        List<MicroProductSelectEntity> productList = productService.selectMicroProductByName(key, mixed);
        Set<String> standardProductSet = technologyService.judgeStandardProductByList(null,
                productList.stream().map(MicroProductSelectEntity::getItemSeq).collect(Collectors.toList()));
        productList.forEach(p -> p.setStandard(standardProductSet.contains(p.getItemSeq())));
        return productList;
    }

    /**
     * 批量删除产品
     * 组合各个并行任务, 任务包括业务表中是否存在对应产品数据, 有任何一个任务返回数据, 就无法删除该产品
     * 1.micro_finished_product_storage 成品库存表
     * 2.micro_manufacture_work_order 生产工单表
     * 3.micro_complete_report 完工报告
     * 4.micro_process_storage 在制品库存表
     * 5.micro_work_submit 报工表
     *
     * @param ids
     * @return
     */
    @Override
    public int removeProductByIds(Long[] ids) {
        AsyncParallelExecutor<TaskRespEntity<List<MicroSelectEntity>>> executor = new AsyncParallelExecutor<>(future -> {
            try {
                return !CollectionUtils.isEmpty(future.get().getData());
            } catch (Exception e) {
                log.error("Exception occurred when the exit_strategy was executed:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
                throw new CustomException("执行退出条件时异常");
            }
        });
        List<Callable<TaskRespEntity<List<MicroSelectEntity>>>> tasks = this.assembleTasks(ids);
        this.judgeResp(executor.anyOfExecute(tasks));
        return productService.deleteMicroProductByIds(ids);
    }

    /**
     * 判断返回值
     *
     * @param resp
     */
    private void judgeResp(TaskRespEntity<List<MicroSelectEntity>> resp) {
        if (resp != null) {
            List<MicroSelectEntity> existList = resp.getData();
            String mark = resp.getMark();
            if (!CollectionUtils.isEmpty(existList)) {
                String s = existList.stream()
                        .map(r -> r.getItemName() + "(" + r.getItemCode() + ")")
                        .distinct()
                        .collect(Collectors.joining(","));
                throw new CustomException(s + "产品存在[" + mark + "]记录,无法删除!");
            }
        }
    }

    /**
     * 组装任务
     * 1.micro_finished_product_storage 成品库存表
     * 2.micro_manufacture_work_order 生产工单表
     * 3.micro_complete_report 完工报告
     * 4.micro_process_storage 在制品库存表
     * 5.micro_work_submit 报工表
     *
     * @param ids
     * @return
     */
    private List<Callable<TaskRespEntity<List<MicroSelectEntity>>>> assembleTasks(Long[] ids) {
        List<Callable<TaskRespEntity<List<MicroSelectEntity>>>> tasks = new ArrayList<>();
        // 查询micro_finished_product_storage 成品库存表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task1 = () -> {
            List<MicroSelectEntity> existList = finishedProductStorageMapper.selectReProductByProductIds(ids);
            return new TaskRespEntity<>("成品库存", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task1);
        // 查询micro_manufacture_work_order 生产工单表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task2 = () -> {
            List<MicroSelectEntity> existList = workOrderMapper.selectReProductByProductIds(ids);
            return new TaskRespEntity<>("生产工单", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task2);
        // 查询micro_complete_report 完工报告
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task3 = () -> {
            List<MicroSelectEntity> existList = completeReportMapper.selectReProductByProductIds(ids);
            return new TaskRespEntity<>("完工报告", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task3);
        // 查询micro_process_storage 在制品库存表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task4 = () -> {
            List<MicroSelectEntity> existList = processStorageMapper.selectReProductByProductIds(ids);
            return new TaskRespEntity<>("在制品库存", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task4);
        // 查询micro_work_submit 报工表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task5 = () -> {
            List<MicroSelectEntity> existList = workSubmitMapper.selectReProductByProductIds(ids);
            return new TaskRespEntity<>("报工", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task5);
        return tasks;
    }

}

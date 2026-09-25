/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.micro.application.assembler.tech.MicroTechnologyAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessMixedResultDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessMixedSelectDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessMultiMixedResultDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessStockQueryDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroProcessFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessSelectEntity;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroLastPreProcessEntity;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessMixedResultEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProductProcessEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.SimpleProcessResult;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroProcessChainMapper;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.parallel.AsyncParallelExecutor;
import com.cosmo.hhim.micro.infrastructure.parallel.TaskRespEntity;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroProcessFacadeServiceImpl implements IMicroProcessFacadeService {
    @Autowired
    private IMicroTechnologyService technologyService;
    @Autowired
    private MicroProcessChainMapper processChainMapper;
    @Autowired
    private IMicroProcessCommonService processCommonService;
    @Autowired
    private MicroProcessCommonMapper processCommonMapper;
    @Autowired
    private IMicroWorkSubmitService workSubmitService;
    @Autowired
    private MicroManufactureTaskMapper taskMapper;
    @Autowired
    private MicroProcessStorageMapper processStorageMapper;

    /**
     * 有工艺的产品，推荐工艺默认的工序
     *
     * @param productSeq
     * @param operateProcessSeq
     * @return
     */
    @Override
    public List<SimpleProcessResult> defaultRecommendByStandardTech(String productSeq, String operateProcessSeq) {
        log.info("请求的参数为:productSeq:{}operateProcessSeq:{}", productSeq, operateProcessSeq);
        if (StringUtils.isEmpty(operateProcessSeq)) {
            return Collections.emptyList();
        }
        List<MicroProcessChainBindEntity> bindEntityList = technologyService.selectMicroTechChain(
                MicroTechnologyAssembler.toTechQuery(ThreadContext.get(Constants.TARGET_CUSTOMER).toString(), null, productSeq));
        return processCommonService.defaultRecommendByStandardTech(bindEntityList, operateProcessSeq);
    }

    /**
     * 下拉工序附带库存查询实体
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<MicroProcessSelectEntity> selectProcessStock(MicroProcessStockQueryDTO queryDTO) {
        return processCommonMapper.selectMicroProcessStock(queryDTO.getProductSeq(), queryDTO.getProcessList(), ThreadContext.get(Constants.TARGET_CUSTOMER).toString());
    }

    /**
     * 批量报工流程中,根据产品查询工序下拉列表,如果非标准工艺的产品,需要将推荐的数据放到头部
     * 1.没有产品推荐工序
     * 2.根据产品推送现工序
     * 2.1存在标准工艺   只能在工艺链范围内选择
     * 2.2草稿工艺      优先放入草稿工序,后追加
     * 2.3无工艺        库易记:从预研团队获取 工易派:所有
     * 3.根据产品+现工序    推荐前工序
     * 3.1存在标准工艺   找前一个
     * 3.2草稿工艺      优先推荐前一个
     * 3.3无工艺        预研团队推荐
     *
     * @param mixedSelectDTO
     * @return
     */
    @Override
    public MicroProcessMixedResultDTO selectMixedByName(MicroProcessMixedSelectDTO mixedSelectDTO) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(mixedSelectDTO));
        MicroProcessMixedResultDTO resultDTO = new MicroProcessMixedResultDTO();
        List<MicroSelectEntity> extraList = new ArrayList<>();
        String searchKey = mixedSelectDTO.getSearchKey();
        String productSeq = mixedSelectDTO.getProductSeq();
        boolean range = false;
        if (!productEmpty(mixedSelectDTO)) {
            String operateProcessSeq = mixedSelectDTO.getOperateProcessSeq();
            MicroProcessMixedResultEntity resultEntity = technologyService.selectRangeProcessInTechByProductOrOperateProcess(productSeq, operateProcessSeq);
            resultDTO.setStandard(resultEntity.isStandard());
            extraList = resultEntity.getResultList();
            if (resultEntity.isFinalReturn()) {
                if (CollectionUtil.isNotEmpty(extraList) && !StringUtils.isEmpty(searchKey)) {
                    // 根据searchKey在过滤一下
                    extraList = extraList.stream().filter(obj -> obj.getItemName().contains(searchKey) || obj.getItemCode().contains(searchKey))
                            .collect(Collectors.toList());
                }
                resultDTO.setSelectList(extraList);
                return resultDTO;
            }
            range = resultEntity.isStandard();
            if (!resultEntity.isStandard() && !resultEntity.isDraft()) {
                //无工艺(既不是标准又不是草稿)  不走推荐,正常查询所有
            }
            if (CollectionUtil.isNotEmpty(extraList) && !StringUtils.isEmpty(searchKey)) {
                // 根据searchKey过滤
                extraList = extraList.stream().filter(obj -> obj.getItemName().contains(searchKey) || obj.getItemCode().contains(searchKey))
                        .collect(Collectors.toList());
            }
        }
        List<MicroProcessSelectEntity> processList = processCommonService.selectMicroProcessByName(searchKey, productSeq, range);
        if (!CollectionUtils.isEmpty(processList)) {
            List<MicroSelectEntity> copyToList = BeanUtil.copyToList(processList, MicroSelectEntity.class);
            if (!CollectionUtils.isEmpty(extraList)) {
                copyToList.addAll(0, extraList);
            }
            resultDTO.setSelectList(copyToList.stream().distinct().collect(Collectors.toList()));
        }
        return resultDTO;
    }

    /**
     * 多产品+多现工序推荐第一个前工序+是否首尾序
     *
     * @param multiList
     * @return
     */
    @Override
    public List<MicroProcessMultiMixedResultDTO> selectMultiMixed(List<MicroProcessMixedSelectDTO> multiList) {
        log.info("selectMultiMixed请求的参数为:{}", JSONObject.toJSON(multiList));
        List<MicroProcessMultiMixedResultDTO> result = new ArrayList<>();
        List<MicroProductProcessEntity> rawStandardList = new ArrayList<>();
        List<MicroLastPreProcessEntity> rawNonStandardList = new ArrayList<>();
        MicroProductProcessEntity processEntity;
        MicroLastPreProcessEntity lastPreProcessEntity;
        for (MicroProcessMixedSelectDTO dto : multiList) {
            if (dto.getStandard()) {
                processEntity = new MicroProductProcessEntity();
                processEntity.setProductSeq(dto.getProductSeq());
                processEntity.setOperateProcessSeq(dto.getOperateProcessSeq());
                rawStandardList.add(processEntity);
            } else {
                lastPreProcessEntity = new MicroLastPreProcessEntity();
                BeanUtil.copyProperties(dto, lastPreProcessEntity);
                rawNonStandardList.add(lastPreProcessEntity);
            }
        }
        if (!CollectionUtils.isEmpty(rawStandardList)) {
            List<MicroProductProcessEntity> standardList = technologyService.getPreProcessByStandardProductAndProcess(rawStandardList);
            result.addAll(BeanUtil.copyToList(standardList, MicroProcessMultiMixedResultDTO.class));
        }
        if (!CollectionUtils.isEmpty(rawNonStandardList)) {
            List<MicroLastPreProcessEntity> nonStandardList = workSubmitService.findLatestPreProcessAndFirstOrLast(rawNonStandardList);
            result.addAll(BeanUtil.copyToList(nonStandardList, MicroProcessMultiMixedResultDTO.class));
        }
        this.getPreprocessStock(result);
        log.info("selectMultiMixed返回的的参数为:{}", JSONObject.toJSON(result));
        return result;
    }

    /**
     * 根据工序id集合删除工序
     * manufacture_work_task 生产任务表
     * micro_process_chain 工艺链
     * process_storage 在制品库存表
     * work_submit 报工表
     *
     * @param ids
     * @return
     */
    @Override
    public int removeProcessByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 1;
        }
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
        return processCommonService.deleteMicroProcessCommonByIds(ids);
    }

    /**
     * 组装task
     * manufacture_work_task 生产任务表
     * micro_process_chain 工艺链
     * process_storage 在制品库存表
     * work_submit 报工表
     *
     * @param ids
     * @return
     */
    private List<Callable<TaskRespEntity<List<MicroSelectEntity>>>> assembleTasks(Long[] ids) {
        List<Callable<TaskRespEntity<List<MicroSelectEntity>>>> tasks = new ArrayList<>();
        // 查询 manufacture_work_task 生产任务表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task1 = () -> {
            List<MicroSelectEntity> existList = taskMapper.selectReProcessByProcessIds(ids);
            return new TaskRespEntity<>("生产任务", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task1);
        // 查询 micro_process_chain 工艺链
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task2 = () -> {
            List<MicroSelectEntity> existList = processChainMapper.selectReProcessByProcessIds(ids);
            return new TaskRespEntity<>("工艺路线", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task2);
        // 查询 process_storage 在制品库存表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task3 = () -> {
            List<MicroSelectEntity> existList = processStorageMapper.selectReProcessByProcessIds(ids);
            return new TaskRespEntity<>("在制品库存", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task3);
        // 查询 work_submit 报工表
        Callable<TaskRespEntity<List<MicroSelectEntity>>> task4 = () -> {
            List<MicroSelectEntity> existList = workSubmitService.selectReProcessByProcessIds(ids);
            return new TaskRespEntity<>("报工", CollectionUtils.isEmpty(existList) ? Collections.emptyList() : existList);
        };
        tasks.add(task4);
        return tasks;
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
                throw new CustomException(s + "工序存在[" + mark + "]记录,无法删除!");
            }
        }
    }

    private void getPreprocessStock(List<MicroProcessMultiMixedResultDTO> result) {
        if (!CollectionUtils.isEmpty(result)) {
            Map<String, List<MicroProcessMultiMixedResultDTO>> collect = result.stream().collect(Collectors.groupingBy(MicroProcessMultiMixedResultDTO::getProductSeq));
            String customerCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
            List<MicroProcessMultiMixedResultDTO> value;
            for (Map.Entry<String, List<MicroProcessMultiMixedResultDTO>> entry : collect.entrySet()) {
                String productSeq = entry.getKey();
                value = entry.getValue();
                List<Long> processIds = value.stream().map(MicroProcessMultiMixedResultDTO::getPreProcessId)
                        .filter(preProcessId -> preProcessId != null && preProcessId != 0)
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(processIds)) {
                    continue;
                }
                List<MicroProcessSelectEntity> selected = processCommonMapper.selectMicroProcessStockByIds(processIds, productSeq, customerCode);
                Map<Long, MicroProcessSelectEntity> processStockMap = selected.stream().collect(Collectors.groupingBy(MicroProcessSelectEntity::getItemId,
                        Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0))));
                MicroProcessSelectEntity entity;
                for (MicroProcessMultiMixedResultDTO resultDTO : value) {
                    Long preProcessId = resultDTO.getPreProcessId();
                    if (processStockMap.containsKey(preProcessId)) {
                        entity = processStockMap.get(preProcessId);
                        resultDTO.setUnapprovedNum(entity.getUnapprovedNum());
                        resultDTO.setTotalStockNum(entity.getTotalStockNum());
                    }
                }
            }
        }
    }

    private boolean productEmpty(MicroProcessMixedSelectDTO mixedSelectDTO) {
        return StringUtils.isEmpty(mixedSelectDTO.getProductSeq());
    }
}

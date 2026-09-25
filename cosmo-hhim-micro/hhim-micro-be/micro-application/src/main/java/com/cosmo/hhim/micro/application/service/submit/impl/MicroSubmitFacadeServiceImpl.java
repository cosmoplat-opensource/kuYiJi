/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.submit.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.complete.MicroSettlementAssembler;
import com.cosmo.hhim.micro.application.assembler.submit.MicroWorkSubmitAssembler;
import com.cosmo.hhim.micro.application.assembler.tech.MicroTechnologyAssembler;
import com.cosmo.hhim.micro.application.dto.submit.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.TotalSubmitNumDTO;
import com.cosmo.hhim.micro.application.service.submit.IMicroSubmitFacadeService;
import com.cosmo.hhim.micro.application.util.TechChainUtils;
import com.cosmo.hhim.micro.base.domain.entity.check.*;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.submit.FirstOrLastProcess;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementReportEntity;
import com.cosmo.hhim.micro.complete.domain.mapper.MicroSettlementReportMapper;
import com.cosmo.hhim.micro.complete.domain.service.IMicroSettlementReportService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import com.cosmo.hhim.micro.infrastructure.designPattern.factory.SubmitStrategyFactory;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.SubmitStrategy;
import com.cosmo.hhim.micro.infrastructure.enums.*;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ng.CheckStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.events.FlushWarningMetricsEvent;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.ng.domain.entity.MicroQualityControlRecord;
import com.cosmo.hhim.micro.ng.domain.mapper.MicroQualityControlRecordMapper;
import com.cosmo.hhim.micro.planning.domain.entity.task.MicroManufactureTask;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.task.MicroManufactureTaskMapper;
import com.cosmo.hhim.micro.planning.domain.service.task.IMicroManufactureTaskService;
import com.cosmo.hhim.micro.planning.domain.service.work.IMicroManufactureWorkOrderService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroSubmitFacadeServiceImpl implements IMicroSubmitFacadeService {
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private MicroManufactureTaskMapper microManufactureTaskMapper;
    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;
    @Autowired
    private IMicroTechnologyService technologyService;
    @Autowired
    private IMicroManufactureTaskService microManufactureTaskService;
    @Autowired
    private IMicroManufactureWorkOrderService microManufactureWorkOrderService;
    @Autowired
    private IMicroProcessStorageService microProcessStorageService;
    @Autowired
    private MicroSupportUtil supportUtil;
    @Autowired
    private IMicroSettlementReportService settlementReportService;
    @Autowired
    private IMicroEmailService microEmailService;
    @Autowired
    private MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;
    @Autowired
    private MicroQualityControlRecordMapper microQualityControlRecordMapper;
    @Autowired
    private MicroSettlementReportMapper microSettlementReportMapper;
    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 新增报工记录
     *
     * @param microWorkSubmitDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createSubmitRecord(MicroWorkSubmitDto microWorkSubmitDto) {
        log.info("创建报工记录, productSeq:{}, processSeq:{}, preProcessSeq:{}",
                microWorkSubmitDto.getProductSeq(), microWorkSubmitDto.getOperateProcessSeq(), microWorkSubmitDto.getPreProcessSeq());
        // 2.报工domain实体
        MicroWorkSubmit microWorkSubmit = MicroWorkSubmitAssembler.toMicroWorkSubmitDomain(microWorkSubmitDto);
        // 3. 执行具体业务操作
        SubmitStrategy submitStrategy = SubmitStrategyFactory.getSubmitStrategy(SecurityUtils.getApplicationSign());
        MicroWorkSubmit submit = (MicroWorkSubmit) submitStrategy.submit(microWorkSubmit);
        return microWorkSubmitService.insertMicroWorkSubmit(submit);
    }

    /**
     * 更新报工记录
     *
     * @param microWorkSubmitDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroWorkSubmit(MicroWorkSubmitDto microWorkSubmitDto) {
        log.info("更新报工记录, productSeq:{}, processSeq:{}, preProcessSeq:{}",
                microWorkSubmitDto.getProductSeq(), microWorkSubmitDto.getOperateProcessSeq(), microWorkSubmitDto.getPreProcessSeq());
        // 校验参数
        this.checkSubmitParams(microWorkSubmitDto);
        // 校验报工记录是否有标准工艺并且是否符合标准工艺路线
        boolean flag = technologyService.validSubmitRecordTechInfo(microWorkSubmitDto.getProductSeq(),microWorkSubmitDto.getOperateProcessSeq(), microWorkSubmitDto.getPreProcessSeq());
        if (!flag) {
            throw new CustomException("报工记录不符合产品的工艺路线, 请编辑报工记录");
        }
        // Dto转换Domain
        MicroWorkSubmit microWorkSubmit = MicroWorkSubmitAssembler.toMicroWorkSubmitDomain(microWorkSubmitDto);
        return microWorkSubmitService.updateMicroWorkSubmit(microWorkSubmit);
    }

    /**
     * 删除报工记录
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMicroWorkSubmitById(Long id) {
        return microWorkSubmitService.deleteMicroWorkSubmitById(id);
    }

    /**
     * 批量删除报工记录信息
     *
     * @param ids
     */
    @Override
    public int removeMicroWorkSubmitRecordByIds(Long[] ids) {
        log.info("批量删除报工记录的ids:{}", JSONArray.toJSON(ids));
        // 查询质检记录, 有质检记录的报工不可以删除
        List<MicroQualityControlRecord> microQualityControlRecords = microQualityControlRecordMapper.selectMicroQualityControlRecordBySubmitIdList(Arrays.asList(ids));
        if (!CollectionUtils.isEmpty(microQualityControlRecords)) {
            throw new CustomException("报工存在质检记录不允许删除");
        }

        // 查询报工记录进行判断
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectMicroWorkSubmitExByIds(ids, null, null);
        if (CollectionUtils.isEmpty(microWorkSubmitDtoList)) {
            throw new CustomException("未查询到报工记录信息");
        }
        microWorkSubmitDtoList.forEach(obj -> {
            if (SubmitStatusEnum.APPROVED.getCode().equals(obj.getSubmitStatus())) {
                throw new CustomException("已审核的报工记录不可以删除");
            }
        });
        List<String> submitNoList = microWorkSubmitDtoList.stream().map(MicroWorkSubmitDto::getSubmitNo)
                .collect(Collectors.toList());
        // 删除报工变动历史记录
        String[] submitNos = submitNoList.toArray(new String[submitNoList.size()]);
        log.info("报工记录号为:{}", JSONArray.toJSON(submitNos));
        microWorkSubmitHistoryMapper.deleteMicroWorkSubmitHistoryBySubmitNos(submitNos);
        // 删除报工历史记录
        return microWorkSubmitMapper.deleteMicroWorkSubmitByIds(ids);
    }

    /**
     * 批量审核接口
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult checkSubmitRecordBatch(Long[] ids) {
        log.info("要审核的报工记录ids:{}", Arrays.toString(ids));
        // 1. 根据ids获取到报工记录
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitByIds(ids);
        // 2. 校验和过滤报工数据
        microWorkSubmitList = this.validAndFilterSubmitRecord(microWorkSubmitList);
        // 3. 报工记录不为null
        if (!CollectionUtils.isEmpty(microWorkSubmitList)) {
            // 审核之后的数据
            microWorkSubmitList = microWorkSubmitService.checkSubmitRecordBatch(microWorkSubmitList);
            // 调用工易派的方法, 更新任务单、工单等信息
            if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
                List<MicroManufactureTask> manufactureTasks = new ArrayList<>();
                // 实体转换
                microWorkSubmitList.forEach(obj -> {
                    MicroManufactureTask temp = MicroWorkSubmitAssembler.toManufactureTaskDomain(obj);
                    manufactureTasks.add(temp);
                });
                microManufactureTaskService.recordChange(true, manufactureTasks);
            } else {
                microWorkSubmitList.forEach(obj -> {
                    microProcessStorageService.changeStorage(obj, CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT);
                });
                // 刷新redis里面的产品 + 工序的良品率和产能
                publisher.publishEvent(new FlushWarningMetricsEvent((String) ThreadContext.get(Constants.TARGET_CUSTOMER)));
                settlementReportService.generateReportAfterCheck(MicroSettlementAssembler.submitToSettlement(microWorkSubmitList));
            }
        }
        return AjaxResult.success();
    }

    /**
     * 报工明细 - 审产接口 (需传递 id 和 isLastProcess 字段)
     *
     * @param microWorkSubmits
     */
    @Override
    public AjaxResult checkSubmitRecordBatchByProduct(List<MicroWorkSubmit> microWorkSubmits) {
        // 审核操作
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitService.checkSubmitRecordBatchByProduct(microWorkSubmits);
        // 更新相关库存信息
        microWorkSubmitList.forEach(obj -> {
            microProcessStorageService.changeStorage(obj, CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT);
        });
        settlementReportService.generateReportAfterCheck(MicroSettlementAssembler.submitToSettlement(microWorkSubmitList));
        // 刷新redis里面的产品 + 工序的良品率和产能
        publisher.publishEvent(new FlushWarningMetricsEvent((String) ThreadContext.get(Constants.TARGET_CUSTOMER)));
        return AjaxResult.success();
    }

    /**
     * 编辑并审核接口
     *
     * @param microWorkSubmitDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult editAndCheck(MicroWorkSubmitDto microWorkSubmitDto) {
        // 1.打印参数
        log.info("报工记录参数：{}", JSONObject.toJSONString(microWorkSubmitDto));
        // 2. 校验参数
        this.checkSubmitParams(microWorkSubmitDto);
        // 审产数量不再在这里硬拒：统一由 MicroWorkSubmitServiceImpl.editAndCheck 里的 normalizeCheckNums 处理
        // （null/0 → 按报工数回填；报工数也为 0 才拒绝），与批量审产两个入口保持同一口径
        // 3. 校验报工记录是否有标准工艺并且是否符合标准工艺路线
        boolean flag = technologyService.validSubmitRecordTechInfo(microWorkSubmitDto.getProductSeq(),
                microWorkSubmitDto.getOperateProcessSeq(), microWorkSubmitDto.getPreProcessSeq());
        if (!flag) {
            throw new CustomException("报工记录不符合产品的工艺路线, 请编辑报工记录");
        }

        // 4. 实体转换
        MicroWorkSubmit microWorkSubmit = MicroWorkSubmitAssembler.toMicroWorkSubmitDomain(microWorkSubmitDto);
        // 工易派不需要调用该返回值 
        MicroWorkSubmit workSubmit = microWorkSubmitService.editAndCheck(microWorkSubmit);

        // 5.调用工易派的方法, 更新任务单、工单等信息
        if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            List<MicroManufactureTask> microManufactureTasks = new ArrayList<>();
            MicroManufactureTask microManufactureTask = MicroWorkSubmitAssembler.toManufactureTaskDomain(microWorkSubmit);
            microManufactureTasks.add(microManufactureTask);
            microManufactureTaskService.recordChange(true, microManufactureTasks);
        } else {
            microProcessStorageService.changeStorage(workSubmit, CommonConstants.STORAGE_CHANGE_TYPE_SUBMIT);
            publisher.publishEvent(new FlushWarningMetricsEvent((String) ThreadContext.get(Constants.TARGET_CUSTOMER)));
        }
        settlementReportService.generateReportAfterCheck(MicroSettlementAssembler.submitToSettlement(Collections.singletonList(workSubmit)));
        return AjaxResult.success();
    }

    /**
     * 记工驳回接口
     *
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectSubmitRecord(Long[] ids) {
        return microWorkSubmitService.rejectSubmitRecord(ids);
    }

    /**
     * 审核撤销接口
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean undoCheckedRecord(Long id) {
        MicroWorkSubmit microWorkSubmit = microWorkSubmitService.undoCheckedRecord(id, UndoCheckRecordTypeEnum.NORMAL_UNDO.getCode());
        // 调用工易派的方法, 更新任务单、工单等信息
        if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            List<MicroManufactureTask> microManufactureTaskList = new ArrayList<>();
            MicroManufactureTask microManufactureTask = MicroWorkSubmitAssembler.toManufactureTaskDomain(microWorkSubmit);
            microManufactureTaskList.add(microManufactureTask);
            microManufactureTaskService.recordChange(false, microManufactureTaskList);
        } else {
            // ku易记里面更改库存信息
            microProcessStorageService.changeStorage(microWorkSubmit, CommonConstants.STORAGE_CHANGE_TYPE_ELIMINATION);
        }
        settlementReportService.undoSettlementReport(Collections.singletonList(id));
        return true;
    }

    /**
     * 送检接口
     *
     * 1 - 只传递报工记录id，更新送检状态
     * 2 - 生成报工 + 送检
     *
     * @param microWorkSubmitDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submittalForInspection(MicroWorkSubmitDto microWorkSubmitDto) {
        log.info("请求参数为:{}", JSONObject.toJSONString(microWorkSubmitDto));
        Assert.notNull(microWorkSubmitDto,"传递参数不能为空");

        // 1. 实体设置已送检状态
        microWorkSubmitDto.setCheckStatus(CheckStatusEnum.ALREADY_SUBMIT_FOR_INSPECTION.getCode());

        // 2. 判断是那种送检方式
        if (microWorkSubmitDto.getIds() != null) {
            // 转换为ids数组
            Long[] ids = Arrays.stream(microWorkSubmitDto.getIds().split(",")).map(s -> Long.parseLong(s.trim()))
                    .toArray(obj -> new Long[obj]);
            // 审核送检的方式, 改变单据状态
            List<MicroWorkSubmit> submitListTemp = microWorkSubmitMapper.selectMicroWorkSubmitByIds(ids);
            if (CollectionUtils.isEmpty(submitListTemp)) {
                throw new CustomException("未查询到要送检报工记录的信息");
            }
            submitListTemp = this.filterSubmitRecordByStandardTech(submitListTemp);
            if (CollectionUtils.isEmpty(submitListTemp)) {
                return 0;
            }
            List<MicroWorkSubmit> submitForUpdate = new ArrayList<>();
            for (MicroWorkSubmit submitTemp : submitListTemp) {
                if (submitTemp.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode()) || submitTemp.getSubmitStatus().equals(SubmitStatusEnum.REJECT.getCode())) {
                    throw new CustomException("已审核或者驳回的报工记录不能送检");
                }
                // 更新单据状态
                MicroWorkSubmit updateParam = new MicroWorkSubmit();
                updateParam.setId(submitTemp.getId());
                updateParam.setCheckStatus(CheckStatusEnum.ALREADY_SUBMIT_FOR_INSPECTION.getCode());
                updateParam.setLastUpdBy(SecurityUtils.getUserId().toString());
                updateParam.setLastUpdDate(DateUtils.getNowDate());
                // 如果是已质检过的需要清空质检生成的数量
                if (submitTemp.getCheckStatus().equals(CheckStatusEnum.FINISHED_INSPECTION.getCode())) {
                    updateParam.setCheckPassNum(null);
                    updateParam.setCheckNgNum(null);
                    updateParam.setQcDate(null);
                    updateParam.setQcUser(null);
                }
                submitForUpdate.add(updateParam);
            }
            // 批量更新
            return microWorkSubmitMapper.updateSubmitRecordCheckStatusBatch(submitForUpdate);
        } else {
            // 报工 + 送检的方式
            return this.createSubmitRecord(microWorkSubmitDto);
        }
    }

    /**
     * 导出报工记录信息（记工排行页面的导出）
     *
     * @param receivedBy
     */
    @Override
    public String exportEmployeeSubmitRecord(Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:startDate:{}-endDate:{}-receivedBy:{}", startDate, endDate, receivedBy);
        // 校验邮箱格式
        if (!this.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }
        // 1. 构建导出数据
        SubmitRecordQueryParam param = new SubmitRecordQueryParam();
        param.setStartDate(startDate);
        param.setEndDate(endDate);
        param.setSubmitType(supportUtil.getSubmitType());
        // (1). 获取报工记录
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectSubmitRecordByUser(param);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            throw new CustomException("该时间范围内还没有报工记录信息");
        }
        // 有审产人的报工记录则获取审产人的中文名
        microWorkSubmitList.forEach(obj -> {
            if (!org.springframework.util.StringUtils.isEmpty(obj.getCheckUser())) {
                String checkNickName = getNickName(obj.getCheckUser());
                obj.setCheckNickName(checkNickName);
            }
        });
        List<Long> submitIdList = microWorkSubmitList.stream().map(MicroWorkSubmit::getId).collect(Collectors.toList());
        // (2). 获取质检记录信息 -> 质检人
        List<MicroQualityControlRecord> microQualityControlRecords = microQualityControlRecordMapper.selectMicroQualityControlRecordBySubmitIdList(submitIdList);
        // (3). 获取结算报工信息
        List<MicroSettlementReportEntity> microSettlementReportEntityList = microSettlementReportMapper.selectReportListBySubmitIds(submitIdList);

        // 2. 组装返回结果信息实体
        List<MicroWorkSubmitRecordExportDto> microWorkSubmitRecordExportDtoList = MicroWorkSubmitAssembler.toMicroWorkSubmitRecordExportDto(microWorkSubmitList, microQualityControlRecords, microSettlementReportEntityList);

        // 3. 导出并发送邮件
        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.EMPLOYEE_SUBMIT_EXCEL_NAME);
        return microEmailService.exportAndSendEmail(MailConstants.EMPLOYEE_SUBMIT_MAIL_SUBJECT,
                MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                receivedBy,
                microWorkSubmitRecordExportDtoList,
                MicroWorkSubmitRecordExportDto.class,
                placeholderMap);
    }

    private String getNickName(String userId) {
        Object userCache = supportUtil.getUserCache(userId);
        String nickName = "";
        if (userCache != null) {
            nickName = ((MicroUser) userCache).getNickName();
        }
        return nickName;
    }


    /**
     * 工易派 - 工单维度审核列表
     *
     * @param checkParamByWorkOrder
     */
    @Override
    public List<SubmitRecordInfoByWorkOrderDto> selectSubmitRecordInfoByWorkOrder(CheckParamByWorkOrder checkParamByWorkOrder) {

        List<SubmitRecordInfoByWorkOrder> submitRecordInfoByWorkOrders = microWorkSubmitService.selectSubmitRecordInfoByWorkOrder(checkParamByWorkOrder);
        if (CollectionUtils.isEmpty(submitRecordInfoByWorkOrders)) {
            return Collections.emptyList();
        }
        List<String> workOrderNoList = submitRecordInfoByWorkOrders.stream().map(SubmitRecordInfoByWorkOrder::getWorkOrderNo).collect(Collectors.toList());
        List<MicroManufactureWorkOrder> workOrderInfoBySubmit = microManufactureWorkOrderService.getWorkOrderInfoBySubmit(workOrderNoList);
        if (CollectionUtils.isEmpty(workOrderInfoBySubmit)) {
            throw new CustomException("未查询到报工记录对应的工单信息");
        }
        Map<String, List<MicroManufactureWorkOrder>> map = workOrderInfoBySubmit.stream().collect(Collectors.groupingBy(MicroManufactureWorkOrder::getWorkOrderNo));

        List<SubmitRecordInfoByWorkOrderDto> result = new ArrayList<>();
        submitRecordInfoByWorkOrders.forEach(obj -> {
            SubmitRecordInfoByWorkOrderDto temp = new SubmitRecordInfoByWorkOrderDto();
            BeanUtils.copyProperties(obj, temp);
            List<MicroManufactureWorkOrder> microManufactureWorkOrders = map.get(obj.getWorkOrderNo());
            if (CollectionUtils.isEmpty(microManufactureWorkOrders)) {
                throw new CustomException("未查询到报工记录对应的工单信息");
            }
            MicroManufactureWorkOrder microManufactureWorkOrder = microManufactureWorkOrders.get(0);
            temp.setMlineCode(microManufactureWorkOrder.getMlineCode());
            temp.setMlineName(microManufactureWorkOrder.getMlineName());
            temp.setWshopCode(microManufactureWorkOrder.getWshopCode());
            temp.setWshopName(microManufactureWorkOrder.getWshopName());
            temp.setWorkOrderNum(microManufactureWorkOrder.getWorkOrderNum());
            temp.setPassNum(microManufactureWorkOrder.getPassNum());
            result.add(temp);
        });

        return result;
    }

    /**
     * 工易派 - 某个工单下的报工记录详细展示信息
     *
     * @param workOrderNo
     */
    @Override
    public List<DetailSubmitRecordInfoByWorkOrder> selectDetailSubmitRecordInfoByWorkOrderList(String workOrderNo, String submitStatus) {
        List<DetailSubmitRecordInfoByWorkOrder> detailSubmitRecordInfoByWorkOrders = microWorkSubmitService.selectDetailSubmitRecordInfoByWorkOrderList(workOrderNo, submitStatus);
        if (CollectionUtils.isEmpty(detailSubmitRecordInfoByWorkOrders)) {
            return Collections.emptyList();
        }
        // 查询工序任务信息
        MicroManufactureTask queryParam = new MicroManufactureTask();
        queryParam.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        queryParam.setWorkOrderNo(workOrderNo);
        List<MicroManufactureTask> microManufactureTaskList = microManufactureTaskService.selectMicroManufactureTaskList(queryParam);
        if (CollectionUtil.isEmpty(microManufactureTaskList)) {
            return detailSubmitRecordInfoByWorkOrders;
        } else {
            // 按照sort排序
            microManufactureTaskList = microManufactureTaskList.stream()
                    .sorted(Comparator.comparing(MicroManufactureTask::getSort))
                    .collect(Collectors.toList());
            // 存储排好序之后的信息
            List<DetailSubmitRecordInfoByWorkOrder> cacheChain = new ArrayList<>();
            // 按照工序任务的下发顺序进行展示
            for (MicroManufactureTask microManufactureTask : microManufactureTaskList) {
                List<DetailSubmitRecordInfoByWorkOrder> tempList = detailSubmitRecordInfoByWorkOrders.stream()
                        .filter(obj -> obj.getOperateProcessSeq().equals(microManufactureTask.getProcessSeq()))
                        .collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(tempList)) {
                    DetailSubmitRecordInfoByWorkOrder temp = tempList.get(0);
                    // 从工序任务获取尾序信息
                    temp.setIsFirstProcess(microManufactureTask.getIsFirstProcess());
                    cacheChain.add(temp);
                }
            }
            return cacheChain;
        }
    }

    /**
     * 工易派 - 获取某个工单某个工序下面的到人的报工记录信息
     *
     * @param userSubmitRecordQueryParam
     */
    @Override
    public List<UserDetailSubmitRecordInfoByWorkOder> selectUserDetailSubmitRecordInfoByWorkOder(UserSubmitRecordQueryParam userSubmitRecordQueryParam) {
        return microWorkSubmitService.selectUserDetailSubmitRecordInfoByWorkOder(userSubmitRecordQueryParam);
    }

    /**
     * 产品维度下 审核列表
     *
     * @param checkByProductParam
     */
    @Override
    public List<SubmitRecordGroupByProduct> selectSubmitRecordGroupByProductList(CheckByProductParam checkByProductParam) {
        log.info("查询报工记录分组列表, productSeq:{}, submitStatus:{}", checkByProductParam.getProductSeq(), checkByProductParam.getSubmitStatus());
        List<SubmitRecordGroupByProduct> submitRecordGroupByProducts = microWorkSubmitService.selectSubmitRecordGroupByProductList(checkByProductParam);
        if (CollectionUtils.isEmpty(submitRecordGroupByProducts)) {
            return Collections.emptyList();
        }

        // 如果产品有标准工艺则展示总的工序数量
        List<Long> productIds = submitRecordGroupByProducts.stream().map(SubmitRecordGroupByProduct::getProductId).collect(Collectors.toList());
        List<MicroProcessChainBindEntity> bindEntityList = technologyService.selectMicroTechChainByProductIdsOrSeqs(productIds, Collections.emptyList(), BomAndTechTypeEnum.STANDARD.getCode());
        Map<Long, List<MicroProcessChainBindEntity>> chainMap = new HashMap<>(16);
        if (!CollectionUtils.isEmpty(bindEntityList)) {
            chainMap = bindEntityList.stream().collect(Collectors.groupingBy(MicroProcessChainBindEntity::getProductId, HashMap::new, Collectors.toList()));
        }
        for (SubmitRecordGroupByProduct submitRecordGroupByProduct : submitRecordGroupByProducts) {
            long processCount;
            if (submitRecordGroupByProduct.isStandard()) {
                List<MicroProcessChainBindEntity> chain = chainMap.get(submitRecordGroupByProduct.getProductId());
                processCount = chain.stream().map(MicroProcessChainBindEntity::getProcessSeq).distinct().count();
            } else {
                processCount = Arrays.stream(submitRecordGroupByProduct.getProcessSeq().split(",")).distinct().count();
            }
            submitRecordGroupByProduct.setProcessCount((int) processCount);
        }
        return submitRecordGroupByProducts;
    }

    /**
     * 产品维度下 详细的报工记录信息展示
     *
     * @param checkByProductParam
     */
    @Override
    public DetailCheckSubmitRecordByProductRes showDetailSubmitRecordByProduct(CheckByProductParam checkByProductParam) {
        log.info("查询报工记录详情, productSeq:{}, submitStatus:{}", checkByProductParam.getProductSeq(), checkByProductParam.getSubmitStatus());
        if (checkByProductParam.getProductSeq() == null || checkByProductParam.getSubmitStatus() == null) {
            throw new CustomException("请求参数中产品序列码和报工状态不能为空");
        }
        // 1. 根据该产品下面的报工记录id获取详细的报工记录信息
        SubmitRecordQueryParam queryParam = new SubmitRecordQueryParam();
        BeanUtils.copyProperties(checkByProductParam, queryParam);
        queryParam.setSubmitType(supportUtil.getSubmitType());
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectSubmitRecordByUser(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmitList)) {
            return new DetailCheckSubmitRecordByProductRes();
        }
        // 2. 获取该产品的工艺链路
        Optional<Long> productIdOptional = microWorkSubmitList.stream().map(MicroWorkSubmit::getProductId).findFirst();
        if (!productIdOptional.isPresent()) {
            throw new CustomException("不存在产品id信息");
        }
        Long productId = productIdOptional.get();
        String productSeq = checkByProductParam.getProductSeq();
        List<MicroProcessChainBindEntity> bindEntityList = technologyService.selectMicroTechChain(MicroTechnologyAssembler.toTechQuery(ThreadContext.get(Constants.TARGET_CUSTOMER).toString(), productId, productSeq));
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = BeanUtil.copyToList(microWorkSubmitList, MicroWorkSubmitDto.class);
        return microWorkSubmitService.showDetailSubmitRecordByProduct(productSeq, microWorkSubmitDtoList, bindEntityList);
    }

    /**
     * 根据产品 + 工序 获取首尾序的标示
     *
     * @param standard
     */
    @Override
    public FirstOrLastProcess getFirstOrLastProcessFlag(String productSeq, String processSeq, boolean standard) {
        if (standard) {
            return technologyService.getFirstOrLastProcessFlagByCraftTech(productSeq, processSeq);
        } else {
            return microWorkSubmitService.getFirstOrLastProcessFlagByRecords(productSeq, processSeq);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int multiMixed(BatchSubmitOrQcDto batchSubmitOrQcDto) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(batchSubmitOrQcDto));
        this.validBatchSubmitOrQcDto(batchSubmitOrQcDto);
        String appSign = SecurityUtils.getApplicationSign();
        if (appSign.equals(ApplicationTypeEnum.KU_YI_JI.getCode())) {
            // 2.报工domain实体
            List<MicroWorkSubmit> microWorkSubmitList = MicroWorkSubmitAssembler.multiMixedToMicroWorkSubmitDomain(batchSubmitOrQcDto.getMultiMixedList());
            if (CollectionUtils.isEmpty(microWorkSubmitList)) {
                throw new CustomException("无法获取记工数据");
            }
            //这里暂时循环调用, 后面报工接口需要重构,支持批量报工
            for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
                SubmitStrategy submitStrategy = SubmitStrategyFactory.getSubmitStrategy(appSign);
                MicroWorkSubmit submit = (MicroWorkSubmit) submitStrategy.submit(microWorkSubmit);
                // 如果是批量送检，则在报工的基础上再加上送检，送检由状态位决定
                if (batchSubmitOrQcDto.getSubmitWay().equals(SubmitWayEnum.BATCH_SUBMIT_AND_SEND_FOR_QC.getCode())) {
                    submit.setCheckStatus(CheckStatusEnum.ALREADY_SUBMIT_FOR_INSPECTION.getCode());
                }
                microWorkSubmitService.insertMicroWorkSubmit(submit);
            }
        }
        return 1;
    }

    /**
     * 获取不同状态下的报工记录条数
     *
     * @param microWorkSubmit
     */
    @Override
    public Map<String, Integer> obtainedSubmitRecordNumInDifferentStatus(MicroWorkSubmit microWorkSubmit) {
        return microWorkSubmitService.obtainedSubmitRecordNumInDifferentStatus(microWorkSubmit);
    }

    /**
     * 计算报工的数量 （已审核、未审核）
     *
     * @param processSeq
     */
    @Override
    public TotalSubmitNumDTO obtainedSubmitNum(String workOrderNo, String processSeq) {
        // 构建查询参数
        MicroWorkSubmit queryParam = new MicroWorkSubmit();
        queryParam.setWorkOrderNo(workOrderNo);
        queryParam.setOperateProcessSeq(processSeq);
        Long submitType = supportUtil.getSubmitType();
        queryParam.setSubmitType(submitType);

        // 根据报工记录获取报工数量
        TotalSubmitNumDTO result = microWorkSubmitService.obtainedSubmitNum(queryParam);

        // 如果是工易派的话还要获取一下任务计划数量
        if (SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode().equals(submitType)) {
            MicroManufactureTask param = new MicroManufactureTask();
            param.setWorkOrderNo(workOrderNo);
            param.setProcessSeq(processSeq);
            List<MicroManufactureTask> microManufactureTaskList = microManufactureTaskMapper.selectMicroManufactureTaskList(param);
            if (CollectionUtil.isNotEmpty(microManufactureTaskList)) {
                BigDecimal taskPlanNum = microManufactureTaskList.get(0).getTaskPlanNum();
                result.setTaskPlanNum(taskPlanNum);
            }
        }
        return result;
    }

    /**
     * 获取到人的不同状态报工记录汇总信息
     *
     * @param submitStatus
     */
    @Override
    public List<SubmitInfoInDifferentStatusByUser> getSubmitInfoInDifferentStatusByUser(Date startDate, Date endDate, Long submitStatus) {
        List<SubmitInfoInDifferentStatusByUser> result = new ArrayList<>();
        List<MicroWorkSubmit> submitInfoInDifferentStatusByUser = microWorkSubmitService.getSubmitInfoInDifferentStatusByUser(startDate, endDate, submitStatus);
        if (CollectionUtil.isNotEmpty(submitInfoInDifferentStatusByUser)) {
            result = MicroWorkSubmitAssembler.microWorkSubmitDomain2SubmitInfoInDifferentStatusByUser(submitInfoInDifferentStatusByUser);
        }
        return result;
    }

    /**
     * 排序
     *
     */
    public void sortByStandardTech(List<MicroProcessChainBindEntity> currentNodeList,
                                   List<DetailSubmitRecordInfoByWorkOrder> detailSubmitRecordInfoByWorkOrderList,
                                   List<MicroProcessChainBindEntity> chainBindEntityList,
                                   List<DetailSubmitRecordInfoByWorkOrder> cacheChain) {
        for (MicroProcessChainBindEntity currentNode : currentNodeList) {
            detailSubmitRecordInfoByWorkOrderList.forEach(obj -> {
                if (currentNode.getProcessSeq().contains(obj.getOperateProcessSeq())) {
                    obj.setIsLastProcess(currentNode.getIsLastProcess());
                    // 首序
                    if (!StringUtils.isEmpty(currentNode.getParentProcessSeq()) && CommonConstants.ROOT_PROCESS_SEQ.equals(currentNode.getParentProcessSeq())) {
                        obj.setIsFirstProcess(CommonConstants.YES);
                    } else {
                        obj.setIsFirstProcess(CommonConstants.NO);
                    }
                    cacheChain.add(obj);
                }
            });
        }

        // 找到当前节点的上一层节点
        List<MicroProcessChainBindEntity> nextNodeList = new ArrayList<>();
        for (MicroProcessChainBindEntity microProcessChainBind : chainBindEntityList) {
            for (MicroProcessChainBindEntity currentNode : currentNodeList) {
                if (!CommonConstants.ROOT_PROCESS_SEQ.equals(currentNode.getParentProcessSeq()) && currentNode.getParentProcessSeq().contains(microProcessChainBind.getProcessSeq())) {
                    nextNodeList.add(microProcessChainBind);
                }
            }
        }

        if (!CollectionUtils.isEmpty(nextNodeList)) {
            // 并序情况，nextNodeList去重，如果有工序相同的则合并前工序为一个字段
            nextNodeList = TechChainUtils.mergeNextNodeList(nextNodeList);
            sortByStandardTech(nextNodeList, detailSubmitRecordInfoByWorkOrderList, chainBindEntityList,cacheChain);
        }
    }

    public void validBatchSubmitOrQcDto(BatchSubmitOrQcDto batchSubmitOrQcDto) {
        Assert.notNull(batchSubmitOrQcDto, "请求的参数不能空");
        Assert.notNull(batchSubmitOrQcDto.getSubmitWay(), "批量操作的方式不能为空");
        Assert.notNull(batchSubmitOrQcDto.getMultiMixedList(), "报工记录参数不能为空");
    }

    /**
     * 校验报工记录入参
     *
     */
    private void checkSubmitParams(MicroWorkSubmitDto microWorkSubmitDto) {
        if (CheckObjectUtils.isEmpty(microWorkSubmitDto)) {
            throw new CustomException("报工参数不能为空");
        }
        if (CheckObjectUtils.isEmpty(microWorkSubmitDto.getId())) {
            throw new CustomException("id不能为空");
        }
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(microWorkSubmitDto.getProductName(), microWorkSubmitDto.getOperateProcessName())) {
            throw new CustomException("产品名称和当前工序名称不能为空");
        }
        if (StringUtils.hasText(microWorkSubmitDto.getPreProcessName()) && microWorkSubmitDto.getOperateProcessName().equals(microWorkSubmitDto.getPreProcessName())) {
            throw new CustomException("前工序和当前工序的名称不能相同");
        }
        if (microWorkSubmitDto.getPassNum().signum() == 0 && microWorkSubmitDto.getNgNum().signum() == 0) {
            throw new CustomException("良品和不良品的报工数量不能同时为0");
        }
    }

    /**
     * 校验报工记录的异常情况
     *
     * 1. 是否被审核
     * 2. 有标准工艺路线，判断是否符合标准工艺路线
     *
     */
    public List<MicroWorkSubmit> validAndFilterSubmitRecord(List<MicroWorkSubmit> microWorkSubmitList) {
        if (CheckObjectUtils.isEmpty(microWorkSubmitList)) {
            throw new CustomException("未查询到相关报工记录信息");
        }
        microWorkSubmitList.forEach(obj -> {
            if (obj.getSubmitStatus().equals(SubmitStatusEnum.APPROVED.getCode())) {
                throw new CustomException("报工记录:" + obj.getId().toString() + "已被审核");
            }
        });
        // 工易派不需要考虑标准工艺的问题
        Long submitType = supportUtil.getSubmitType();
        if (submitType.equals(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode())) {
            return this.filterSubmitRecordByStandardTech(microWorkSubmitList);
        }
        return microWorkSubmitList;
    }

    /**
     * 过滤不符合标准工艺路线的报工记录
     *
     */
    public List<MicroWorkSubmit> filterSubmitRecordByStandardTech(List<MicroWorkSubmit> microWorkSubmitList) {
        List<Long> exceptionIds = new ArrayList<>();
        // 员工列表可以批量送检, 需要判断下是否有记录不符合标准工艺路线
        for (MicroWorkSubmit microWorkSubmit : microWorkSubmitList) {
            boolean flag = technologyService.validSubmitRecordTechInfo(microWorkSubmit.getProductSeq(), microWorkSubmit.getOperateProcessSeq(), microWorkSubmit.getPreProcessSeq());
            if (!flag) {
                exceptionIds.add(microWorkSubmit.getId());
            }
        }
        if (exceptionIds.size() > 0) {
            log.info("不符合标准工艺的报工记录ids为:{}", JSONObject.toJSONString(exceptionIds));
            microWorkSubmitList = microWorkSubmitList.stream().filter(obj -> !exceptionIds.contains(obj.getId())).collect(Collectors.toList());
        }
        return microWorkSubmitList;
    }

    /**
     * 邮件格式验证
     *
     */
    public boolean isValidEmail(String receivedBy) {
        if (com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(receivedBy)) {
            return Pattern.matches("^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$", receivedBy);
        }
        return false;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.page.ListPagePower;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.web.page.PageDomain;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroStaffCalendarService;
import com.cosmo.hhim.micro.infrastructure.enums.StaffCalendarStatisticTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroDateUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroStaffCalendarServiceImpl extends ListPagePower implements IMicroStaffCalendarService {

    private final MicroWorkSubmitMapper microWorkSubmitMapper;
    private final MicroProcessCommonMapper microProcessCommonMapper;
    private final MicroProductMapper microProductMapper;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 查询员工记工统计信息
     */
    @Override
    public StaffWorkStatisticResult queryStaffWorkStatistic(StaffWorkStatisticParam param) {
        StaffWorkStatisticResult result = new StaffWorkStatisticResult();
        String userId = SecurityUtils.getUserId().toString();

        // 1.查询报工记录
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper
                .selectWorkSubmitInfosBySubmitUserAndDay(userId, param.getQueryStartDate(), param.getQueryEndDate(), param.getSubmitType());
        if (CollectionUtils.isEmpty(microWorkSubmits)) {
            return result;
        }

        // 2.统计指标
        // 2.1 产品总数
        if (param.getSubmitType().equals(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode())) {
            // Ku易记 - 设置产品数
            result.setProductSum(microWorkSubmits.stream().map(MicroWorkSubmit::getProductSeq).distinct().count());
        } else {
            // 工易派 - 设置工单数
            result.setWorkOrderNum(microWorkSubmits.stream().map(MicroWorkSubmit::getWorkOrderNo).distinct().count());
        }

        // 2.2 待审核总数
        BigDecimal totalWaitCheckNum = this.calcWaitCheckNum(microWorkSubmits);
        result.setWaitCheckSum(totalWaitCheckNum);

        // 2.3 驳回待处理总数
        BigDecimal totalRejectCheckNum = this.calcRejectCheckNum(microWorkSubmits);
        Long rejectRecordNum = this.calcRejectRecordNum(microWorkSubmits);
        result.setRejectSum(totalRejectCheckNum);
        result.setRejectRecordNum(rejectRecordNum);

        // 2.4 记工总数
        BigDecimal totalFinishCheckNum = this.calcFinishCheckNum(microWorkSubmits);
        BigDecimal workSubmitSum = totalWaitCheckNum.add(totalFinishCheckNum).add(totalRejectCheckNum);
        result.setWorkSubmitSum(workSubmitSum);

        // 2.5 出勤天数
        result.setWorkDays(calcWorkDays(microWorkSubmits));

        long finishCheckWorkDays = calcFinishCheckWorkDays(microWorkSubmits);
        // 针对平均日产能、平均良品率以今日之前的历史数据进行计算需求特殊处理
        Date nowBeginDate = DateUtil.beginOfDay(DateUtils.getNowDate()).toJdkDate();
        if (!param.getQueryEndDate().before(nowBeginDate)) { // 查询结束时间大于等于当天的起始时间则重新计算日产能、良品率相关计算节点 
            microWorkSubmits = microWorkSubmitMapper
                    .selectWorkSubmitInfosBySubmitUserAndDay(userId, param.getQueryStartDate(), nowBeginDate, param.getSubmitType());
            if (CollectionUtils.isEmpty(microWorkSubmits)) {
                return result;
            }

            // 出勤天数
            finishCheckWorkDays = calcFinishCheckWorkDays(microWorkSubmits);

            // 记工总数
            workSubmitSum = calcWaitCheckNum(microWorkSubmits).add(calcFinishCheckNum(microWorkSubmits)).add(calcRejectCheckNum(microWorkSubmits));

            // 已审核总数
            totalFinishCheckNum = this.calcFinishCheckNum(microWorkSubmits);
        }

        // 2.6 平均日产能（已审核总数 / 出勤天数）
        if (totalFinishCheckNum.signum() == 0) {
            result.setAverageCapacityOfDay(BigDecimal.ZERO);
        } else {
            result.setAverageCapacityOfDay(totalFinishCheckNum.divide(new BigDecimal(finishCheckWorkDays), 2, RoundingMode.HALF_UP));
        }

        // 2.7 平均良品率（每日良品率之和(良品率：已审核良品数/已审核总数) / 出勤天数）
        BigDecimal totalGoodProductRate = this.calcTotalGoodProductRate(microWorkSubmits);
        if (totalGoodProductRate.signum() == 0) {
            result.setAverageGoodProductRatioOfDay(BigDecimal.ZERO);
        } else {
            BigDecimal averageGoodProductRatioOfDay = totalGoodProductRate
                    .divide(new BigDecimal(finishCheckWorkDays), 4, RoundingMode.HALF_UP);
            result.setAverageGoodProductRatioOfDay(averageGoodProductRatioOfDay);
        }

        return result;
    }

    /**
     * 查询员工记工日历统计明细
     */
    @Override
    public List<StaffWorkCalendarDetailResult> queryStaffCalendarDetail(StaffWorkCalendarDetailParam param) {
        List<StaffWorkCalendarDetailResult> resultList = Lists.newArrayList();
        String userId = SecurityUtils.getUserId().toString();

        // 1.填充默认初始化数据
        Map<String, StaffWorkCalendarDetailResult> defaultMap = Maps.newHashMap();
        List<DateTime> dateTimes = DateUtil.rangeToList(param.getQueryStartDate(), param.getQueryEndDate(), DateField.DAY_OF_MONTH);
        List<String> betweenDateAllDates = dateTimes.stream().map(e -> e.toString(DATE_FORMAT)).collect(Collectors.toList());
        for (String date : betweenDateAllDates) {
            defaultMap.put(date, new StaffWorkCalendarDetailResult());
        }

        // 2.查询报工记录
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectWorkSubmitInfosBySubmitUserAndDay(userId, param.getQueryStartDate(), param.getQueryEndDate(), param.getSubmitType());
        if (!CollectionUtils.isEmpty(microWorkSubmits)) {
            // 2.1 按天分组统计记工总数
            Map<String, BigDecimal> dayGroupMap = statisticWorkNumGroupByDay(param.getStatisticType(), microWorkSubmits);

            // 2.2 填充默认的初始化数据
            if (!CollectionUtils.isEmpty(dayGroupMap)) {
                for (Iterator<Map.Entry<String, BigDecimal>> iterator = dayGroupMap.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<String, BigDecimal> entry = iterator.next();
                    String day = entry.getKey();
                    BigDecimal value = entry.getValue();

                    StaffWorkCalendarDetailResult staffWorkCalendarDetailResult = defaultMap.get(day);
                    staffWorkCalendarDetailResult.setDay(day);
                    staffWorkCalendarDetailResult.setStatisticNumber(value);
                    defaultMap.put(day, staffWorkCalendarDetailResult);
                }
            }

        }

        // 3.组装返回对象给到前端，方便前端渲染
        for (Iterator<Map.Entry<String, StaffWorkCalendarDetailResult>> iterator = defaultMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, StaffWorkCalendarDetailResult> entry = iterator.next();
            String dayPoint = entry.getKey();
            StaffWorkCalendarDetailResult result = entry.getValue();

            result.setDay(dayPoint);
            resultList.add(result);
        }

        // 按日期升序
        resultList = resultList.stream().sorted(Comparator.comparing(StaffWorkCalendarDetailResult::getDay)).collect(Collectors.toList());

        return resultList;
    }


    /**
     * 按照产品种类分组查询员工工序明细信息
     */
    @Override
    public List<StaffProcessSeqInfoForProduct> queryProcessSeqDetailForProduct(PageDomain pageDomain, StaffWorkStatisticParam param) {
        List<StaffProcessSeqInfoForProduct> resultList = Lists.newArrayList();

        String userId = SecurityUtils.getUserId().toString();

        // 1.查询报工记录
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper
                .selectWorkSubmitInfosBySubmitUserAndDay(userId, param.getQueryStartDate(), param.getQueryEndDate(), param.getSubmitType());
        if (CollectionUtils.isEmpty(microWorkSubmits)) {
            return resultList;
        }

        // 按照产品编码分组统计记工审核数
        Map<String, BigDecimal> collectProductGroup = microWorkSubmits.stream().map(e -> {
            if (SubmitStatusEnum.APPROVED == SubmitStatusEnum.getEnum(e.getSubmitStatus())) { // 审核通过
                e.setTotalNum(e.getCheckNgNum().add(e.getCheckPassNum()));
            } else if (SubmitStatusEnum.UN_APPROVE == SubmitStatusEnum.getEnum(e.getSubmitStatus())
                    || SubmitStatusEnum.REJECT == SubmitStatusEnum.getEnum(e.getSubmitStatus())) { // 待审核或审核驳回
                e.setTotalNum(e.getPassNum().add(e.getNgNum()));
            }
            return e;
        }).collect(Collectors.groupingBy(MicroWorkSubmit::getProductSeq, Collectors.reducing(BigDecimal.ZERO, MicroWorkSubmit::getTotalNum, BigDecimal::add)));

        for (String productSeq : collectProductGroup.keySet()) {
            StaffProcessSeqInfoForProduct result = new StaffProcessSeqInfoForProduct();
            result.setProductSeq(productSeq);
            result.setProductTotalNum(collectProductGroup.get(productSeq));

            // 查询产品基本信息
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(productSeq);
            if (null != microProduct) {
                result.setProductCode(microProduct.getProductCode());
                result.setProductName(microProduct.getProductName());
            }

            // 查询指定产品按照工序分组统计的记工良品数和不良品数
            List<StaffProcessSeqInfoForProduct.ProcessSeqInfo> processSeqInfoList = Lists.newArrayList();
            Map<String, List<MicroWorkSubmit>> collectProcessGroup = microWorkSubmits.stream().filter(e -> e.getProductSeq().equals(productSeq)).map(e -> {
                if (SubmitStatusEnum.APPROVED == SubmitStatusEnum.getEnum(e.getSubmitStatus())) { // 审核通过
                    e.setPassNum(e.getCheckPassNum());
                    e.setNgNum(e.getCheckNgNum());
                }
                return e;
            }).collect(Collectors.groupingBy(MicroWorkSubmit::getOperateProcessSeq));

            for (String processSeq : collectProcessGroup.keySet()) {
                List<MicroWorkSubmit> workSubmits = collectProcessGroup.get(processSeq);

                StaffProcessSeqInfoForProduct.ProcessSeqInfo processSeqInfo = new StaffProcessSeqInfoForProduct.ProcessSeqInfo();
                processSeqInfo.setProcessSeq(processSeq);

                // 查询工序基本信息
                MicroProcessCommon microProcessCommon = microProcessCommonMapper.selectMicroProcessCommonByProcessSeq(processSeq);
                if (null != microProcessCommon) {
                    processSeqInfo.setProcessCode(microProcessCommon.getProcessCode());
                    processSeqInfo.setProcessName(microProcessCommon.getProcessName());
                }

                processSeqInfo.setTotalPassProductNum(workSubmits.stream().map(MicroWorkSubmit::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add));
                processSeqInfo.setTotalNgProductNum(workSubmits.stream().map(MicroWorkSubmit::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add));
                processSeqInfoList.add(processSeqInfo);
            }

            result.setProcessSeqInfoList(processSeqInfoList);

            resultList.add(result);
        }


        return doPageList(pageDomain, resultList);
    }

    /**
     * 按天分组统计记工总数
     */
    private Map<String, BigDecimal> statisticWorkNumGroupByDay(String statisticType, List<MicroWorkSubmit> microWorkSubmits) {
        Map<String, BigDecimal> dayGroupMap = Maps.newHashMap();
        switch (StaffCalendarStatisticTypeEnum.getEnum(statisticType)) {
            case WORK_STATISTIC: // 记工统计 
                dayGroupMap = microWorkSubmits.stream().map(e -> {
                    StaffWorkCalendarDetailResult result = new StaffWorkCalendarDetailResult();
                    result.setDay(MicroDateUtils.dateToFormatStr(e.getSubmitDay(), MicroDateUtils.TIME_FORMAT_A));
                    if (e.getSubmitStatus() == SubmitStatusEnum.UN_APPROVE.getCode().longValue()
                            || e.getSubmitStatus() == SubmitStatusEnum.REJECT.getCode().longValue()) { // 未审核 或 审核驳回
                        result.setStatisticNumber(e.getPassNum().add(e.getNgNum()));
                    } else if (e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue()) { // 已审核
                        result.setStatisticNumber(e.getCheckPassNum().add(e.getCheckNgNum()));
                    }
                    return result;
                }).collect(Collectors.groupingBy(StaffWorkCalendarDetailResult::getDay,
                        Collectors.reducing(BigDecimal.ZERO, StaffWorkCalendarDetailResult::getStatisticNumber, BigDecimal::add)));
                break;

            case WAIT_CHECK_STATISTIC: // 待审统计 
                dayGroupMap = microWorkSubmits.stream()
                        .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.UN_APPROVE.getCode().longValue())
                        .map(e -> {
                            StaffWorkCalendarDetailResult result = new StaffWorkCalendarDetailResult();
                            result.setDay(MicroDateUtils.dateToFormatStr(e.getSubmitDay(), MicroDateUtils.TIME_FORMAT_A));
                            result.setStatisticNumber(e.getPassNum().add(e.getNgNum()));
                            return result;
                        }).collect(Collectors.groupingBy(StaffWorkCalendarDetailResult::getDay,
                                Collectors.reducing(BigDecimal.ZERO, StaffWorkCalendarDetailResult::getStatisticNumber, BigDecimal::add)));
                break;

            default:
                break;
        }
        return dayGroupMap;
    }

    /**
     * 计算出勤天数
     */
    private Long calcWorkDays(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream().map(MicroWorkSubmit::getSubmitDay).distinct().count();
    }

    /**
     * 计算已审核记工的出勤天数
     */
    private Long calcFinishCheckWorkDays(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream()
                .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue())
                .map(MicroWorkSubmit::getSubmitDay)
                .distinct()
                .count();
    }

    /**
     * 计算待审核总数
     */
    private BigDecimal calcWaitCheckNum(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream()
                .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.UN_APPROVE.getCode().longValue())
                .map(e -> e.getNgNum().add(e.getPassNum()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算审核驳回总数
     */
    private BigDecimal calcRejectCheckNum(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream()
                .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.REJECT.getCode().longValue())
                .map(e -> e.getNgNum().add(e.getPassNum()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算驳回的报工记录条数
     */
    private Long calcRejectRecordNum(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream()
                .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.REJECT.getCode().longValue())
                .count();
    }

    /**
     * 计算已审核总数
     */
    private BigDecimal calcFinishCheckNum(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream()
                .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue())
                .map(e -> e.getCheckNgNum().add(e.getCheckPassNum()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算良品总数
     */
    private BigDecimal calcGoodProductNum(List<MicroWorkSubmit> microWorkSubmits) {
        return microWorkSubmits.stream().map(e -> {
            if (e.getSubmitStatus() == SubmitStatusEnum.UN_APPROVE.getCode().longValue()) {
                return e.getPassNum();
            } else if (e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue()) {
                return e.getCheckPassNum();
            }
            return BigDecimal.ZERO;
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算已审核总良品率
     */
    private BigDecimal calcTotalGoodProductRate(List<MicroWorkSubmit> microWorkSubmits) {
        Map<Date, List<MicroWorkSubmit>> groupByDayCollect = microWorkSubmits.stream().collect(Collectors.groupingBy(MicroWorkSubmit::getSubmitDay));
        return groupByDayCollect.values().stream().map(e -> {
            // 已审核报工记录
            List<MicroWorkSubmit> approvedWorkSubmitList = e.stream().filter(workSubmit -> workSubmit.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(approvedWorkSubmitList)) {
                return BigDecimal.ZERO;
            }

            // 每天的已审核良品总数
            BigDecimal goodProductNumForDay = approvedWorkSubmitList.stream()
                    .map(MicroWorkSubmit::getCheckPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 每天的已审核总数
            BigDecimal finishCheckNumForDay = approvedWorkSubmitList.stream()
                    .map(workSubmit -> workSubmit.getCheckPassNum().add(workSubmit.getCheckNgNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 每天的良品率：该天无质检数（合计=0）时算不出比率 → 记 0（原实现 divide(0) → 员工产能接口 500）
            if (finishCheckNumForDay.signum() == 0) {
                return BigDecimal.ZERO;
            }
            return goodProductNumForDay.divide(finishCheckNumForDay, 4, RoundingMode.HALF_UP);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

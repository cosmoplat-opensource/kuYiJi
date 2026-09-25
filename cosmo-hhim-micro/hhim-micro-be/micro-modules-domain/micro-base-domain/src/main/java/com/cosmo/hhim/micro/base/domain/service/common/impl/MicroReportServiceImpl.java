/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.DailyReportContentEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.common.WeekReportContentEntity;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroReportService;
import com.cosmo.hhim.micro.infrastructure.enums.IsCompleteEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroReportServiceImpl implements IMicroReportService {

    private static final long DELAY_DAYS = 2L;

    private final MicroWorkSubmitMapper microWorkSubmitMapper;
    private final MicroUserMapper microUserMapper;
    private final MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;

    /**
     * 统计生产日报
     *
     * @param reportDate
     */
    @Override
    public DailyReportContentEntity statisticsTodayReport(Date reportDate) {

        Date beginDate = DateUtil.beginOfDay(reportDate).toJdkDate();
        Date endDate = DateUtil.endOfDay(reportDate).toJdkDate();

        long productTotalNum = 0L;
        long workerNum = 0L;
        BigDecimal rejectProductNum = BigDecimal.ZERO;
        BigDecimal checkProgress = BigDecimal.ZERO;
        BigDecimal inProductNum = BigDecimal.ZERO;

        // 查询报工信息表
        // 获取应用标示，确定报工方式
        Long submitType = SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode();
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            submitType = SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode();
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            submitType = SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode();
        }
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper
                .selectWorkSubmitInfosBySubmitUserAndDay(null, beginDate, endDate, submitType);
        if (!CollectionUtils.isEmpty(microWorkSubmits)) {
            // 产品总数
            productTotalNum = microWorkSubmits.stream().map(MicroWorkSubmit::getProductSeq).distinct().count();

            // 记工人数
            workerNum = microWorkSubmits.stream().map(MicroWorkSubmit::getSubmitUser).distinct().count();

            // 不良品数
            rejectProductNum = microWorkSubmits.stream()
                    .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue())
                    .map(MicroWorkSubmit::getCheckNgNum)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 审核进度
            BigDecimal checkFinishTotalNum = microWorkSubmits.stream()
                    .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue())
                    .map(e -> e.getCheckPassNum().add(e.getCheckNgNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalNum = microWorkSubmits.stream().map(e -> {
                if (e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue()) {
                    return e.getCheckPassNum().add(e.getCheckNgNum());
                } else {
                    return e.getPassNum().add(e.getNgNum());
                }
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 无报工（totalNum=0）时算不出审产进度：置 0（原实现直接 divide(0) → 日报接口 500，而"今天没报工"是常态）
            checkProgress = totalNum.signum() == 0 ? BigDecimal.ZERO
                    : checkFinishTotalNum.divide(totalNum, 3, RoundingMode.DOWN).multiply(new BigDecimal(100));

            // 在产数量（今日所有已审核报工非尾序产品的不良品+良品数之和）
            inProductNum = microWorkSubmits.stream()
                    .filter(e -> e.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue() && "1".equals(e.getIsLastProcess()))
                    .map(e -> e.getCheckPassNum().add(e.getCheckNgNum()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        DailyReportContentEntity contentEntity = new DailyReportContentEntity();
        // 生成日报内容

        contentEntity.setProductTotalNum(productTotalNum);
        contentEntity.setWorkerNum(workerNum);
        contentEntity.setRejectProductNum(rejectProductNum);
        contentEntity.setCheckProgress(checkProgress);
        contentEntity.setInProductNum(inProductNum);
        return contentEntity;
    }

    /**
     * 统计生产周报
     *
     * @param reportDate
     * @return
     */
    @Override
    public WeekReportContentEntity statisticsWeekReport(Date reportDate) { 

        Date date = com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(reportDate, -1L, ChronoUnit.DAYS);
        Date startDate = DateUtil.beginOfWeek(date).toJdkDate();
        Date endDate = DateUtil.endOfWeek(date).toJdkDate();

        WeekReportContentEntity result = new WeekReportContentEntity();

        long productModelNum = 0L;
        long submitUserNum = 0L;
        BigDecimal submitTotalNum = BigDecimal.ZERO;
        BigDecimal waitCheckTotalNum = BigDecimal.ZERO;
        BigDecimal timelyRatio = BigDecimal.ZERO;
        BigDecimal completeWaitNum = BigDecimal.ZERO;
        BigDecimal completeFinishedNum = BigDecimal.ZERO;
        BigDecimal goodRatio = BigDecimal.ZERO;

        // 获取应用标示，确定报工方式
        Long submitType = SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode();
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            submitType = SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode();
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            submitType = SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode();
        }
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper
                .selectWorkSubmitInfosBySubmitUserAndDay(null, startDate, endDate, submitType);

        if (!CollectionUtils.isEmpty(microWorkSubmits)) {

            // 产品款数
            productModelNum = microWorkSubmits.stream().map(MicroWorkSubmit::getProductSeq).distinct().count();

            // 记工人数（排除管理员）
            // 查询管理员userId
            List<MicroUser> microUsers = microUserMapper.selectMicroUserInfoByRoleCode(RoleCodeEnum.MANAGER.getCode());
            if (!CollectionUtils.isEmpty(microUsers)) {
                List<Long> managerUserIds = microUsers.stream().map(MicroUser::getId).distinct().collect(Collectors.toList());

                submitUserNum = microWorkSubmits.stream().map(MicroWorkSubmit::getSubmitUser)
                        .filter(e -> Long.parseLong(e) != managerUserIds.get(0)).distinct().count();
            } else {
                submitUserNum = microWorkSubmits.stream().map(MicroWorkSubmit::getSubmitUser).distinct().count();
            }

            // 记工总数
            submitTotalNum = microWorkSubmits.stream().filter(e -> !SubmitStatusEnum.REJECT.getCode().equals(e.getSubmitStatus())).map(e -> {
                BigDecimal submitNum = BigDecimal.ZERO;
                if (SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.APPROVED
                        || SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.COMPLETE) { // 已审核或已完工
                    submitNum = e.getCheckPassNum().add(e.getCheckNgNum());
                } else if (SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.UN_APPROVE) { // 待审核
                    submitNum = e.getPassNum().add(e.getNgNum());
                }
                return submitNum;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 未审核记工总数
            waitCheckTotalNum = microWorkSubmits.stream().filter(e -> SubmitStatusEnum.UN_APPROVE.getCode().equals(e.getSubmitStatus()))
                    .map(e -> e.getPassNum().add(e.getNgNum())).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 平均良品率
            BigDecimal goodTotalNum = microWorkSubmits.stream().filter(e -> SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.COMPLETE
                    || SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.APPROVED).map(MicroWorkSubmit::getCheckPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 平均良品率：无报工时置 0（原实现 divide(0) → 日报接口 500）
            goodRatio = submitTotalNum.signum() == 0 ? BigDecimal.ZERO
                    : goodTotalNum.divide(submitTotalNum, BigDecimal.ROUND_HALF_UP, RoundingMode.CEILING)
                            .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);

            // 审核及时率
            BigDecimal delayNum = microWorkSubmits.stream().map(e -> {
                BigDecimal num = BigDecimal.ZERO;
                if (SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.UN_APPROVE) { // 待审核，当天日期-记工日期>2天。则延误数+1
                    if (com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(e.getCreatedDate(), DELAY_DAYS, ChronoUnit.DAYS).before(reportDate)) {
                        num = BigDecimal.ONE;
                    }
                } else if (SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.APPROVED
                        || SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.COMPLETE) { // 已审核或已完工，审核时间-记工时间>2天，则延误数+1
                    if (com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(e.getCreatedDate(), DELAY_DAYS, ChronoUnit.DAYS).before(e.getCheckDate())) {
                        num = BigDecimal.ONE;
                    }
                } else if (SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.REJECT) { // 已驳回，驳回时间-记工时间>2天，则延误数+1
                    // 查询驳回时间
                    MicroWorkSubmitHistory queryParam = new MicroWorkSubmitHistory();
                    queryParam.setSubmitNo(e.getSubmitNo());
                    List<MicroWorkSubmitHistory> submitHistories = microWorkSubmitHistoryMapper.selectMicroWorkSubmitHistoryList(queryParam);
                    if (!CollectionUtils.isEmpty(submitHistories)) {
                        Date createdDate = submitHistories.get(0).getCreatedDate();
                        if (com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(e.getCreatedDate(), DELAY_DAYS, ChronoUnit.DAYS).before(createdDate)) {
                            num = BigDecimal.ONE;
                        }
                    }
                }
                return num;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 审核及时率：无报工（无待办）时视为 100%（没有延误），原实现 divide(0) → 日报接口 500
            timelyRatio = submitTotalNum.signum() == 0 ? new BigDecimal("100")
                    : submitTotalNum.subtract(delayNum).divide(submitTotalNum, BigDecimal.ROUND_HALF_UP, RoundingMode.CEILING)
                            .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);

            // 已完工数量
            completeFinishedNum = microWorkSubmits.stream().filter(e -> IsCompleteEnum.getEnum(e.getIsComplete()) == IsCompleteEnum.YES
                            && IsLastProcessEnum.getEnum(e.getIsLastProcess()) == IsLastProcessEnum.YES)
                    .map(MicroWorkSubmit::getCheckPassNum)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 待完工数量
            completeWaitNum = microWorkSubmits.stream().filter(e -> SubmitStatusEnum.getEnum(e.getSubmitStatus()) == SubmitStatusEnum.APPROVED
                            && IsLastProcessEnum.getEnum(e.getIsLastProcess()) == IsLastProcessEnum.YES
                            && IsCompleteEnum.getEnum(e.getIsComplete()) == IsCompleteEnum.NO)
                    .map(MicroWorkSubmit::getCheckPassNum)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        result.setProductModelNum(productModelNum);
        result.setSubmitUserNum(submitUserNum);
        result.setSubmitTotalNum(submitTotalNum.setScale(2, RoundingMode.HALF_UP));
        result.setWaitCheckTotalNum(waitCheckTotalNum.setScale(2, RoundingMode.HALF_UP));
        result.setGoodRatio(goodRatio + "%");
        result.setTimelyRatio(timelyRatio + "%");
        result.setCompleteFinishedNum(completeFinishedNum.setScale(2, RoundingMode.HALF_UP));
        result.setCompleteWaitNum(completeWaitNum.setScale(2, RoundingMode.HALF_UP));
        return result;
    }


}

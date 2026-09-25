/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitWorkInfo;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.infrastructure.constant.ContentTipConstants.KEY_A;
import static com.cosmo.hhim.micro.infrastructure.constant.ContentTipConstants.KEY_B;

/**
 * @author cosmo-hhim-open Team
 * @desc 校验当前员工昨天的记工总数超过{?}%的员工
 * @createTime 2023/2/9
 */
@Slf4j
@Component
public class GoodNumWorkSubmitOfYesterdayHandler extends AbstractBusinessHandler {

    public static final String START_RATIO = "startRatio";
    public static final String END_RATIO = "endRatio";

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;


    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 获取业务条件配置
        Map<String, String> businessConditionConfigMap = super.getBusinessConditionConfigMap();
        if (CollectionUtils.isEmpty(businessConditionConfigMap)
                || !StringUtils.hasText(businessConditionConfigMap.get(START_RATIO))
                || !StringUtils.hasText(businessConditionConfigMap.get(END_RATIO))) {
            log.warn("业务条件参数未配置或配置格式非法！");
            return result;
        }

        Date nowDate = DateUtils.getNowDate();
        Date yesterday = com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(nowDate, -1, ChronoUnit.DAYS);


        // 1.普通员工角色判断是否是当天首次进入小程序
        if (!super.isFirstEnterProgramForWorkerRole(param, nowDate, MicroBusinessConstants.WORKER_ROLE)) {
            return result;
        }

        // 2.判断当前员工昨天的记工总数是否超过昨天参与记工80%的员工
        // 按照用户分组统计昨天已审核的记工总数
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setStartDate(DateUtil.beginOfDay(yesterday).toJdkDate());
        queryParam.setEndDate(DateUtil.endOfDay(yesterday).toJdkDate());
        queryParam.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode()); // 审核通过 
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            queryParam.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            queryParam.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        List<Map<String, Object>> groupSumResult = microWorkSubmitMapper.selectWorkSubmitSumGroupByUser(queryParam);
        if (!CollectionUtils.isEmpty(groupSumResult)) {
            List<SubmitWorkInfo> collect = groupSumResult.stream().map(e -> {
                SubmitWorkInfo submitWorkInfo = new SubmitWorkInfo();
                submitWorkInfo.setSubmitUser((String) e.get(KEY_A));
                submitWorkInfo.setWorkSubmitTotalNum((BigDecimal) e.get(KEY_B));
                return submitWorkInfo;
            }).sorted(Comparator.comparing(SubmitWorkInfo::getWorkSubmitTotalNum)).collect(Collectors.toList());

            // 记工数量相同的用户安排到一组
            Map<BigDecimal, List<SubmitWorkInfo>> totalNumGroupMap = collect.stream().collect(Collectors.groupingBy(SubmitWorkInfo::getWorkSubmitTotalNum));

            // 定位当前用户记工总数在昨天记工排行的位置
            Integer index = null;
            for (int i = 0; i < collect.size(); i++) {
                if (collect.get(i).getSubmitUser().equals(param.getUserId().toString())) {
                    // 计算记工数量相同的用户群中最小的排行索引位置
                    List<String> submitUserList = totalNumGroupMap.get(collect.get(i).getWorkSubmitTotalNum())
                            .stream().map(SubmitWorkInfo::getSubmitUser).collect(Collectors.toList());
                    index = submitUserList.stream().map(e -> getIndex(e, collect)).min(Integer::compareTo).orElse(0);
                    break;
                }
            }

            // 计算业务条件的阈值
            BigDecimal startRatio = new BigDecimal(businessConditionConfigMap.get(START_RATIO));
            BigDecimal endRatio = new BigDecimal(businessConditionConfigMap.get(END_RATIO));
            BigDecimal targetStartValue = (new BigDecimal(collect.size()).multiply(startRatio));
            BigDecimal targetEndValue = (new BigDecimal(collect.size()).multiply(endRatio));

            // 判断当前用户记工总数是否达到业务条件的阈值
            if (null != index
                    && new BigDecimal(index).compareTo(targetStartValue) >= 0
                    && new BigDecimal(index).compareTo(targetEndValue) < 0) {
                result.setMatched(true);

                // 设置占位符填充值
                List<String> placeHolderList = Lists.newArrayList();
                String showStartRatio = String.valueOf(startRatio.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP));
                placeHolderList.add(showStartRatio);

                result.setPlaceHolderInfos(placeHolderList);
            }
        }
        return result;
    }

    /**
     * 获取用户在记工排行的索引位置
     *
     * @param userId
     * @param collect
     * @return
     */
    private Integer getIndex(String userId, List<SubmitWorkInfo> collect) {
        Integer index = null;
        for (int i = 0; i < collect.size(); i++) {
            if (collect.get(i).getSubmitUser().equals(userId)) {
                index = i + 1;
                break;
            }
        }
        return index;
    }


}

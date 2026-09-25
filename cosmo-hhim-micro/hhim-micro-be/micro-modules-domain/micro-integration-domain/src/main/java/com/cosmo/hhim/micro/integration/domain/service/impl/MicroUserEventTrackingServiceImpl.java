/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.micro.infrastructure.config.MicroStatisticsConfig;
import com.cosmo.hhim.micro.infrastructure.enums.base.UserRecommendButtonEnum;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUseButtonEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUsePageEntity;
import com.cosmo.hhim.micro.integration.domain.mapper.MicroUserEventTrackingMapper;
import com.cosmo.hhim.micro.integration.domain.service.IMicroUserEventTrackingService;
import com.cosmo.hhim.micro.integration.domain.tool.TrackingQueueProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户行为事件埋点Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-23
 */
@Slf4j
@Service
public class MicroUserEventTrackingServiceImpl implements IMicroUserEventTrackingService {

    private static final int RECENT_EVENT_THRESHOLD = 2;
    @Autowired
    private MicroUserEventTrackingMapper microUserEventTrackingMapper;
    @Autowired
    private TrackingQueueProcessor queueProcessor;
    @Autowired
    private MicroStatisticsConfig statisticsConfig;

    /**
     * 查询用户行为事件埋点
     *
     * @param id 用户行为事件埋点ID
     * @return 用户行为事件埋点
     */
    @Override
    public MicroUserEventTrackingEntity selectMicroUserEventTrackingById(Long id) {
        return microUserEventTrackingMapper.selectMicroUserEventTrackingById(id);
    }

    /**
     * 查询用户行为事件埋点列表
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 用户行为事件埋点
     */
    @Override
    public List<MicroUserEventTrackingEntity> selectMicroUserEventTrackingList(MicroUserEventTrackingEntity microUserEventTracking) {
        return microUserEventTrackingMapper.selectMicroUserEventTrackingList(microUserEventTracking);
    }

    /**
     * 新增用户行为事件埋点
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 结果
     */
    @Override
    public int insertMicroUserEventTracking(MicroUserEventTrackingEntity microUserEventTracking) {
        queueProcessor.addToQueue(microUserEventTracking);
        return 1;
    }

    /**
     * 修改用户行为事件埋点
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 结果
     */
    @Override
    public int updateMicroUserEventTracking(MicroUserEventTrackingEntity microUserEventTracking) {
        return microUserEventTrackingMapper.updateMicroUserEventTracking(microUserEventTracking);
    }

    /**
     * 批量删除用户行为事件埋点
     *
     * @param ids 需要删除的用户行为事件埋点ID
     * @return 结果
     */
    @Override
    public int deleteMicroUserEventTrackingByIds(Long[] ids) {
        return microUserEventTrackingMapper.deleteMicroUserEventTrackingByIds(ids);
    }

    /**
     * 删除用户行为事件埋点信息
     *
     * @param id 用户行为事件埋点ID
     * @return 结果
     */
    @Override
    public int deleteMicroUserEventTrackingById(Long id) {
        return microUserEventTrackingMapper.deleteMicroUserEventTrackingById(id);
    }

    /**
     * 统计使用次数最多的来源页面(导航栏)
     *
     * @param microUserMostUsePageEntity
     */
    @Override
    public Map<String, Double> getUsingTimesByRefCondition(MicroUserMostUsePageEntity microUserMostUsePageEntity) {
        List<MicroUserEventTrackingEntity> list = microUserEventTrackingMapper.selectUsingTimesByRefCondition(microUserMostUsePageEntity);
        Map<String, List<MicroUserEventTrackingEntity>> collectMap = list.stream().collect(Collectors.groupingBy(MicroUserEventTrackingEntity::getReferPage));
        return commonDeal(collectMap);
    }

    /**
     * 统计使用次数最多的来源页面(子页面)
     *
     * @param microUserMostUsePageEntity
     * @return
     */
    @Override
    public Map<String, Double> getUsingTimesByCurrentCondition(MicroUserMostUsePageEntity microUserMostUsePageEntity) {
        List<MicroUserEventTrackingEntity> list = microUserEventTrackingMapper.selectUsingTimesByCurrentCondition(microUserMostUsePageEntity);
        Map<String, List<MicroUserEventTrackingEntity>> collectMap = list.stream().collect(Collectors.groupingBy(MicroUserEventTrackingEntity::getCurrentPage));
        return commonDeal(collectMap);
    }

    /**
     * 根据用户使用情况来决定推荐哪一个按钮
     *
     * @param buttonEntity
     * @return
     */
    @Override
    public List<String> findUserPersonalizedRecommendButton(MicroUserMostUseButtonEntity buttonEntity) {
        List<String> eventContentList = buttonEntity.getEventContentList();
        List<String> buttonList = new ArrayList<>();
        // 是否单次/批量操作
        List<MicroUserEventTrackingEntity> trackingList = microUserEventTrackingMapper.selectTrackingListByConditionList(buttonEntity);
        if (eventContentList.size() == 1) {
            buttonList.add(UserRecommendButtonEnum.QC.getDesc());
            // 1.同一产品+工序是否连续送检3次以上
            if (this.judgeConsecutiveCount(trackingList)) {
                return buttonList;
            }
        }
        //批量
        return this.getButtonList(trackingList);
    }

    /**
     * 同一产品+工序,记工是否>30次?(送检/记工>0.6)?送检:记工:(记工+送检)
     *
     * @param trackingList 事件追踪列表
     */
    private List<String> getButtonList(List<MicroUserEventTrackingEntity> trackingList) {
        List<String> buttonList = new ArrayList<>();
        long submitCount = 0L;
        long qcCount = 0L;
        String eventName;
        for (MicroUserEventTrackingEntity entity : trackingList) {
            eventName = entity.getEventName();
            if (UserRecommendButtonEnum.SUBMIT.getDesc().equals(eventName)) {
                submitCount++;
            } else if (UserRecommendButtonEnum.QC.getDesc().equals(eventName)) {
                qcCount++;
            }
        }
        //如果大于30次记工
        if (submitCount > statisticsConfig.getSubmitCountThreshold()) {
            //送检/记工次数>0.6
            if ((double) qcCount / (submitCount + qcCount) > statisticsConfig.getQcAndSubmitRadio()) {
                buttonList.add(UserRecommendButtonEnum.QC.getDesc());
                return buttonList;
            }
        }
        buttonList.add(UserRecommendButtonEnum.QC.getDesc());
        buttonList.add(UserRecommendButtonEnum.SUBMIT.getDesc());
        return buttonList;
    }

    /**
     * 同一产品+工序是否连续送检n次以上
     *
     * @param list 事件追踪列表
     * @return
     */
    public boolean judgeConsecutiveCount(List<MicroUserEventTrackingEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        int n = statisticsConfig.getConsecutiveQc();
        if (list.size() < n) {
            return false;
        }
        MicroUserEventTrackingEntity last = list.get(list.size() - 1);
        if (!UserRecommendButtonEnum.QC.getDesc().equals(last.getEventName())) {
            return false;
        }
        for (int i = list.size() - RECENT_EVENT_THRESHOLD; i >= list.size() - n; i--) {
            if (!list.get(i).getEventContent().equals(last.getEventContent())
                    || !list.get(i).getEventName().equals(last.getEventName())) {
                return false;
            }
        }
        return true;
    }


    /**
     * 通用处理
     *
     * @param collectMap
     * @return
     */
    private Map<String, Double> commonDeal(Map<String, List<MicroUserEventTrackingEntity>> collectMap) {
        if (collectMap.size() == 1) {
            return collectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                            .mapToDouble(k -> k.getDuration().multiply(k.getWeight()).doubleValue()).sum()));
        }
        return getUsingTimesResult(collectMap);
    }

    /**
     * 获取每个页面的访问次数结果
     *
     * @param collectMap
     * @return
     */
    private Map<String, Double> getUsingTimesResult(Map<String, List<MicroUserEventTrackingEntity>> collectMap) {
        try {
            //动态计算衰减因子decayFactor
            Map<String, BigDecimal> decayFactorMap = this.calculateDecayFactor(collectMap);
            //计算权重
            this.calculateWeight(decayFactorMap, collectMap);
            Map<String, Double> collect = collectMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                            .mapToDouble(k -> k.getDuration().multiply(k.getWeight()).doubleValue()).sum()));
            return collect;
        } catch (Exception e) {
            log.warn("Failed to get the times of visits pages result:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 计算权重
     *
     * @param decayFactorMap
     * @param groupMap
     */
    private void calculateWeight(Map<String, BigDecimal> decayFactorMap, Map<String, List<MicroUserEventTrackingEntity>> groupMap) {
        for (Map.Entry<String, List<MicroUserEventTrackingEntity>> entry : groupMap.entrySet()) {
            String page = entry.getKey();
            if (decayFactorMap.containsKey(page)) {
                for (MicroUserEventTrackingEntity entity : entry.getValue()) {
                    this.finalWeight(decayFactorMap.get(page), entity);
                }
            }
        }
    }

    /**
     * 根据衰减因子最终计算权重
     * weight = count / (1 + decayFactor * daysSinceLastVisit)
     * 其中，count是页面的访问次数，decayFactor是衰减系数，daysSinceLastVisit是距离上次访问该页面的天数。
     *
     * @param decayFactor
     * @param entity
     */
    private void finalWeight(BigDecimal decayFactor, MicroUserEventTrackingEntity entity) {
        long daysSinceLastVisit = DateUtil.between(entity.getCreatedDate(), DateUtil.date(), DateUnit.DAY);
        // 全部使用 BigDecimal 计算权重，避免 double 除法产生 NaN/Infinity 与浮点误差（拒绝服务与精度加固）
        BigDecimal decay = decayFactor == null ? BigDecimal.ZERO : decayFactor;
        BigDecimal denominator = BigDecimal.ONE.add(decay.multiply(BigDecimal.valueOf(daysSinceLastVisit)));
        BigDecimal weight;
        if (denominator.signum() == 0) {
            weight = BigDecimal.ZERO;
        } else {
            weight = BigDecimal.ONE.divide(denominator, 8, RoundingMode.HALF_UP);
        }
        entity.setWeight(entity.getWeight().multiply(weight).setScale(2, RoundingMode.UP));
    }

    /**
     * 计算衰减因子
     *
     * @param collectMap
     * @return
     */
    private Map<String, BigDecimal> calculateDecayFactor(Map<String, List<MicroUserEventTrackingEntity>> collectMap) {
        //将查询出的pageList转成Map<page,List<CreateDate>>形式,为了计算衰减因子
        Map<String, List<LocalDateTime>> timeMap = collectMap.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(k -> LocalDateTime.ofInstant(k.getCreatedDate().toInstant(), ZoneId.systemDefault()))
                        .collect(Collectors.toList())));
        Map<String, BigDecimal> result = timeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> finalDecayFactor(entry.getValue())));
        return result;
    }

    /**
     * 计算最终的衰减因子
     *
     * @param visitTimes
     * @return
     */
    public BigDecimal finalDecayFactor(List<LocalDateTime> visitTimes) {
        // 初始值
        double decayFactor = statisticsConfig.getDecayFactor().doubleValue();
        if (visitTimes.size() >= RECENT_EVENT_THRESHOLD) {
            long totalSeconds = 0;
            for (int i = 1; i < visitTimes.size(); i++) {
                Duration duration = Duration.between(visitTimes.get(i - 1), visitTimes.get(i));
                totalSeconds += duration.getSeconds();
            }
            double meanSeconds = (double) totalSeconds / (visitTimes.size() - 1);
            if (meanSeconds > 0) {
                decayFactor = 1 / meanSeconds;
            }
        }
        return BigDecimal.valueOf(decayFactor).setScale(2, RoundingMode.UP);
    }

}

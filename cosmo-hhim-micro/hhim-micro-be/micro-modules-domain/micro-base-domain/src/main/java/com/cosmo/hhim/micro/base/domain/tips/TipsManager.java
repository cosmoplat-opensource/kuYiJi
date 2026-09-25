/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.tips.*;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroContentTipConfigMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroContentTipRecordMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerChainManager;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler.ClockTipHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.clock.router.ClockHandlerRouter;
import com.cosmo.hhim.micro.infrastructure.enums.PeriodTimeUnitEnum;
import com.cosmo.hhim.micro.infrastructure.enums.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-02-01
 */
@Slf4j
@Component
public class TipsManager implements ApplicationContextAware {

    @Autowired
    private ClockHandlerRouter clockHandlerRouter;
    @Autowired
    private MicroContentTipConfigMapper microContentTipConfigMapper;
    @Autowired
    private MicroContentTipRecordMapper microContentTipRecordMapper;
    @Autowired
    private MicroUserMapper microUserMapper;
    // decouple-from-ops-platform-cleanup (A.3): trial service 引用已删除

    private ApplicationContext applicationContext;

    /**
     * 执行提示
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public TipResult executeTip(MicroContentTipExecuteParam executeParam) {
        TipResult tipResult = new TipResult();
        String tipContent = null;
        String tipWay = null;

        // 查询当前用户的角色列表
        List<String> roleCodes = queryUserRoles();

        // 1.通过用户角色、触发动作筛选符合提示的配置项集合
        List<MicroContentTipConfig> contentTipConfigs = queryMicroContentTipConfigs(executeParam, roleCodes);
        if (CollectionUtils.isEmpty(contentTipConfigs)) {
            return tipResult;
        }

        log.info("1.内容提示>>>>> DB筛选后BeanNames:{}", this.extractBusinessBeanNames(contentTipConfigs));

        // 2.执行时间触发条件过滤动作
        contentTipConfigs = clockFilterConfig(contentTipConfigs);
        if (CollectionUtils.isEmpty(contentTipConfigs)) {
            return tipResult;
        }
        log.info("2.内容提示>>>>> 时间触发条件筛选后BeanNames:{}", this.extractBusinessBeanNames(contentTipConfigs));

        // 3.按照优先级排序
        contentTipConfigs = contentTipConfigs.stream()
                .sorted(Comparator.comparing(MicroContentTipConfig::getPriority))
                .collect(Collectors.toList());

        // 4.执行业务条件过滤动作
        Long tipConfigId = null;
        List<MicroContentTipConfig> tipConfigs = contentTipConfigs.stream()
                .filter(e -> StringUtils.hasText(e.getTriggerBusinessBean()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tipConfigs)) { // 无业务条件 
            MicroContentTipConfig microContentTipConfig = contentTipConfigs.get(0);
            tipWay = microContentTipConfig.getTipWay();
            tipContent = microContentTipConfig.getTipContent();
            tipConfigId = microContentTipConfig.getId();
        } else {
            // 添加业务处理器到责任链
            BusinessHandlerChainManager businessHandlerChainManager = new BusinessHandlerChainManager(applicationContext);
            tipConfigs.forEach(businessHandlerChainManager::addHandler);

            // 执行业务处理责任链线条
            BusinessHandlerParam handlerParam = new BusinessHandlerParam();
            handlerParam.setUserId(SecurityUtils.getUserId());
            handlerParam.setRoleCodes(roleCodes);
            BusinessHandlerResult handlerResult = businessHandlerChainManager.executeNextHandler(handlerParam);
            if (null != handlerResult && handlerResult.isMatched()) {
                MicroContentTipConfig tipConfig = handlerResult.getTipConfig();
                tipConfigId = tipConfig.getId();
                tipWay = tipConfig.getTipWay();
                tipContent = tipConfig.getTipContent();

                // 填充提示内容占位符信息
                if (!CollectionUtils.isEmpty(handlerResult.getPlaceHolderInfos())) {
                    List<String> placeHolderInfos = handlerResult.getPlaceHolderInfos();
                    tipContent = MessageFormat.format(tipContent, placeHolderInfos.toArray());
                }
            }
        }

        // 5.存储内容消息提示记录表
        if (null != tipConfigId && StringUtils.hasText(tipContent)) {

            // decouple-from-ops-platform-cleanup (A.3): trial 分支已下线
            Long userId = SecurityUtils.getUserId();
            String userType = UserTypeEnum.OFFICIAL_USER.getCode();
            // 老的 trial 分支：if (StringUtils.hasText(SecurityUtils.getTrialPhone())) { ... } 已 no-op

            MicroContentTipRecord tipRecord = new MicroContentTipRecord();
            tipRecord.setTipDate(DateUtils.getNowDate());
            tipRecord.setUserId(userId);
            tipRecord.setUserType(userType);
            tipRecord.setTipContent(tipContent);
            tipRecord.setTipConfigId(tipConfigId);
            microContentTipRecordMapper.insertMicroContentTipRecord(tipRecord);
        }

        tipResult.setTipContent(tipContent);
        tipResult.setTipWay(tipWay);

        return tipResult;
    }

    /**
     * 查询内容提示配置
     *
     * @param executeParam
     * @param roleCodes
     * @return
     */
    private List<MicroContentTipConfig> queryMicroContentTipConfigs(MicroContentTipExecuteParam executeParam, List<String> roleCodes) {
        MicroContentTipConfigParam queryParam = new MicroContentTipConfigParam();
        queryParam.setTriggerRoles(roleCodes);
        queryParam.setTriggerActions(executeParam.getTriggerActions());
        queryParam.setActionType(executeParam.getActionType());
        List<MicroContentTipConfig> contentTipConfigs = microContentTipConfigMapper.selectMicroContentTipConfigList(queryParam);
        return contentTipConfigs;
    }

    /**
     * 查询当前用户的角色列表
     *
     * @return
     */
    private List<String> queryUserRoles() {
        Long userId = SecurityUtils.getUserId();
        // userId=0 表示当前请求未经过 CertificateInterceptor（不在 includedPaths 内），
        // 角色查询无意义，返回空列表让调用方跳过提示逻辑而非抛异常
        if (userId == null || userId == 0) {
            log.debug("queryUserRoles: userId={}, 跳过角色查询", userId);
            return Collections.emptyList();
        }
        List<MicroRole> microRoles = microUserMapper.selectRoleInfoByUserId(userId);
        if (CollectionUtils.isEmpty(microRoles)) {
            throw new CustomException("用户无对应角色信息！");
        }
        List<String> roleCodes = microRoles.stream().map(MicroRole::getRoleCode).collect(Collectors.toList());
        return roleCodes;
    }

    /**
     * 时间触发条件过滤
     *
     * @param contentTipConfigs
     * @return
     */
    private List<MicroContentTipConfig> clockFilterConfig(List<MicroContentTipConfig> contentTipConfigs) {
        return contentTipConfigs.stream().filter(e -> {
            // 1.获取时间触发的Handler
            ClockTipHandler clockTipHandler = clockHandlerRouter.getHandler(e.getPeriodType(), null);

            // 2.执行handler判断时间触发条件是否满足
            TimeTriggerInfo timeTriggerInfo = new TimeTriggerInfo();
            timeTriggerInfo.setTipConfigId(e.getId());
            timeTriggerInfo.setPeriodType(e.getPeriodType());
            timeTriggerInfo.setPeriod(e.getPeriodTime());
            if (StringUtils.hasText(e.getPeriodUnit())) {
                timeTriggerInfo.setPeriodTimeUnit(PeriodTimeUnitEnum.getEnum(e.getPeriodUnit()).getChronoUnit());
            }
            timeTriggerInfo.setIntervalTime(e.getIntervalTime());
            if (StringUtils.hasText(e.getIntervalUnit())) {
                timeTriggerInfo.setIntervalTimeUnit(PeriodTimeUnitEnum.getEnum(e.getIntervalUnit()).getChronoUnit());
            }
            timeTriggerInfo.setTriggerMaxNum(e.getPeriodMaxNum());

            boolean matchTipCondition = clockTipHandler.isMatchTipCondition(timeTriggerInfo);
            log.info("内容提示>>>contentTipConfigBusinessName:{} ---> ClockHandlerName:{}，时间触发条件满足性结果:{}",
                    e.getTriggerBusinessBean(), clockTipHandler.getClass().getSimpleName(), matchTipCondition);

            return matchTipCondition;
        }).collect(Collectors.toList());
    }


    /**
     * 提取业务处理BeanName
     *
     * @param tipConfigs
     * @return
     */
    private String extractBusinessBeanNames(List<MicroContentTipConfig> tipConfigs) {
        List<String> businessBeanNames = tipConfigs.stream().map(MicroContentTipConfig::getTriggerBusinessBean).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(businessBeanNames)) {
            return null;
        }

        return JSON.toJSONString(businessBeanNames);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 根据试用者手机号获取试用者用户ID
     * decouple-from-ops-platform-cleanup (A.3): trial 下线后此方法无 caller，保留为 no-op 防止编译失败
     */
    @Deprecated
    private Long getTrialUserIdByTrialPhone(String trialPhone) {
        return null;
    }
}

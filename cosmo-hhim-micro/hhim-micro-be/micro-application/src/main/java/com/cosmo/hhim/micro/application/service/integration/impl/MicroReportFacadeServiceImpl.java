/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.MapBeanUtil;
import com.cosmo.hhim.common.event.EventManager;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.service.integration.IMicroReportFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.DailyReportContentEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.common.WeekReportContentEntity;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroReportService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.ActiveFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import com.cosmo.hhim.micro.integration.domain.entity.NotifyMessageContent;
import com.cosmo.hhim.micro.integration.domain.entity.TodayReportMessageContent;
import com.cosmo.hhim.micro.integration.domain.entity.WeekReportMessageContent;
import com.cosmo.hhim.micro.integration.domain.notify.WxMessageEventFactory;
import com.cosmo.hhim.micro.integration.domain.notify.event.WeekReportMessageEvent;
import com.cosmo.hhim.micro.integration.domain.notify.event.WxTemplateMessageEvent;
import com.cosmo.hhim.micro.integration.domain.service.IMicroNoticeConfigService;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MicroReportFacadeServiceImpl implements IMicroReportFacadeService {

    @Autowired
    private IMicroReportService reportService;
    @Autowired
    private MicroUserMapper microUserMapper;
    @Autowired
    private WxMessageEventFactory wxMessageEventFactory;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IMicroNoticeConfigService microNoticeConfigService;

    @Value("${sms.weekReportForMicroProcessTempleteId}")
    private String weekReportForMicroProcessTempleteId;

    /**
     * 生产日报推送消息
     *
     * @param reportDate
     */
    @Override
    public NotifyMessageContent genTodayReportMessageContent(Date reportDate) {
        TodayReportMessageContent messageContent = new TodayReportMessageContent();
        DailyReportContentEntity contentEntity = reportService.statisticsTodayReport(reportDate);
        BeanUtils.copyProperties(contentEntity, messageContent);
        return messageContent;
    }

    /**
     * 生产周报推送消息
     *
     * @param reportDate
     */
    @Override
    public NotifyMessageContent genWeekReportMessageContent(Date reportDate) {
        WeekReportMessageContent messageContent = new WeekReportMessageContent();
        WeekReportContentEntity contentEntity = reportService.statisticsWeekReport(reportDate);
        BeanUtils.copyProperties(contentEntity, messageContent);

        return messageContent;
    }

    /**
     * 按照推送配置推送通知消息
     *
     * @param reportDate
     * @param noticeBusinessSignEnum
     */
    @Override
    public void pushMessageByNoticeConfig(Date reportDate, NotifyEnums.NoticeBusinessSignEnum noticeBusinessSignEnum) {

        // 1.查询推送配置信息表
        MicroNoticeConfig queryParam = new MicroNoticeConfig();
        queryParam.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
        queryParam.setBusinessSign(noticeBusinessSignEnum.getCode());
        List<MicroNoticeConfig> microNoticeConfigs = microNoticeConfigService.selectMicroNoticeConfigList(queryParam);

        // 可通知配置过滤
        microNoticeConfigs = microNoticeConfigs.stream()
                .filter(e -> StringUtils.hasText(e.getNoticeUsers()) || StringUtils.hasText(e.getNoticeRoles()))
                .collect(Collectors.toList());

        // 2.发送渠道推送事件
        for (MicroNoticeConfig noticeConfig : microNoticeConfigs) {
            ThreadContext.put(Constants.APPLICATION_SIGN, noticeConfig.getAppSign());
            switch (NotifyEnums.NoticeBusinessSignEnum.getEnum(noticeConfig.getBusinessSign())) { // 按照业务类型生成推送报文 
                case PRODUCE_TODAY_REPORT: // 生产日报 
                    publishChannelMessageEvent(genTodayReportMessageContent(reportDate), noticeConfig);
                    break;

                case PRODUCE_WEEK_REPORT: // 生产周报 
                    publishChannelMessageEvent(genWeekReportMessageContent(reportDate), noticeConfig);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 按照渠道发布推送事件
     *
     * @param messageContent
     * @param microNoticeConfig
     */
    private void publishChannelMessageEvent(NotifyMessageContent messageContent, MicroNoticeConfig microNoticeConfig) {
        String[] noticeChannelList = microNoticeConfig.getNoticeChannels().split(",");
        for (String noticeChannel : noticeChannelList) {
            switch (NotifyEnums.NoticeChannelEnum.getEnum(noticeChannel)) {
                case WECHAT_MP:
                case WECHAT_MINI_PROGRAM:
                    // 发布微信消息推送事件
                    publishWxMessageEvent(microNoticeConfig, noticeChannel, messageContent);

                    break;
                case SMS:
                    // 发布SMS推送事件
                    publishSMSMessageEvent(microNoticeConfig, messageContent);

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 发布SMS消息推送事件
     *
     * @param microNoticeConfig
     * @param messageContent
     */
    private void publishSMSMessageEvent(MicroNoticeConfig microNoticeConfig, NotifyMessageContent messageContent) { 

        // 1.查询需要推送的用户
        Set<String> phoneNumberSet = Sets.newHashSet();

        // 1.1 查询角色对应的用户手机号集合
        if (StringUtils.hasText(microNoticeConfig.getNoticeRoles())) {
            List<String> roleCodes = Arrays.asList(microNoticeConfig.getNoticeRoles().split(","));
            List<String> phoneByRoleCodes = microUserMapper.selectMicroUserPhoneByRoleCodes(roleCodes);
            if (!CollectionUtils.isEmpty(phoneByRoleCodes)) {
                phoneNumberSet.addAll(phoneByRoleCodes);
            }
        }

        // 1.2 查询用户ID对应的用户手机号集合
        if (StringUtils.hasText(microNoticeConfig.getNoticeUsers())) {
            List<String> userIds = Arrays.asList(microNoticeConfig.getNoticeUsers().split(","));
            List<MicroUser> microUsers = microUserMapper.selectMicroUserListByUserIdList(new HashSet<>(userIds));
            if (!CollectionUtils.isEmpty(microUsers)) {
                List<String> phones = microUsers.stream().map(MicroUser::getPhonenumber).collect(Collectors.toList());
                phoneNumberSet.addAll(phones);
            }
        }

        if (CollectionUtils.isEmpty(phoneNumberSet)) {
            return;
        }

        // 2.发布事件
        for (Iterator<String> iterator = phoneNumberSet.iterator(); iterator.hasNext(); ) {
            String phoneNumber = iterator.next();

            // 生成事件
            WeekReportMessageEvent messageEvent = new WeekReportMessageEvent();
            SendSmsParam sendSmsParam = new SendSmsParam();
            sendSmsParam.setTempId(weekReportForMicroProcessTempleteId);
            sendSmsParam.setReceiver(phoneNumber);
            sendSmsParam.setParams(MapBeanUtil.beanToMap(messageContent));
            messageEvent.setSendSmsParam(sendSmsParam);

            // 发布事件
            EventManager.publishEvent(messageEvent);
        }
    }

    /**
     * 发布微信消息推送事件
     *
     * @param microNoticeConfig
     * @param messageContent
     */
    private void publishWxMessageEvent(MicroNoticeConfig microNoticeConfig, String noticeChannel, NotifyMessageContent messageContent) {
        String appSign = SecurityUtils.getApplicationSign();
        String appCode = this.remoteQueryAppCode(appSign);

        // 1.查询需要推送的用户
        Set<String> openIdSet = Sets.newHashSet();

        // 1.1 查询角色对应的用户openId集合
        if (StringUtils.hasText(microNoticeConfig.getNoticeRoles())) {
            List<String> noticeRoles = Arrays.asList(microNoticeConfig.getNoticeRoles().split(","));
            List<String> openIdsByRoles = microUserMapper.selectPlatformOpenIdsByRoles(noticeRoles, appCode, appSign,
                    NotifyEnums.NoticeChannelEnum.getEnum(noticeChannel).getSign());
            if (!CollectionUtils.isEmpty(openIdsByRoles)) {
                openIdSet.addAll(openIdsByRoles);
            }
        }

        // 1.2 查询需要推送的用户的openId集合
        if (StringUtils.hasText(microNoticeConfig.getNoticeUsers())) {
            List<String> noticeUsers = Arrays.asList(microNoticeConfig.getNoticeUsers().split(","));
            List<String> openIdsByUserIds = microUserMapper.selectPlatformOpenIdsByUserIds(noticeUsers, appSign,
                    NotifyEnums.NoticeChannelEnum.getEnum(noticeChannel).getSign());
            if (!CollectionUtils.isEmpty(openIdsByUserIds)) {
                openIdSet.addAll(openIdsByUserIds);
            }
        }

        if (CollectionUtils.isEmpty(openIdSet)) {
            return;
        }

        // 2.发布事件
        for (Iterator<String> iterator = openIdSet.iterator(); iterator.hasNext(); ) {
            String openId = iterator.next();
            // 生成推送事件
            WxTemplateMessageEvent messageEvent = wxMessageEventFactory.createMessageEvent(openId, microNoticeConfig.getAppSign(),
                    noticeChannel, microNoticeConfig.getBusinessSign(), messageContent);

            // 发布推送事件
            if (null != messageEvent) {
                EventManager.publishEvent(messageEvent);
            }
        }
    }


    /**
     * 调用三方平台根据应用标识查询应用编码
     *
     * @param appSign
     * @return
     */
    public String remoteQueryAppCode(String appSign) {
        Map<String, Object> cacheMap = redisCache.getCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY);
        return CollectionUtils.isEmpty(cacheMap) ? "" : (String) cacheMap.get(appSign);
    }
}

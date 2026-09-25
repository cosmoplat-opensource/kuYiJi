/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.notify;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatMsgTemplateConfig;
import com.cosmo.hhim.micro.integration.domain.entity.TodayReportMessageContent;
import com.cosmo.hhim.micro.integration.domain.entity.NotifyMessageContent;
import com.cosmo.hhim.micro.integration.domain.mapper.MicroWechatMsgTemplateConfigMapper;
import com.cosmo.hhim.micro.integration.domain.notify.event.TodayReportMessageEvent;
import com.cosmo.hhim.micro.integration.domain.notify.event.WxTemplateMessageEvent;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageDataItem;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/22
 */
@Slf4j
@Component
public class WxMessageEventFactory {

    @Autowired
    private MicroWechatMsgTemplateConfigMapper microWechatMsgTemplateConfigMapper;
    @Value("${miniProgram.state:developer}")
    private String miniProgramState;

    public <T extends WxTemplateMessageEvent> T createMessageEvent(String openid, String appSign, String noticeChannel, String businessSign, NotifyMessageContent messageContent) {

        try {
            // 1.查询消息模版配置
            MicroWechatMsgTemplateConfig microWechatMsgTemplateConfig = microWechatMsgTemplateConfigMapper
                    .selectMicroWechatMsgTemplateConfig(appSign, noticeChannel, businessSign);
            if (null == microWechatMsgTemplateConfig) {
                log.warn("未查询到符合的消息模版配置！serviceSign:{}, templateType:{}, messageType:{}", appSign, noticeChannel, businessSign);
                return null;
            }
            // 消息事件类白名单：仅允许已注册的事件类，防止数据库配置被篡改后加载任意类（显式分发替代反射）
            String messageClazzName = microWechatMsgTemplateConfig.getMessageClazz();
            if (!TodayReportMessageEvent.class.getName().equals(messageClazzName)) {
                log.warn("非法的消息模版类名，已拒绝加载: {}", messageClazzName);
                return null;
            }

            // 2.构建消息发送体
            List<SendMessageParam> sendMessageParams = Lists.newArrayList();
            if (StringUtils.hasText(openid)) {
                sendMessageParams.add(this.createMsgTemplateParam(microWechatMsgTemplateConfig, NotifyEnums.NoticeBusinessSignEnum.getEnum(businessSign), openid, messageContent));
            }

            // 3.创建消息对应的事件
            if (!CollectionUtils.isEmpty(sendMessageParams)) {
                return getInstance(sendMessageParams, appSign, noticeChannel);
            }

        } catch (Exception e) {
            log.error("生成微信模版消息事件发生异常！errorMsg:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }


    /**
     * 创建消息事件实例（显式分发，替代反射创建）
     *
     * @param sendMessageParams
     * @param serviceSign
     * @param templateType
     * @return
     */
    private <T extends WxTemplateMessageEvent> T getInstance(List<SendMessageParam> sendMessageParams, String serviceSign, String templateType) {
        sendMessageParams = sendMessageParams.stream().filter(distinctByKey(SendMessageParam::getTouser)).collect(Collectors.toList());
        return (T) new TodayReportMessageEvent(sendMessageParams,
                CommonConstant.ApplicationSignEnum.getEnum(serviceSign),
                NotifyEnums.NoticeChannelEnum.getEnum(templateType));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>(16);
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    /**
     * 构建微信模版消息
     *
     * @param wechatMsgTemplateConfig
     * @param openid
     * @param messageContent
     * @return
     */
    private SendMessageParam createMsgTemplateParam(MicroWechatMsgTemplateConfig wechatMsgTemplateConfig,
                                                    NotifyEnums.NoticeBusinessSignEnum businessSignEnum, String openid,
                                                    NotifyMessageContent messageContent) {
        Map<String, SendMessageDataItem> messageContentMap = Maps.newHashMap();
        switch (businessSignEnum) {
            case PRODUCE_TODAY_REPORT: // 生产日报 
                DecimalFormat decimalFormatA = new DecimalFormat("0.0#");
                DecimalFormat decimalFormatB = new DecimalFormat("0.00#");

                TodayReportMessageContent resultMsgContent = (TodayReportMessageContent) messageContent;
                messageContentMap.put(NotifyEnums.TodayReportMsgTemplateContentKey.KEYWORD1.getKey(), SendMessageDataItem.builder().value(String.valueOf(resultMsgContent.getProductTotalNum())).build());
                messageContentMap.put(NotifyEnums.TodayReportMsgTemplateContentKey.KEYWORD2.getKey(), SendMessageDataItem.builder().value(decimalFormatB.format(resultMsgContent.getInProductNum())).build());
                messageContentMap.put(NotifyEnums.TodayReportMsgTemplateContentKey.KEYWORD3.getKey(), SendMessageDataItem.builder().value(String.valueOf(resultMsgContent.getWorkerNum())).build());
                messageContentMap.put(NotifyEnums.TodayReportMsgTemplateContentKey.KEYWORD4.getKey(), SendMessageDataItem.builder().value(decimalFormatA.format(resultMsgContent.getCheckProgress()) + "%").build());
                messageContentMap.put(NotifyEnums.TodayReportMsgTemplateContentKey.KEYWORD5.getKey(), SendMessageDataItem.builder().value(decimalFormatB.format(resultMsgContent.getRejectProductNum())).build());
                break;

            default:
                break;
        }

        SendMessageParam msgTemplateParam = new SendMessageParam();
        msgTemplateParam.setTemplate_id(wechatMsgTemplateConfig.getTemplateId());
        msgTemplateParam.setPage(wechatMsgTemplateConfig.getSkipUrl());
        msgTemplateParam.setTouser(openid);
        msgTemplateParam.setData(messageContentMap);
        msgTemplateParam.setMiniprogram_state(miniProgramState);
        return msgTemplateParam;
    }


}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.integration.MicroCustomerSuggestionDTO;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroSuggestion;
import com.cosmo.hhim.micro.base.domain.service.common.ISuggestionService;
import com.cosmo.hhim.micro.infrastructure.events.OpenFeishuEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;

/**
 * decouple-from-ops-platform：意见反馈改用本地 ISuggestionService
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class CustomerSuggestionFacadeService {
    @Autowired
    private ISuggestionService suggestionService;
    @Autowired
    private ApplicationEventPublisher publisher;
    /** 意见反馈详情页链接（开源部署通过配置注入，默认空） */
    @Value("${feedback.detail-url:}")
    private String feedbackDetailUrl;

    public TableDataInfo suggestionList(Integer pageNum, Integer pageSize) {
        log.warn("[deprecation] suggestionList 改读本地 micro_suggestion；新表结构无 role/phone 维度筛选，建议后续接 Controller 走 Service.list");
        TableDataInfo data = new TableDataInfo();
        data.setRows(Collections.emptyList());
        data.setTotal(0L);
        return data;
    }

    public int submitNewSuggestion(MicroCustomerSuggestionDTO suggestionDTO) {
        MicroSuggestion record = new MicroSuggestion();
        record.setPhone(SecurityUtils.getUsername());
        record.setTenantCode((String) ThreadContext.get(Constants.TARGET_CUSTOMER));
        record.setContent(suggestionDTO.getContent());
        record.setCreatedAt(new Date());

        int affected = suggestionService.insert(record);
        if (affected > 0) {
            sendFeishuBotMsg(suggestionDTO);
            return 1;
        }
        return 0;
    }

    private void sendFeishuBotMsg(MicroCustomerSuggestionDTO suggestionDTO) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("请注意,有新的意见反馈:\n\n")
                    .append("用户:<").append(SecurityUtils.getUsername()).append(">\n")
                    .append("反馈内容:<").append(suggestionDTO.getContent()).append(">\n")
                    .append("联系方式:<").append(suggestionDTO.getContactNumber()).append(">\n")
                    .append(StringUtils.hasText(feedbackDetailUrl) ? "点击查看详情:" + feedbackDetailUrl : "");
            publisher.publishEvent(new OpenFeishuEvent(sb.toString()));
        } catch (Exception e) {
            log.error("推送飞书机器人意见反馈信息失败", e);
        }
    }
}

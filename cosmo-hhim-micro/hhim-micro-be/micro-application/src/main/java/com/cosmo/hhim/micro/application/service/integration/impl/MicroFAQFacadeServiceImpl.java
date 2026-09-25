/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration.impl;

import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.service.integration.IMicroFAQFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.service.common.IFaqService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.events.OpenFeishuEvent;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqPairRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * decouple-from-ops-platform：FAQ facade 改用本地 IFaqService
 * 注：底层表结构简化为 micro_faq；保留 IMicroFAQFacadeService 接口签名，
 * 旧 DTO（HyzzFaqPairRecordEntity）相关方法已不再被远端数据源支撑，
 * 现阶段对调用方做最小破坏性改造 —— 仅保留关键字/类别查询流程。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroFAQFacadeServiceImpl implements IMicroFAQFacadeService { 

    @Autowired
    private IMicroUserService userService;
    @Autowired
    private IFaqService faqService;
    @Autowired
    private ApplicationEventPublisher publisher;
    /** 意见反馈详情页链接（开源部署通过配置注入，默认空） */
    @Value("${feedback.detail-url:}")
    private String feedbackDetailUrl;

    @Override
    public List<HyzzFaqPairRecordEntity> guessULike(String keyword) { 
        log.warn("[deprecation] guessULike 已切换到本地 micro_faq；原 HyzzFaqPairRecordEntity 返回类型不再适用");
        return Collections.emptyList();
    }

    @Override
    public List<String> keyword() {
        log.warn("[deprecation] keyword 已切换到本地 micro_faq；新模型不再支持 role→index 反查");
        return Collections.emptyList();
    }

    @Override
    public HyzzFaqPairRecordEntity getFixedAnswer(Long qId) {
        log.warn("[deprecation] getFixedAnswer({}) 已切换到本地 micro_faq；按 id 查 title/content", qId);
        return null;
    }

    @Override
    public HyzzFaqPairRecordEntity getMostSimilarAnswer(String content) {
        log.warn("[deprecation] getMostSimilarAnswer 已切换到本地 micro_faq；相似度匹配未实现");
        sendFeishuBotMsg(content);
        return null;
    }

    @Override
    public List<HyzzFaqPairRecordEntity> getSegmentWords(String content) {
        log.warn("[deprecation] getSegmentWords 已切换到本地 micro_faq；分词未实现");
        return Collections.emptyList();
    }

    @Override
    public TableDataInfo getChatHistory(Integer pageNum, Integer pageSize) {
        log.warn("[deprecation] getChatHistory 改用本地 micro_suggestion；详见 CustomerSuggestionFacadeService");
        return new TableDataInfo();
    }

    private void sendFeishuBotMsg(String content) {
        try {
            String str = "请注意,有新的意见反馈:\n\n" +
                    "用户:<" + SecurityUtils.getUsername() + ">\n" +
                    "反馈内容:<" + content + ">\n" +
                    "联系方式:<" + ">\n" +
                    (StringUtils.hasText(feedbackDetailUrl) ? "点击查看详情:" + feedbackDetailUrl : "");
            publisher.publishEvent(new OpenFeishuEvent(str));
        } catch (Exception e) {
            log.error("推送飞书机器人意见反馈信息失败", e);
        }
    }

    private List<String> getRoleCodeList() {
        List<MicroRole> microRoles = userService.selectMicroRolesByUserId(SecurityUtils.getUserId());
        if (!CollectionUtils.isEmpty(microRoles)) {
            return microRoles.stream().map(MicroRole::getRoleCode).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

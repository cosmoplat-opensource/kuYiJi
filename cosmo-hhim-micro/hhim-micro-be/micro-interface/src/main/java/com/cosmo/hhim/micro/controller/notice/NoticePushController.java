/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.controller.notice;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.service.integration.IMicroReportFacadeService;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;

/**
 * 通知推送 · 手动触发入口
 *
 * <p>用于运维/定时任务手动触发生产日报、生产周报的渠道推送（微信 / 短信，按
 * {@code micro_notice_config} 中已启用且配置了通知人或角色的记录逐条推送）。
 *
 * <p><b>安全说明</b>：本接口未标注 {@code @AccessAuth}，因此不经登录态与角色校验
 * （{@code AccessAuthFilter} 仅对标注该注解的接口强制鉴权），且推送无去重、无限流，
 * 反复调用会向所有已配置用户重复推送（短信渠道还会产生真实费用）。
 * 因此默认关闭，需显式设置 {@code NOTIFY_MANUAL_TRIGGER_ENABLED=true} 才可调用。
 *
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/9
 */
@RestController
@RequestMapping("/notice")
public class NoticePushController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroReportFacadeService microReportfacadeService;

    /**
     * 手动触发推送开关（默认 false）。
     *
     * <p>本接口无鉴权且推送无幂等，对外暴露会被反复调用刷消息 / 刷短信费，
     * 故默认关闭；需要外部定时任务触发时在 .env 中开启。
     */
    @Value("${notify.manual-trigger-enabled:false}")
    private boolean manualTriggerEnabled;

    /**
     * 触发指定业务的渠道推送（手动调用）
     *
     * @param businessSign 业务标识：10 = 生产日报，20 = 生产周报
     */
    @GetMapping("/send")
    public AjaxResult sendWxMiniProgram(@RequestParam("businessSign") NotifyEnums.NoticeBusinessSignEnum businessSign) {
        if (!manualTriggerEnabled) {
            return AjaxResult.error("手动触发推送已关闭（如需开启请设置 NOTIFY_MANUAL_TRIGGER_ENABLED=true）");
        }
        microReportfacadeService.pushMessageByNoticeConfig(new Date(), businessSign);
        return AjaxResult.success();
    }

}

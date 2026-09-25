/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserIndividuationConfig;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserIndividuationConfigService;
import com.cosmo.hhim.micro.infrastructure.events.UserRecommendStatisticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 用户推荐统计时间窗口刷新事件
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class FlushUserRecommendStatisticsListener implements ApplicationListener<UserRecommendStatisticsEvent> {
    @Autowired
    private IMicroUserIndividuationConfigService userConfigService;

    @Async("asyncEventExecutor")
    @Override
    public void onApplicationEvent(UserRecommendStatisticsEvent statisticsEvent) {
        log.info("监听到:用户推荐统计时间窗口刷新事件,开始执行");
        Long userId = statisticsEvent.getUserId();
        String tenantCode = statisticsEvent.getTenantCode();
        MicroUserIndividuationConfig config = new MicroUserIndividuationConfig();
        config.setUserId(userId);
        config.setTenantCode(tenantCode);
        config.setStatisticsBeginTime(new Date());
        config.setTenantCode(tenantCode);
        userConfigService.flushUserRecommendWindow(config);
        log.info("{}用户推荐统计时间窗口刷新事件:结束", userId);
    }
}

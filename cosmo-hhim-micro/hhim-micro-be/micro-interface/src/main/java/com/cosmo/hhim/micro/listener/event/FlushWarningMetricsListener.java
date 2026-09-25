/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.cosmo.hhim.micro.application.service.storage.IMicroProcessStorageFacadeService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.events.FlushWarningMetricsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 刷新异常指标事件监听
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class FlushWarningMetricsListener implements ApplicationListener<FlushWarningMetricsEvent> {
    @Autowired
    private IMicroProcessStorageFacadeService processStorageFacadeService;
    @Value("${spring.profiles.active}")
    private String env;

    @Async("asyncEventExecutor")
    @Override
    public void onApplicationEvent(FlushWarningMetricsEvent flushWarningMetricsEvent) {
        if (!CommonConstants.ACTIVE_ENV_SET.contains(env)) {
            return;
        }
        log.info("监听到:刷新异常指标事件,开始执行");
        String customerCode = flushWarningMetricsEvent.getMessage();
        processStorageFacadeService.flushWarningMetrics(customerCode);
        log.info("刷新异常指标事件结束:{}", customerCode);
    }
}

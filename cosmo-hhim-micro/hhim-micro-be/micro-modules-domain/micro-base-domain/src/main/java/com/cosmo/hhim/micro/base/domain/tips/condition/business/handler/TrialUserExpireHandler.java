/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * decouple-from-ops-platform-cleanup (A.1): trial 流程已下线，handler 永久 no-op
 *
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/23
 */
@Slf4j
@Component
public class TrialUserExpireHandler extends AbstractBusinessHandler {

    public static final String EXPIRATION_THRESHOLD = "expirationThreshold";

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        // decouple-from-ops-platform-cleanup (A.1): trial 已下线，永远不命中
        log.debug("[deprecated] TrialUserExpireHandler 已 no-op；trial 流程下线");
        return new BusinessHandlerResult();
    }
}

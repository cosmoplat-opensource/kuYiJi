/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.clock.router;

import com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler.ClockTipHandler;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerRouterAutoImpl;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.HandlerRouter;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@HandlerRouterAutoImpl("clockHandlerRouter")
public interface ClockHandlerRouter extends HandlerRouter<ClockTipHandler> {

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.router.importexport;

import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.HandlerRouter;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerRouterAutoImpl;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@HandlerRouterAutoImpl("microImportedHandlerRouter")
public interface MicroImportedHandlerRouter extends HandlerRouter<MicroImportedHandler> {

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.integration;

import com.cosmo.hhim.micro.application.dto.integration.MicroUserEventTrackingDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户行为事件埋点服务
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroUserEventTrackingFacadeService {
    /**
     * 收集用户行为埋点
     *
     * @param trackingDTO
     * @param request
     */
    int collectUserEvent(MicroUserEventTrackingDTO trackingDTO, HttpServletRequest request); 
}

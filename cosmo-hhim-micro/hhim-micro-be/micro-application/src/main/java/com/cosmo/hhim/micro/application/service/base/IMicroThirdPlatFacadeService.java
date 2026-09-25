/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroThirdPlatFacadeService {
    /**
     * 请求三方平台获取app_sign和app_code对应关系
     *
     * @return Map<String, String> app_sign(k)和app_code(v)对应关系
     */
    Map<String, String> cacheMicroApplicationConfig();
}

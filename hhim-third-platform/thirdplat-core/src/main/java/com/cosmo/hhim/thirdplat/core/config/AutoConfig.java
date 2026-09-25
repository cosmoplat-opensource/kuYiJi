/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.core.config;

import org.springframework.context.annotation.Bean;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-04
 */

public class AutoConfig {
    @Bean
    public ForestGlobalConfig forestGlobalConfig() {
        return new ForestGlobalConfig();
    }
}

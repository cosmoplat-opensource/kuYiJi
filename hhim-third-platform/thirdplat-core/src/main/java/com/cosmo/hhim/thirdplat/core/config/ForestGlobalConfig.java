/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
@ConfigurationProperties(prefix = "forest")
public class ForestGlobalConfig {
    private Map<String, String> variables;
}

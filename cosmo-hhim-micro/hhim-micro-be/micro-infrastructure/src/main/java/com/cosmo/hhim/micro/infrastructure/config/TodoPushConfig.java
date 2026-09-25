/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "todopush")
@Data
public class TodoPushConfig { 
    private String appId;
    private String parentId;
    private String appSecret;
    private String gatewayUrl;
    private String pushPath;
    private String finishPath;
}

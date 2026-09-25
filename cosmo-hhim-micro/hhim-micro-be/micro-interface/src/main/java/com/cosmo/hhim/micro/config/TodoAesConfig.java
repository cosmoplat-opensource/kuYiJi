/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.cosmo.hhim.common.core.utils.DecryptUtils;

/**
 * 待办系统 AES 加解密密钥配置（可选）：
 * 仅对接卡奥斯内部待办系统时需要，开源独立部署可留空不配置
 */
@Configuration
public class TodoAesConfig {

    @Value("${todo.aes.key:}")
    public void setKey(String key) {
        DecryptUtils.setKey(key);
    }

    @Value("${todo.aes.iv:}")
    public void setIv(String iv) {
        DecryptUtils.setIv(iv);
    }
}

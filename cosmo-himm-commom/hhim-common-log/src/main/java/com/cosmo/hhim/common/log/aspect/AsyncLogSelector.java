/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.log.aspect;

import com.cosmo.hhim.common.log.config.AsyncLogAutoConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

@Slf4j
public class AsyncLogSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        log.info("importSelector---------->AsyncLog加载中...");
        return new String[]{AsyncLogAutoConfig.class.getName()};
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth;

import com.cosmo.hhim.common.security.config.AccessAuthConfig;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author cosmo-hhim-open Team
 * @description 访问权限控制import selector
 * @createTime 2022-11-06
 */
public class AccessAuthSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{AccessAuthConfig.class.getName()};
    }
}

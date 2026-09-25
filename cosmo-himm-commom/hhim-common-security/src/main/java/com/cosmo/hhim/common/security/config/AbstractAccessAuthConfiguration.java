/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.config;

import com.cosmo.hhim.common.security.annotation.EnableAccessAuth;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author cosmo-hhim-open Team
 * @description 访问权限ImportAware配置类
 * @createTime 2022-11-07
 */
public abstract class AbstractAccessAuthConfiguration implements ImportAware {

    protected AnnotationAttributes annotationAttributes;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        // 通过实现ImportAware，获取EnableAccessAuth注解的属性值信息
        this.annotationAttributes = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableAccessAuth.class.getName(), false));
        if (null == annotationAttributes) {
            throw new IllegalArgumentException("获取EnableAccessAuth注解属性信息失败！");
        }
    }
}

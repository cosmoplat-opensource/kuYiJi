/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept.conf;

import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.EnableContentTip;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public abstract class AbstractContentTipConfiguration implements ImportAware {

    protected AnnotationAttributes enableContentTip;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {

        // 通过实现ImportAware，获取EnableContentTip注解的属性值信息
        this.enableContentTip = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableContentTip.class.getName(), false));
        if (null == enableContentTip) {
            throw new IllegalArgumentException("获取EnableContentTip注解属性信息失败！");
        }
    }
}

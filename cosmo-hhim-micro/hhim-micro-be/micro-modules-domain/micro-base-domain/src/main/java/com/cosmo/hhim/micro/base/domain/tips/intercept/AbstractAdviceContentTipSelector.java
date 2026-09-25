/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public abstract class AbstractAdviceContentTipSelector<T extends Annotation> implements ImportSelector {

    public static final String DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME = "mode";


    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        // 获取子类的注解泛型类型
        Class<?> annType = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractAdviceContentTipSelector.class);

        // 获取注解的属性值
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(annType.getName(), false));
        if (attributes == null) {
            throw new IllegalArgumentException("获取注解属性信息失败！");
        }

        AdviceMode adviceMode = attributes.getEnum(DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME);
        String[] imports = selectImports(adviceMode);
        if (imports == null) {
            throw new IllegalArgumentException("根据切面通知模式获取对应的配置类失败！AdviceMode: " + adviceMode);
        }
        return imports;

    }

    protected abstract String[] selectImports(AdviceMode adviceMode); 
}

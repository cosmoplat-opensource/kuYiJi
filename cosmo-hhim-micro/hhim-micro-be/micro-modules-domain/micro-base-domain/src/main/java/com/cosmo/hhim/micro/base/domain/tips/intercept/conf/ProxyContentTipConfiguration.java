/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept.conf;

import com.cosmo.hhim.micro.base.domain.tips.intercept.ContentTipAnnoBeanPostProcessor;
import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.EnableContentTip;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public class ProxyContentTipConfiguration extends AbstractContentTipConfiguration {

    @Bean
    public ContentTipAnnoBeanPostProcessor contentTipAnnoBeanPostProcessor() {
        ContentTipAnnoBeanPostProcessor beanPostProcessor = new ContentTipAnnoBeanPostProcessor();

        // 设置自定义的内容提示注解类型
        Class<? extends Annotation> customContentTipAnnotation = this.enableContentTip.getClass("annotation");
        if (customContentTipAnnotation != AnnotationUtils.getDefaultValue(EnableContentTip.class, "annotation")) { 
            beanPostProcessor.setContentTipAnnotationType(customContentTipAnnotation);
        }

        return beanPostProcessor;
    }

}

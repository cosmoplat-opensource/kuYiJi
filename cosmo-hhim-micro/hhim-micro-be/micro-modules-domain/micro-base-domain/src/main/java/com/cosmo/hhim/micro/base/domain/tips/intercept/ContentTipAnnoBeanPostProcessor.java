/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept;

import com.cosmo.hhim.micro.base.domain.tips.intercept.advisor.ContentTipAnnotationAdvisor;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.beans.factory.BeanFactory;

import java.lang.annotation.Annotation;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
public class ContentTipAnnoBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    // 内容提示注解类型 
    private Class<? extends Annotation> contentTipAnnotationType;

    public void setContentTipAnnotationType(Class<? extends Annotation> contentTipAnnotationType) {
        this.contentTipAnnotationType = contentTipAnnotationType;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);

        // 构建切面
        ContentTipAnnotationAdvisor advisor = new ContentTipAnnotationAdvisor(beanFactory);
        if (this.contentTipAnnotationType != null) {
            advisor.setCustomContentTipAnnotation(this.contentTipAnnotationType);
        }
//        advisor.setBeanFactory(beanFactory); 
        this.advisor = advisor;
    }
}

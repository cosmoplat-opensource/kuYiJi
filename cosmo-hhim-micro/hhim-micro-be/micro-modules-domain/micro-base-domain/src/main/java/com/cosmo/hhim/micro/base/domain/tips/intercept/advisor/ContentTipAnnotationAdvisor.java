/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.intercept.advisor;

import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.micro.base.domain.tips.TipsManager;
import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.ContentTip;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-08-05
 */
public class ContentTipAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    // 定义切面通知 
    private Advice advice;

    // 定义切点 
    private Pointcut pointcut;

    private BeanFactory beanFactory;

    public ContentTipAnnotationAdvisor(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        Set<Class<? extends Annotation>> contentTipAnnotationTypes = new LinkedHashSet<>(2);
        contentTipAnnotationTypes.add(ContentTip.class);

        // 构建切面通知
        this.advice = buildAdvice();
        // 构建切点
        this.pointcut = buildPointcut(contentTipAnnotationTypes);

        setBeanFactory(beanFactory);
    }

    /**
     * 设置自定义的内容提示注解类型
     *
     * @param contentTipAnnotationType
     */
    public void setCustomContentTipAnnotation(Class<? extends Annotation> contentTipAnnotationType) {
        Set<Class<? extends Annotation>> contentTipAnnotationTypes = new HashSet<>();
        contentTipAnnotationTypes.add(contentTipAnnotationType);
        // 设置了自定义注解类型时，需要重新构建切点
        this.pointcut = buildPointcut(contentTipAnnotationTypes);
    }

    /**
     * 构建切面通知
     *
     * @return
     */
    protected Advice buildAdvice() {
        TipsManager tipsManager = beanFactory.getBean(TipsManager.class);
        RedisLockHelper redisLockHelper = beanFactory.getBean(RedisLockHelper.class);
        return new ContentTipAnnotationInterceptor(tipsManager, redisLockHelper);
    }

    /**
     * 构建切点
     *
     * @return
     */
    protected Pointcut buildPointcut(Set<Class<? extends Annotation>> contentTipAnnotationTypes) {

        // 采用组合切面ComposablePointcut，将下面的类上的注解和方法上的注解组合在一起
        ComposablePointcut result = null;
        for (Class<? extends Annotation> contentTipAnnotationType : contentTipAnnotationTypes) {
            // 类匹配（只要在类上有这个注解，所有的方法都匹配）
            Pointcut cpc = new AnnotationMatchingPointcut(contentTipAnnotationType, true);
            // 方法匹配（方法上有这个注解时，会匹配）
            Pointcut mpc = new AnnotationMatchingPointcut(null, contentTipAnnotationType, true);
            if (result == null) {
                result = new ComposablePointcut(cpc);
            } else {
                result.union(cpc);
            }
            result = result.union(mpc);
        }
        // 如果没有指定注解类型，则匹配所有类的所有方法进行拦截处理
        return (result != null ? result : Pointcut.TRUE);
    }


    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
}

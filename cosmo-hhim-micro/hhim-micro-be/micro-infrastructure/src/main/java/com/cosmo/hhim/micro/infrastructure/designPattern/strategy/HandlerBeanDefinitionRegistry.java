/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy; 

import com.cosmo.hhim.micro.infrastructure.annotation.HandlerRouterAutoImpl;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.proxy.HandlerRouterFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@Slf4j
@Component
public class HandlerBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private static final String PROPERTY_A = "interfaceClazz";
    private static final String PROPERTY_B = "className";
    private static final String PROPERTY_C = "applicationContext";

    private ApplicationContext applicationContext;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        // 1.扫描标注@HandlerRouterAutoImpl的接口
        Set<Class<?>> classes = this.getAutoImplClasses();
        for (Class<?> clazz : classes) {

            // 2.获取继承自HandlerRouter的接口的泛型的类型typeName
            Type[] types = clazz.getGenericInterfaces();
            ParameterizedType type = (ParameterizedType) types[0];
            String typeName = type.getActualTypeArguments()[0].getTypeName();

            // 3.注册FactoryBean来动态生成Bean实例（最终通过JDK动态代理生成实例对象）
            HandlerRouterAutoImpl handlerRouterAutoImpl = clazz.getAnnotation(HandlerRouterAutoImpl.class);

            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HandlerRouterFactoryBean.class);
            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getBeanDefinition();
            definition.getPropertyValues().add(PROPERTY_A, clazz);
            definition.getPropertyValues().add(PROPERTY_B, typeName);
            definition.getPropertyValues().add(PROPERTY_C, applicationContext);
            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

            registry.registerBeanDefinition(handlerRouterAutoImpl.value(), definition);
        }


    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * 通过反射扫描出所有使用HandlerRouterAutoImpl的类
     *
     * @return
     */
    private Set<Class<?>> getAutoImplClasses() {
        Reflections reflections = new Reflections(
                "com.cosmo.hhim.micro.*",
                new TypeAnnotationsScanner(),
                new SubTypesScanner()
        );
        return reflections.getTypesAnnotatedWith(HandlerRouterAutoImpl.class);
    }
}

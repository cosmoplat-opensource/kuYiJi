/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy.proxy; 

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Proxy;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@Data
public class HandlerRouterFactoryBean<T> implements FactoryBean<T> {

    private Class<T> interfaceClazz;
    private String className;
    private ApplicationContext applicationContext;

    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class[]{interfaceClazz},
                new HandlerRouterInvocation(className, applicationContext));
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

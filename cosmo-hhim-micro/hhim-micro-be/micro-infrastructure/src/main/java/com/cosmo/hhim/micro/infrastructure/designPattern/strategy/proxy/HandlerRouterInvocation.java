/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy.proxy; 

import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@Slf4j
public class HandlerRouterInvocation implements InvocationHandler {

    private String className;
    private ApplicationContext applicationContext;

    private final Map<HandlerTypeEnum, Object> handlerMapping = Maps.newHashMap();

    public HandlerRouterInvocation(String className, ApplicationContext applicationContext) {
        this.className = className;
        this.applicationContext = applicationContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (CollectionUtils.isEmpty(handlerMapping)) {
            this.initHandlerMap();
        }

        String type = (String) args[0];
        return handlerMapping.get(HandlerTypeEnum.getEnum(type));
    }

    /**
     * 初始化type->Handler映射关系的Map集合
     *
     * @throws ClassNotFoundException
     */
    private void initHandlerMap() throws ClassNotFoundException {
        //获取Handler接口的所有实现类
        Map<String, ?> classMap = applicationContext.getBeansOfType(Class.forName(className));

        // 解析Handler上的handlerType注解信息，组装成type -> Handler维护到Map中
        for (Map.Entry<String, ?> entry : classMap.entrySet()) {
            HandlerType handlerType = entry.getValue().getClass().getAnnotation(HandlerType.class);
            if (handlerType == null) {
                continue;
            }
            handlerMapping.put(handlerType.type(), entry.getValue());
        }
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.eventbus;


import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author cosmo-hhim-open Team
 * @description 观察者动作
 * @createTime 2021-06-01
 */
public class ObserverAction {
    private static final Logger log = LoggerFactory.getLogger(ObserverAction.class);

    private Object target;
    private Method method;

    public ObserverAction(Object target, Method method){
        this.target = Preconditions.checkNotNull(target);
        this.method = method;
    }

    public void execute(Object event){
        try {
            method.invoke(target, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("ObserverActionError",e);
        }
    }
}

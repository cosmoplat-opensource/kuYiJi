/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.eventbus;

import com.google.common.base.Preconditions;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author cosmo-hhim-open Team
 * @description 观察者注册器
 * @createTime 2021-06-01
 */
public class ObserverRegistry {

    // 订阅者集合
    // 使用CopyOnWriteArraySet的原因：修改时会重新copy一份出来替换原先的，这样在修改过程中不会影响读操作
    private ConcurrentMap<Class<?>, CopyOnWriteArraySet<ObserverAction>> registry = new ConcurrentHashMap<>();

    /**
     * 将订阅者对象中的相关订阅事件的方法加入到订阅者集合中
     * @param observer
     */
    public void register(Object observer) {
        //获取订阅者类中所有的标注了@Subscribe的方法，并包装成ObserverAction对象后加入到集合
        Map<Class<?>, List<ObserverAction>> observerActions = findAllObserverActions(observer);
        for (Map.Entry<Class<?>, List<ObserverAction>> entry : observerActions.entrySet()) {
            Class<?> eventType = entry.getKey();
            List<ObserverAction> eventActions = entry.getValue();
            //判断订阅者集合中是否已经存在该事件的订阅者，如果不存在则新建一个该事件对应的空集合
            CopyOnWriteArraySet<ObserverAction> registeredEventActions = registry.get(eventType);
            if (registeredEventActions == null) {
                registry.putIfAbsent(eventType, new CopyOnWriteArraySet<>());
                registeredEventActions = registry.get(eventType);
            }
            //加入到该事件的订阅者集合中
            registeredEventActions.addAll(eventActions);
        }
    }

    /**
     * 从订阅者集合中获取指定事件的订阅者列表
     * @param event
     * @return
     */
    public List<ObserverAction> getMatchedObserverActions(Object event) {
        List<ObserverAction> matchedObservers = new ArrayList<>();
        Class<?> postEventType = event.getClass();
        for (Map.Entry<Class<?>, CopyOnWriteArraySet<ObserverAction>> entry : registry.entrySet()) {
            Class<?> eventType = entry.getKey();
            Collection<ObserverAction> eventActions = entry.getValue();
            if (postEventType.isAssignableFrom(eventType)) { //判断此postEventType对象所表示的类或接口与指定的eventType参数所表示的类或接口是否相同，或是否是其超类或超接口
                matchedObservers.addAll(eventActions);
            }
        }
        return matchedObservers;
    }

    /**
     * 将订阅者类中所有的订阅方法包装成ObserverAction，并加入到订阅者集合
     *
     * @param observer
     * @return
     */
    private Map<Class<?>, List<ObserverAction>> findAllObserverActions(Object observer) {
        Map<Class<?>, List<ObserverAction>> observerActions = new HashMap<>();
        Class<?> clazz = observer.getClass();
        for (Method method : getAnnotationMethods(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> eventType = parameterTypes[0];
            if (!observerActions.containsKey(eventType)) { //如果参数类型在缓存中不存在则新建对应的List列表，并加入到缓存
                observerActions.put(eventType, new ArrayList<>());
            }

            observerActions.get(eventType).add(new ObserverAction(observer, method));
        }
        return observerActions;
    }

    /**
     * 获取被注解@Subscribe标注的方法的方法信息
     *
     * @param clazz
     * @return
     */
    private List<Method> getAnnotationMethods(Class<?> clazz) {
        List<Method> annotationMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) { //判断方法中是否有@Subscribe的注解
                Class<?>[] parameterTypes = method.getParameterTypes();
                Preconditions.checkArgument(parameterTypes.length == 1,
                        "目前@Subscribe注解标注的订阅者方法：%s 只拥有 %s 个参数，" +
                                "订阅者的方法至少应该有一个参数", method, parameterTypes.length);
                annotationMethods.add(method);
            }
        }
        return annotationMethods;
    }
}

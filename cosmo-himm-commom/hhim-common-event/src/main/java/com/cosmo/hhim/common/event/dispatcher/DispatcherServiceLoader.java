/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cosmo-hhim-open Team
 * @description 事件分派器 SPI加载器
 * @createTime 2022/3/18
 */
public class DispatcherServiceLoader {

    // 缓存类型和该类型实现类的映射关系
    private static final Map<Class<?>, Collection<Class<?>>> SERVICES = new ConcurrentHashMap<>();

    /**
     * 使用JDK SPI机制加载指定的类型接口的实现类
     *
     * @param service
     * @param <T>
     * @return
     */
    public static <T> Collection<T> load(final Class<T> service) {
        if (SERVICES.containsKey(service)) {
            return newServiceInstances(service);
        }
        Collection<T> result = new LinkedHashSet<T>();
        for (T t : ServiceLoader.load(service)) {
            result.add(t);
            cacheServiceClass(service, t);
        }
        return result;
    }

    // 将指定类型的SPI加载结果缓存
    private static <T> void cacheServiceClass(final Class<T> service, final T instance) {
        if (!SERVICES.containsKey(service)) {
            SERVICES.put(service, new LinkedHashSet<Class<?>>());
        }
        SERVICES.get(service).add(instance.getClass());
    }

    /**
     * 缓存存在则实例化
     * @param service
     * @param <T>
     * @return
     */
    public static <T> Collection<T> newServiceInstances(final Class<T> service) {
        return SERVICES.containsKey(service) ? newServiceInstancesFromCache(service) : Collections.<T>emptyList();
    }

    private static <T> Collection<T> newServiceInstancesFromCache(Class<T> service) {
        Collection<T> result = new LinkedHashSet<T>();
        for (Class<?> each : SERVICES.get(service)) {
            result.add((T) newServiceInstance(each));
        }
        return result;
    }

    private static Object newServiceInstance(final Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("通过JDK SPI机制加载类型:%s的接口实现类发生异常:", clazz.getName()), e);
        }
    }
}

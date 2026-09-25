/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.core.cache;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021/12/3
 */
public class ThreadLocalCache {
    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL_CACHE_MAP = new TransmittableThreadLocal<>();


    public static void saveCache(String key, Object obj){
        if (StringUtils.hasText(key)) {
            Map<String, Object> cacheMap = getAllCache();
            if(CollectionUtils.isEmpty(cacheMap)){
                cacheMap = new HashMap<String, Object>();
            }
            cacheMap.put(key, obj);
            saveAllCache(cacheMap);
        }
    }

    public static void saveAllCache(Map<String, Object> map) {
        if (!CollectionUtils.isEmpty(map)) {
            THREAD_LOCAL_CACHE_MAP.set(map);
        }
    }

    public static Map<String, Object> getAllCache() {
        return THREAD_LOCAL_CACHE_MAP.get();
    }

    public static Object getCache(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        if (null == getAllCache()) {
            return null;
        }
        return getAllCache().get(key);
    }

    public static void removeCache(String key) {
        if (StringUtils.hasText(key)) {
            Map<String, Object> cacheMap = getAllCache();
            if (!CollectionUtils.isEmpty(cacheMap)) {
                cacheMap.remove(key);
            }
            if(CollectionUtils.isEmpty(cacheMap)){
                clearCache();
            }
            saveAllCache(cacheMap);
        }
    }

    public static void clearCache() {
        THREAD_LOCAL_CACHE_MAP.remove();
    }

}

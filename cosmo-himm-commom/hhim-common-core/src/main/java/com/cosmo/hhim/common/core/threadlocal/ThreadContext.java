/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.threadlocal;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;


/**
 * 线程上下文
 */
public final class ThreadContext {
    private static final Logger log = LoggerFactory.getLogger(ThreadContext.class);

    private ThreadContext() {

    }

    /**
     * 设置线程中的对象
     *
     * @param key
     * @param value
     */
    public static void put(Object key, Object value) {
        MDC.put(key.toString(), value == null ? "" : value.toString());
    }

    /**
     * 取得线程中的对象
     *
     * @param key
     * @return
     */
    public static Object get(Object key) {
        return MDC.get(key.toString());
    }

    /**
     * 删除线程中的对象
     *
     * @param key
     * @return
     */
    public static void remove(Object key) {
        MDC.remove(key.toString());
    }

    /**
     * 清空线程Map
     *
     * @return
     */
    public static void clear() {
        MDC.clear();
    }


    public static Map<String, String> getThreadMap() {
        Map<String, String> map = new HashMap<>();
        if (CheckObjectUtils.isNotEmpty(MDC.getCopyOfContextMap())){
            map.putAll(MDC.getCopyOfContextMap());
        }
        return map;
    }

    /**
     * 批量添加数据
     *
     * @param p
     * @return
     */
    public static void putAll(Map<String, String> p) {
        if(p!=null){
            log.info("mq消费设置数据源:{}", JSONObject.toJSONString(p));
            for (Object key : p.keySet()) {
                MDC.put(key.toString(), CheckObjectUtils.isNotEmpty(p.get(key)) ? p.get(key).toString() : "");
            }
        }

    }


}

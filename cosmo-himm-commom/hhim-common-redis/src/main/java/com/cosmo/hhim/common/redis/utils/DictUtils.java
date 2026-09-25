/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.core.utils.SpringUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.redis.dto.SysDictData;
import com.cosmo.hhim.common.redis.service.RedisService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典工具类
 *
 * @author cosmo-hhim-open Team
 */
public class DictUtils {
    /**
     * 设置字典缓存
     *
     * @param key       参数键
     * @param dictDatas 字典数据列表
     */
    public static void setDictCache(String key, List<SysDictData> dictDatas) {
        SpringUtils.getBean(RedisService.class).setCacheObject(getCacheKey(key), dictDatas);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictDatas 字典数据列表
     */
    public static List<SysDictData> getDictCache(String key) {
        Object cacheObj = SpringUtils.getBean(RedisService.class).getCacheObject(getCacheKey(key));
        if (StringUtils.isNotNull(cacheObj)) {
            List<SysDictData> dictDatas = StringUtils.cast(cacheObj);
            return dictDatas;
        }
        return null;
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictDatas 字典数据列表
     */
    public static JSONArray getDictCacheNoType(String key) {
        Object cacheObj = SpringUtils.getBean(RedisService.class).getCacheObject(getCacheKey(key));
        if (StringUtils.isNotNull(cacheObj)) {
            JSONArray parseArray = JSONArray.parseArray(JSON.toJSONString(cacheObj));
            return parseArray;
        }
        return null;
    }


    /**
     * 批量获取字典缓存
     *
     * @param keys 参数键
     * @return dictDatas 字典数据列表
     */
    public static Map<String,JSONArray> getDictCacheNoTypeList(List<String> keys) {
        Map<String,JSONArray> result = new HashMap<>();
        keys.stream().forEach(key -> {
            Object cacheObj = SpringUtils.getBean(RedisService.class).getCacheObject(getCacheKey(key));
            if (StringUtils.isNotNull(cacheObj)) {
                JSONArray parseArray = JSONArray.parseArray(JSON.toJSONString(cacheObj));
                result.put(key,parseArray);
            }
        });
        return result;
    }




    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictDatas 字典数据列表
     */
    public static JSONArray getDictCacheNoType(String key,String customCode) {
        Object cacheObj = SpringUtils.getBean(RedisService.class).getCacheObject(getCacheKey(key,customCode));
        if (StringUtils.isNotNull(cacheObj)) {
            JSONArray parseArray = JSONArray.parseArray(JSON.toJSONString(cacheObj));
            return parseArray;
        }
        return null;
    }

    /**
     * 清空字典缓存
     */
    public static void clearDictCache() {
        Collection<String> keys = SpringUtils.getBean(RedisService.class).keys(Constants.SYS_DICT_KEY + "*");
        SpringUtils.getBean(RedisService.class).deleteObject(keys);
    }


    /**
     清除某一个缓存
     */
    public static void clearDictCache(String dictType) {
        SpringUtils.getBean(RedisService.class).deleteObject(getCacheKey(dictType));
    }

    /**
     * 清楚多个缓存
     * @param dictTypes
     */
    public static void clearDictCache(List<String> dictTypes) {
        if(dictTypes!=null&&dictTypes.size()>0){
            SpringUtils.getBean(RedisService.class).deleteObject(dictTypes);
        }
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey) {

        String customCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        return Constants.SYS_DICT_KEY + customCode+ configKey;
    }

    public static String getCacheKey(String configKey,String customCode) {
        return Constants.SYS_DICT_KEY + customCode+ configKey;
    }

    /**
     * 根据字典类型清空字典缓存
     */
    public static void clearDictCacheByCondition(String dicType) {
        Collection<String> keys = SpringUtils.getBean(RedisService.class).keys(Constants.SYS_DICT_KEY + dicType);
        SpringUtils.getBean(RedisService.class).deleteObject(keys);
    }

    /**
     *  根据字典类型，字典值，从缓存信息中解析名称
     **/
    public static String getDictLabel(String key,String val)
    {
        String label="";
        List<SysDictData> dicts = getDictCache(key);
        SysDictData labels = dicts.stream().filter(s -> s.getDictValue().equals(val)).findAny().orElse(null);

        return labels.getDictLabel();
    }

    /**
     * 根据字典类型，字典值解析名称
     **/
    public static String getDictLabel(String key, String val, Map cacheMap)
    {
        if(null==cacheMap|| null==cacheMap.get(key)){
            return val;
        }
        String label="";
        List<SysDictData> dictList= (List<SysDictData>) cacheMap.get(key);
        SysDictData labels = dictList.stream().filter(s -> s.getDictValue().equals(val)).findAny().orElse(null);

        return labels.getDictLabel();
    }
}

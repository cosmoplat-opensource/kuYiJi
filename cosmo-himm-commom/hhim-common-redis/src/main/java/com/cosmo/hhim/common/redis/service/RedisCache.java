/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 *
 * @author cosmo-hhim-open Team
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     * @return 缓存的对象
     */
    public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value);
        return operation;
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     * @return 缓存的对象
     */
    public <T> ValueOperations<String, T> setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        operation.set(key, value, timeout, timeUnit);
        return operation;
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 获取多个key的数据
     *
     * @param keys Redis键
     */
    public <T> List<T> getMultiValue(final Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 塞入多个key的数据
     *
     * @param map 键值对
     */
    public void setMultiValue(final Map map, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().multiSet(map);
        for (Object key : map.keySet()) {
            redisTemplate.expire(key, timeout, timeUnit);
        }
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deleteObject(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection
     */
    public void deleteObject(Collection collection) {
        redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList) {
        ListOperations listOperation = redisTemplate.opsForList();
        if (null != dataList) {
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                listOperation.leftPush(key, dataList.get(i));
            }
        }
        return listOperation;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(String key) {
        List<T> dataList = new ArrayList<T>();
        ListOperations<String, T> listOperation = redisTemplate.opsForList();
        Long size = listOperation.size(key);

        for (int i = 0; i < size; i++) {
            dataList.add(listOperation.index(key, i));
        }
        return dataList;
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(String key) {
        Set<T> dataSet = new HashSet<T>();
        BoundSetOperations<String, T> operation = redisTemplate.boundSetOps(key);
        dataSet = operation.members();
        return dataSet;
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     * @return
     */
    public <T> HashOperations<String, String, T> setCacheMap(String key, Map<String, T> dataMap) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        if (null != dataMap) {
            for (Map.Entry<String, T> entry : dataMap.entrySet()) {
                hashOperations.put(key, entry.getKey(), entry.getValue());
            }
        }
        return hashOperations;
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(String key) {
        Map<String, T> map = redisTemplate.opsForHash().entries(key);
        return map;
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 根据redis获取自增ID
     *
     * @param code 前缀
     * @param step 步长
     * @return 自增ID
     */
    public String incrby(final String code, final int step) {
        final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Object id = redisTemplate.execute((RedisCallback) connection -> {
            long incr = connection.incrBy(dateStr.getBytes(), step);
            if (incr == 1L) {
                connection.expire(dateStr.getBytes(), 86400);
            }
            return incr;
        });
        String idStr = StringUtils.leftPad(String.valueOf(id), 6, "0");
        return String.format("%s%s%s", code, dateStr.substring(2), idStr);
    }

    /**
     * 按天生产不带Code
     *
     * @param code
     * @param step
     * @return
     */
    public String incrByDay(final String code, final int step, int length) {
        final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Object id = redisTemplate.execute((RedisCallback) connection -> {
            long incr = connection.incrBy((code + dateStr).getBytes(), step);
            if (incr == 1L) {
                connection.expire((code + dateStr).getBytes(), 86400);
            }
            return incr;
        });
        String idStr = StringUtils.leftPad(String.valueOf(id), length, "0");
        return String.format("%s%s", dateStr.substring(2), idStr);
    }

    /**
     * 自增code
     *
     * @param code
     * @param step
     * @return
     */
    public String incrCodeByDay(final String code, final int step, int length) {
        final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Object id = redisTemplate.execute((RedisCallback) connection -> {
            long incr = connection.incrBy((code + dateStr).getBytes(), step);
            if (incr == 1L) {
                connection.expire((code + dateStr).getBytes(), 86400);
            }
            return incr;
        });
        String idStr = StringUtils.leftPad(String.valueOf(id), length, "0");
        return String.format("%s%s%s", code, dateStr.substring(2), idStr);
    }

    /**
     * redis模糊查询
     *
     * @return
     */
    public Set<String> scan(String matchKey) {
        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match("*" + matchKey + "*").count(1000).build());
            while (cursor.hasNext()) {
                keysTmp.add(new String(cursor.next()));
            }
            return keysTmp;
        });

        return keys;
    }

    /**
     * redis模糊查询
     *
     * @return
     */
    public Set<String> match(String matchKey) {
        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(matchKey + "*").count(1000).build());
            while (cursor.hasNext()) {
                keysTmp.add(new String(cursor.next()));
            }
            return keysTmp;
        });

        return keys;
    }

    /**
     * 根据redis指定自增ID
     *
     * @param key
     * @param length
     * @param start
     * @param end
     * @return
     */
    public Long incrbySet(String key, int length, int start, int end) {
        Long num = redisTemplate.opsForValue().increment(key, length) + start - 1;
        if (num == end) {
            redisTemplate.delete(key);
            return redisTemplate.opsForValue().increment(key, length) + start - 1;
        } else {
            return num;
        }

    }

    /**
     * 自增code
     *
     * @param code
     * @param step
     * @return
     */
    public String generateSerialNumber(final String code, final int step, int length) {
        Object id = redisTemplate.execute((RedisCallback) connection -> {
            long incr = connection.incrBy((code).getBytes(), step);
            if (incr == 1L) {
                connection.expire((code).getBytes(), 86400);
            }
            return incr;
        });
        String idStr = StringUtils.leftPad(String.valueOf(id), length, "0");
        return idStr;
    }

    /**
     * 修改hash中的字段值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Boolean setHashFiled(String key, String field, Object value) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return false;
        }
        if (!org.springframework.util.StringUtils.hasText(field)) {
            return false;
        }
//        return redisTemplate.opsForHash().putIfAbsent(getCustom(key), field, value);
        redisTemplate.opsForHash().put(key, field, value);
        return true;
    }

    /**
     * 删除hash中的字段
     *
     * @param key
     * @param field
     * @return
     */
    public Boolean delHashField(String key, String... field) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return false;
        }
        if (null == field || field.length == 0) {
            return false;
        }
        redisTemplate.opsForHash().delete(key, field);
        return true;
    }

    /**
     * 获取hash指定field的value
     *
     * @param key
     * @param field
     * @return
     */
    public Object getHashFiled(String key, String field) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return null;
        }
        if (!org.springframework.util.StringUtils.hasText(field)) {
            return null;
        }
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 判断hash指定的field是否存在
     *
     * @param key
     * @param field
     * @return
     */
    public Boolean existsHashKeyField(String key, String field) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return false;
        }
        if (!org.springframework.util.StringUtils.hasText(field)) {
            return false;
        }

        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 增加hash指定field的值
     *
     * @param key
     * @param field
     * @param increment
     * @return
     */
    public Boolean hashValueIncrement(String key, String field, Long increment) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return false;
        }
        if (!org.springframework.util.StringUtils.hasText(field)) {
            return false;
        }
        if (null == increment) {
            return false;
        }
        redisTemplate.opsForHash().increment(key, field, increment);

        return true;
    }

    /**
     * 获取hash指定key的所有数据
     *
     * @param key
     * @return
     */
    public Map<String, Object> getHashAll(String key) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return null;
        }
        return (Map<String, Object>) redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取key的有效剩余时间
     *
     * @param key
     * @return
     */
    public Long getKeyTTL(String key, TimeUnit timeUnit) {
        if (!org.springframework.util.StringUtils.hasText(key) || null == timeUnit) {
            return null;
        }
        return redisTemplate.getExpire(key, timeUnit);
    }


    /**
     * 推送到redis队列
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long pushCacheQueue(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获取队列中多少个元素,但不会删除队列中元素
     *
     * @param key
     * @param nums
     * @param <T>
     * @return
     */
    public <T> List<T> rangeCacheList(final String key, final Integer nums) {
        return redisTemplate.opsForList().range(key, 0, nums);
    }

    /**
     * 取第一个元素并删除
     *
     * @param key
     * @param timeout
     * @param timeUnit
     * @return
     */
    public Object popCacheQueue(final String key, final Integer timeout, TimeUnit timeUnit) {
        return redisTemplate.opsForList().leftPop(key, timeout, timeUnit);
    }

}

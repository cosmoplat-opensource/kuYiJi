/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.service;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheCustomer {

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
        key = getCustom(key);
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
        key = getCustom(key);
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
        key = getCustom(key);
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deleteObject(String key) {
        key = getCustom(key);
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
        key = getCustom(key);
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
        key = getCustom(key);
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
        key = getCustom(key);
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
        key = getCustom(key);
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
        key = getCustom(key);
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
        key = getCustom(key);
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
        pattern = getCustom(pattern);
        return redisTemplate.keys(pattern);
    }

    /**
     * 根据redis获取自增ID
     *
     * @param code 前缀
     * @param step 步长
     * @return 自增ID
     */
    public String incrby(String code, final int step) {
        code = getCustom(code);
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
    public String incrByDay(String code, final int step, int length) {
        code = getCustom(code);
        final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String finalCode = code;
        Object id = redisTemplate.execute((RedisCallback) connection -> {
            long incr = connection.incrBy((finalCode + dateStr).getBytes(), step);
            if (incr == 1L) {
                connection.expire((finalCode + dateStr).getBytes(), 86400);
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
    public String incrCodeByDay(String code, final int step, int length) {
        code = getCustom(code);
        final String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String finalCode = code;
        Object id = redisTemplate.execute((RedisCallback) connection -> {
            long incr = connection.incrBy((finalCode + dateStr).getBytes(), step);
            if (incr == 1L) {
                connection.expire((finalCode + dateStr).getBytes(), 86400);
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
        matchKey = getCustom(matchKey);
        String finalMatchKey = matchKey;
        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match("*" + finalMatchKey + "*").count(1000).build());
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
     * @return 自增ID
     */
    public Long incrbySet(String key, int length, int start, int end) {
        key = getCustom(key);
        Long num = redisTemplate.opsForValue().increment(key, length) + start - 1;
        if (num == end) {
            redisTemplate.delete(key);
            return redisTemplate.opsForValue().increment(key, length) + start - 1;
        } else {
            return num;
        }

    }

    /**
     * 设置key的过期时间
     *
     * @param key
     * @return
     */
    public Boolean expireKey(String key, Long timeouts, TimeUnit unit) {
        if (!org.springframework.util.StringUtils.hasText(key) || null == timeouts || null == unit) {
            return false;
        }
        return redisTemplate.expire(getCustom(key), timeouts, unit);
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public Boolean existsKey(String key) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return false;
        }
        return redisTemplate.hasKey(getCustom(key));
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
        redisTemplate.opsForHash().put(getCustom(key), field, value);
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
        redisTemplate.opsForHash().delete(getCustom(key), field);
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
        return redisTemplate.opsForHash().get(getCustom(key), field);
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

        return redisTemplate.opsForHash().hasKey(getCustom(key), field);
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
        redisTemplate.opsForHash().increment(getCustom(key), field, increment);

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
        return (Map<String, Object>) redisTemplate.opsForHash().entries(getCustom(key));
    }

    /**
     * 新增GEO结构经纬度信息
     *
     * @param key
     * @param longitude
     * @param latitude
     * @param member
     * @return
     */
    public Long geoAdd(String key, Double longitude, Double latitude, Object member) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return 0L;
        }
        if (null == longitude || null == latitude || null == member) {
            return 0L;
        }
        return redisTemplate.opsForGeo().add(getCustom(key), new Point(longitude, latitude), member);
    }

    /**
     * 移除GEO结构中的指定member的经纬度信息
     *
     * @param key
     * @param member
     * @return
     */
    public Long geoRemove(String key, Object member) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return 0L;
        }
        if (null == member) {
            return 0L;
        }
        return redisTemplate.opsForGeo().remove(getCustom(key), member);
    }

    /**
     * 根据给定的经纬度，返回半径不超过指定距离的元素
     *
     * @param key
     * @param longitude
     * @param latitude
     * @param distance
     * @param count
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> nearByXY(String key, Double longitude, Double latitude, Double distance, Metrics metrics, Long count) {
        if (!org.springframework.util.StringUtils.hasText(key)) {
            return null;
        }

        if (null == longitude || null == latitude || null == distance) {
            return null;
        }

        if (null == count) {
            count = 10L;
        }

        if (null == metrics) {
            metrics = Metrics.MILES;
        }

        // includeDistance 包含距离
        // includeCoordinates 包含经纬度
        // sortAscending 正序排序
        // limit 限定返回的记录数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance().includeCoordinates().sortAscending().limit(count);
        Point centerPoint = new Point(longitude, latitude);
        Circle circle = new Circle(centerPoint, new Distance(distance, metrics));
        return redisTemplate.opsForGeo().radius(getCustom(key), circle, args);
    }

    /**
     * 计算指定地点到redis中指定member得距离
     * @param key geoKey
     * @param longitude 指定地点经度
     * @param latitude 指定地点维度
     * @param member redis中指定member
     * @param metrics 距离单位
     * @return
     */
    public Distance getPointToMemberDistance(String key,Double longitude,Double latitude,String member,Metrics metrics){
        Distance distance = new Distance(-1L,metrics);
        //将指定地点信息临时添加到redis中
        Long addResult = this.geoAdd(key,longitude,latitude,"tempPoint");
        if(com.cosmo.hhim.common.core.utils.StringUtils.isNotEmpty(member) && addResult != null && addResult > 0L){
            //计算两个地点的距离信息
            distance = redisTemplate.opsForGeo().distance(getCustom(key), "tempPoint", member, metrics);
            //删除临时地点信息
            this.geoRemove(key,"tempPoint");
        }
        return distance;
    }

    private String getCustom(String key) {
        String custom = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        return key + custom;
    }

    /**
     * 获取key的有效剩余时间
     * @param key
     * @return
     */
    public Long getKeyTTL(String key, TimeUnit timeUnit){
        if (!org.springframework.util.StringUtils.hasText(key) || null == timeUnit) {
            return null;
        }
        return redisTemplate.getExpire(getCustom(key), timeUnit);
    }

}

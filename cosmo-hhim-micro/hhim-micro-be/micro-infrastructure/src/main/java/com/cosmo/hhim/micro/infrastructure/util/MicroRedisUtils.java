/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.HotKeyEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class MicroRedisUtils { 

    @Autowired
    public RedisTemplate redisTemplate;

    /**
 * @author cosmo-hhim-open Team
     * 用户常用的热点数据+1
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @param suffix  后缀 次数/时间序列
     * @param obj     要添加的工序名/产品名等
     */
    public void incrByHotData(HotKeyEnum keyEnum, String suffix, Object obj) {
        redisTemplate.opsForZSet().incrementScore(keyEnum.getCode() + suffix + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId(), obj, 1);
    }

    /**
     * @param obj 要添加的工序名/产品名等
     */
    public void addZset(String key, Object obj, Double score) {
        redisTemplate.opsForZSet().add(key, obj, score);
    }

    /**
     * 用户常用的热点数据时间序列
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @param obj     要添加的工序名/产品名等
     */
    public void addHotData(HotKeyEnum keyEnum, String suffix, Object obj, Double score) {
        redisTemplate.opsForZSet().add(keyEnum.getCode() + suffix + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId(), obj, score);
    }

    /**
     * 移除热点数据
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @param obj     要添加的工序名/产品名等
     */
    public void removeHotData(HotKeyEnum keyEnum, String suffix, Object obj) {
        redisTemplate.opsForZSet().remove(keyEnum.getCode() + suffix + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId(), obj);
    }

    /**
     * 移除热点数据
     *
     * @param key redis的key值,如前工序,当前工序,产品等...
     * @param obj     要添加的工序名/产品名等
     */
    public void removeZset(String key, Object obj) {
        redisTemplate.opsForZSet().remove(key, obj);
    }

    /**
     * 递增
     *
     * @param key
     * @return
     */
    public Long incrBy(String key) {
        return incrBy(key, 1L);
    }

    public Long incrBy(String key, Long step) {
        return redisTemplate.opsForValue().increment(key, step);
    }

    /**
     * 获取用户常用热点数据
     *
     * @param key redis的key值,如前工序,当前工序,产品等...
     * @param start 起始位置
     * @param end 结束位置
     * @return
     */
    public Set<Object> getRank(String key, long start, long end) {
        return redisTemplate.hasKey(key) ? redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1) : Collections.emptySet();
    }

    /**
     * 获取用户常用热点数据
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @return
     */
    public Set<Object> getRankByRange(HotKeyEnum keyEnum, String suffix, long start, long end) {
        String key = keyEnum.getCode() + suffix + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId();
        return this.getRank(key, start, end);
    }

    /**
     * 获取用户常用热点数据
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @return
     */
    public Set<Object> getRankAll(HotKeyEnum keyEnum, String suffix) {
        return this.getRankByRange(keyEnum, suffix, 0, -1);
    }

    /**
     * 获取用户常用热点数据
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @return
     */
    public Set<Object> getRankAndScoreByRange(HotKeyEnum keyEnum, String suffix, long start, long end) {
        String key = keyEnum.getCode() + suffix + ThreadContext.get(Constants.TARGET_CUSTOMER) + CommonConstants.SEPARATOR_SYMBOL + SecurityUtils.getUserId();
        return redisTemplate.hasKey(key) ? redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end) : Collections.emptySet();
    }

    /**
     * 获取用户常用热点数据
     *
     * @param keyEnum redis的key值,如前工序,当前工序,产品等...
     * @return
     */
    public Set<Object> getRankAndScoreAll(HotKeyEnum keyEnum, String suffix) {
        return this.getRankAndScoreByRange(keyEnum, suffix, 0, -1);
    }

    /**
     * 获取用户常用热点数据
     *
     * @param key redis的key值,如前工序,当前工序,产品等...
     * @return
     */
    public Set<String> match(String key) {
        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keysTmp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(key).build());
            while (cursor.hasNext()) {
                keysTmp.add(new String(cursor.next()));
            }
            return keysTmp;
        });
        return keys;
    }

    /**
     * 获取流水号，自增补全后缀
     *
     * @param prefix 前缀
     * @return 对象列表
     */
    public String getSeqNumber(final String prefix) {
        return String.format("%06d", redisTemplate.opsForValue().increment(prefix, 1L));
    }
}
/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.util;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Base64;
import java.util.BitSet;
import java.util.Set;

/**
 * 用户登录统计
 *
 * @author cosmo-hhim-open Team
 */
@Component
public class UserLoginStatisticsUtil {


    private static RedisCache redisCache;

    @Resource
    public void setRedisCache(RedisCache redisCache) {
        UserLoginStatisticsUtil.redisCache = redisCache;
    }

    public static final String DATE_FORMAT_SEPARATOR = "-";
    public static final String REDIS_LOGIN_STATISTICS_KEY = "loginStatistics:";

    /**
     * 添加用户
     *
     * @param userId
     */
    public static void recordUserLogin(Long userId) {
        // 获取当前日期的字符串格式，例如："2023-05-31"
        String currentDate = LocalDate.now().toString().substring(5);
        // 将当前日期的字符串格式转换为数字，例："2023-05-31" -> 531
        long currentDateNumber = Long.parseLong(currentDate.replaceAll(DATE_FORMAT_SEPARATOR, ""));
        String userKey = getUserKey(userId);
        // 使用位图记录用户当天是否登录过
        String str = redisCache.getCacheObject(userKey);
        BitSet userLoginBitSet = new BitSet();
        if (str == null) {
            userLoginBitSet = new BitSet();
        }
        userLoginBitSet.set((int) (currentDateNumber), true);
        String value = Base64.getEncoder().encodeToString(userLoginBitSet.toByteArray());
        redisCache.setCacheObject(userKey, value);
    }

    /**
     * 用户登录天数
     *
     * @param userId
     * @return
     */
    public static int countUserLoginDays(Long userId) {
        // 获取用户的登录位图
        String matchKey = getUserMathKey(userId, "") + CommonConstants.MATCH_SYMBOL;
        Set<String> match = redisCache.match(matchKey);
        int count = 0;
        for (String userKey : match) {
            String str = redisCache.getCacheObject(userKey);
            if (str == null) {
                continue;
            }
            byte[] bytes = Base64.getDecoder().decode(str);
            BitSet userLoginBitSet = BitSet.valueOf(bytes);
            // 统计用户登录天数
            count += userLoginBitSet.cardinality();
        }
        return count;
    }

//    /**
//     * 用户连续登录几天
//     *
//     * @param userName
//     * @return
//     */
//    public static int countUserContinuousLoginDays(Long userId) {
//        // 获取用户的登录位图
//        String userKey = getUserKey(userId);
//        BitSet userLoginBitSet = redisCache.getCacheObject(userKey);
//        if (userLoginBitSet == null) {
//            return 0;
//        }
//        // 统计用户连续登录天数
//        int continuousLoginDays = 0;
//        int maxContinuousLoginDays = 0;
//        for (int i = userLoginBitSet.length() - 1; i >= 0; i--) {
//            if (userLoginBitSet.get(i)) {
//                continuousLoginDays++;
//                if (continuousLoginDays > maxContinuousLoginDays) {
//                    maxContinuousLoginDays = continuousLoginDays;
//                }
//            } else {
//                continuousLoginDays = 0;
//            }
//        }
//        return maxContinuousLoginDays;
//    }

//    /**
//     * 获取最近x天内连续登录y天的活跃用户
//     *
//     * @param recentDays
//     * @param continuousDays
//     * @return
//     */
//    public static Set<String> getActiveUsers(int recentDays, int continuousDays) {
//        // 获取最近x天的日期列表
//        List<LocalDate> recentDateList = new ArrayList<>();
//        for (int i = recentDays - 1; i >= 0; i--) {
//            LocalDate date = LocalDate.now().minusDays(i);
//            recentDateList.add(date);
//        }
//        // 获取最近x天内连续登录y天的活跃用户
//        Set<String> activeUsers = new HashSet<>();
//        String matchKey = getUserKey(null) + CommonConstants.MATCH_SYMBOL;
//        for (String userName : redisCache.match(matchKey)) {
//            BitSet userLoginBitSet = redisCache.getCacheObject(userName);
//            if (userLoginBitSet == null) {
//                continue;
//            }
//            boolean isActiveUser = true;
//            for (LocalDate date : recentDateList) {
//                long dateNumber = Long.parseLong(date.format(DateTimeFormatter.BASIC_ISO_DATE));
//                if (!userLoginBitSet.get((int) (dateNumber - 1))) {
//                    isActiveUser = false;
//                    break;
//                }
//            }
//            if (isActiveUser && countContinuousOnes(userLoginBitSet, continuousDays) >= continuousDays) {
//                activeUsers.add(userName.substring((getUserKey(null)).length()));
//            }
//        }
//        return activeUsers;
//    }

//    private static int countContinuousOnes(BitSet bitSet, int length) {
//        int count = 0;
//        int maxCount = 0;
//        for (int i = bitSet.length() - 1; i >= 0; i--) {
//            if (bitSet.get(i)) {
//                count++;
//                if (count > maxCount && count <= length) {
//                    maxCount = count;
//                }
//            } else {
//                count = 0;
//            }
//        }
//        return maxCount;
//    }

    /**
     * 获取redisKey
     *
     * @param userId
     * @return
     */
    private static String getUserKey(Long userId) {
        return getUserMathKey(userId, String.valueOf(DateUtil.thisYear()));
    }

    /**
     * 获取redisKey
     *
     * @param userId
     * @param thisYear
     * @return
     */
    private static String getUserMathKey(Long userId, String thisYear) {
        String suffix = ThreadContext.get(Constants.TARGET_CUSTOMER).toString() + ":" + userId + ":" + thisYear;
        return CommonConstants.REDIS_KEY_MICRO_PREFIX + REDIS_LOGIN_STATISTICS_KEY + suffix;
    }

}

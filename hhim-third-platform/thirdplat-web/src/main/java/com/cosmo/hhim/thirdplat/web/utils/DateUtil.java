/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.utils;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-30
 */
@Slf4j
public class DateUtil {

    public static final String TIME_FORMAT_A = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT_B = "yyyyMMdd";
    public static final String TIME_FORMAT_C = "HH:mm:ss";
    public static final String TIME_FORMAT_D = "HH:mm";
    public static final String TIME_FORMAT_E = "yyyy-MM-dd";

    /**
     * 将时间点转为当前日期的时间
     *
     * @param time 格式：HH:mm
     * @return
     */
    public static LocalDateTime timeToCurrentDate(String time) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.atTime(LocalTime.parse(time));
    }

    /**
     * 将时间点转为当前日期的时间戳
     *
     * @param time 格式：HH:mm
     * @return
     */
    public static long timeToCurrentDateTimestamp(String time) {
        LocalDate currentDate = LocalDate.now();
        return currentDate.atTime(LocalTime.parse(time)).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 将时间点转为指定日期的时间戳
     *
     * @param time      格式：HH:mm
     * @param localDate 日期
     * @return
     */
    public static long timeToCurrentDateTimestamp(LocalDate localDate, String time) {
        return localDate.atTime(LocalTime.parse(time)).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 将时间点转为指定日期的时间
     *
     * @param time      格式：HH:mm
     * @param localDate 日期
     * @return
     */
    public static LocalDateTime timeToLocalDateTime(LocalDate localDate, String time) {
        return localDate.atTime(LocalTime.parse(time));
    }

    /**
     * 将时间点转为指定日期的时间
     *
     * @param localDate
     * @param time
     * @return
     */
    public static Date timeToDate(LocalDate localDate, String time) {
        return localDateTimeToDate(localDate.atTime(LocalTime.parse(time)));
    }

    /**
     * 将时间点转为指定日期的时间
     *
     * @param time      格式：HH:mm:ss
     * @param localDate 日期
     * @return
     */
    public static LocalDateTime dateTimeToDate(LocalDate localDate, String time) {
        return localDate.atTime(LocalTime.parse(time));
    }

    /**
     * 将时间点转为指定日期的时间戳
     *
     * @param time      格式：HH:mm
     * @param localDate 日期
     * @return
     */
    public static long timeToDateTimestamp(LocalDate localDate, String time) {
        return localDate.atTime(LocalTime.parse(time)).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 将String类型的时间转为时间戳
     *
     * @param date       string类型时间
     * @param timeFormat 时间格式 默认：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long strToTimestamp(String date, String timeFormat) {
        if (!StringUtils.hasText(timeFormat)) {
            timeFormat = "yyyy-MM-dd HH:mm:ss";
        }
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(timeFormat)).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 将String类型的时间格式转化
     *
     * @param date
     * @param oldFormat
     * @param newFormat
     * @return
     */
    public static String strDateFormatChange(String date, String oldFormat, String newFormat) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(oldFormat))
                .format(DateTimeFormatter.ofPattern(newFormat));
    }

    /**
     * 获取指定格式当前时间
     *
     * @param dateFormat
     * @return
     */
    public static String getCurrentDate(String dateFormat) {
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = DateUtil.TIME_FORMAT_B;
        }
        return LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
    }

    /**
     * 将时间戳转为指定格式的String时间
     *
     * @param timestamp
     * @param dateFormat
     * @return
     */
    public static String timestampToFormatStr(Long timestamp, String dateFormat) {
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = DateUtil.TIME_FORMAT_B;
        }
        if (DateUtil.TIME_FORMAT_A.equals(dateFormat)) {
            return timestampToLocalDateTime(timestamp).format(DateTimeFormatter.ofPattern(dateFormat));
        }
        return timestampToLocalDate(timestamp).format(DateTimeFormatter.ofPattern(dateFormat));
    }

    /**
     * 将Date类型转为指定格式的Str
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static String dateToFormatStr(Date date, String dateFormat) {
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = DateUtil.TIME_FORMAT_A;
        }

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(dateFormat));
    }


    /**
     * 时间戳转LocalDate
     *
     * @param timestamp
     * @return
     */
    public static LocalDate timestampToLocalDate(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.of("+8")).toLocalDate();
    }

    /**
     * date 转 LocalDate
     *
     * @param date
     * @return
     */
    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDate();
    }

    /**
     * date 转 LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
    }

    /**
     * 将指定格式的String类型日期转为LocalDate
     *
     * @param dateStr
     * @param dateFormat
     * @return
     */
    public static LocalDate strToLocalDate(String dateStr, String dateFormat) {
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = DateUtil.TIME_FORMAT_B;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(dateFormat));
    }

    /**
     * 时间戳转LocalDateTime
     *
     * @param timestamp
     * @return
     */
    public static LocalDateTime timestampToLocalDateTime(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.of("+8")).toLocalDateTime();
    }

    /**
     * 时间戳转Date
     *
     * @param timestamp
     * @return
     */
    public static Date timestampToDate(Long timestamp) {
        return new Date(timestamp);
    }

    /**
     * localDateTime转Date
     *
     * @param localDateTime
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.of("+8")).toInstant());
    }

    /**
     * localDate转Date
     *
     * @param localDate
     * @return
     */
    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.of("+8")));
    }

    /**
     * 增加或减少日期
     *
     * @param localDateTime
     * @param plusValue
     * @param unit
     * @return
     */
    public static LocalDateTime plus(LocalDateTime localDateTime, long plusValue, ChronoUnit unit) {
        return localDateTime.plus(plusValue, unit);
    }


    /**
     * 增加或减少日期
     *
     * @param localDate
     * @param plusValue
     * @param unit
     * @return
     */
    public static LocalDate plus(LocalDate localDate, long plusValue, ChronoUnit unit) {
        return localDate.plus(plusValue, unit);
    }

    /**
     * 增加或减少日期
     *
     * @param date
     * @param plusValue
     * @param unit
     * @return
     */
    public static Date plus(Date date, long plusValue, ChronoUnit unit) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneOffset.of("+8")).toLocalDateTime();
        LocalDateTime plus = plus(localDateTime, plusValue, unit);
        return Date.from(plus.atZone(ZoneOffset.of("+8")).toInstant());
    }

    /**
     * 增加或减少日期
     *
     * @param timestamp
     * @param plusValue
     * @param unit
     * @return
     */
    public static Long plus(Long timestamp, long plusValue, ChronoUnit unit) {
        LocalDateTime localDateTime = timestampToLocalDateTime(timestamp);
        LocalDateTime plus = plus(localDateTime, plusValue, unit);
        return plus.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 获取两个时间戳之前所有的日期，并按照指定格式格式化为String集合（按天间隔）
     *
     * @param startTimestamp
     * @param endTimestamp
     * @param dateFormat
     * @return
     */
    public static List<String> getBetweenTimestampAllDateStr(Long startTimestamp, Long endTimestamp, String dateFormat) {
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = DateUtil.TIME_FORMAT_B;
        }

        LocalDate startLocalDate = timestampToLocalDate(startTimestamp);
        LocalDate endLocalDate = timestampToLocalDate(endTimestamp);

        List<String> result = Lists.newArrayList();
        while (endLocalDate.isAfter(startLocalDate)) {
            result.add(startLocalDate.format(DateTimeFormatter.ofPattern(dateFormat)));

            startLocalDate = plus(startLocalDate, 1L, ChronoUnit.DAYS);
        }
        result.add(endLocalDate.format(DateTimeFormatter.ofPattern(dateFormat)));
        return result;
    }

    /**
     * 计算两个日期的时间差
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static long calcBetweenDate(Date startDate, Date endDate, ChronoUnit chronoUnit) {
        LocalDateTime startLocalDateTime = dateToLocalDateTime(startDate);
        LocalDateTime endLocalDateTime = dateToLocalDateTime(endDate);

        return chronoUnit.between(startLocalDateTime, endLocalDateTime);
    }

    /**
     * 获取两个日期范围内的所有的日期
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> genAllDateFromBeginDateToEndDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 声明保存日期集合
        List<String> list = Lists.newArrayList();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.DATE, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            log.error("Failed to parse date :{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
        return list;
    }

    public static Date getDayBeginTime(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }


    public static Date getDayEndTime(final Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }


    public static void main(String[] args) {
        long startTimestamp = 1649386462832L; // 2022-04-08 10:54:22:832
        long endTimestamp = 1650020062832L; // 2022-04-15 18:54:22:832

//        List<String> allDateStr = getBetweenTimestampAllDateStr(startTimestamp, endTimestamp, DateUtil.TIME_FORMAT_B);
//        allDateStr.forEach(System.out::println);

//        String s = timestampToFormatStr(startTimestamp, DateUtil.TIME_FORMAT_A);
//        System.out.println(s);

        Date nowDate = new Date();
        String time = dateToFormatStr(nowDate, TIME_FORMAT_D);
//        System.out.println(time);
    }

}

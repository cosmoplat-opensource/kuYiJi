/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.cosmo.hhim.common.core.exception.CustomException;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;


/**
 * 时间工具类
 *
 * @author cosmo-hhim-open Team
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
    public static final String YYYY = "yyyy";

    public static final String YYYY_MM = "yyyy-MM";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String YYYYMMDD = "yyyyMMdd";

    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000L * 24L * 60L * 60L;
        long nh = 1000L * 60L * 60L;
        long nm = 1000L * 60L;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    public static Date getNextWeekDay(Date date, int weekDay) {
        weekDay = weekDay == 7 ? 1 : weekDay + 1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天 (周日:1,周一:2,...,周六7)
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return addDays(date, weekDay > day ? weekDay - day : 7 + weekDay - day);
    }

    public static Date getNextMonthDay(Date date, int monthDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个月的第几天
        int day = cal.get(Calendar.DAY_OF_MONTH);
        for (cal.set(Calendar.DAY_OF_MONTH, 1); ; cal.add(Calendar.MONTH, 1)) {
            // 当月最大天数小于设定值 || 当前月天>=设定值 => 跳至下一月
            if (cal.getActualMaximum(Calendar.DAY_OF_MONTH) < monthDay || monthDay <= day) {
                day = 0;
                continue;
            }
            cal.set(Calendar.DAY_OF_MONTH, monthDay);
            return cal.getTime();
        }
    }

    // 获取周几  (周日:1,周一:2,...,周六7)
    public static int getWeekDay(Calendar cal) {
        // 获得当前日期是一个星期的第几天 (周日:1,周一:2,...,周六7)
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    // 获取几号
    public static int getMonthDay(Calendar cal) {
        // 获得当前日期是一个月的第几天
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    // 获取年周
    public static int getYearWeek(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return year * 100 + week;
    }

    // 获取年月
    public static int getYearMonth(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return year * 100 + month;
    }

    // 获取年季度
    public static int getYearQuarter(Calendar cal) {
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        return year * 100 + (month / 3 + 1);
    }

    // 获取上周的WeekOfYear
    public static int getLastWeekYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int year = cal.get(Calendar.YEAR);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        return year * 100 + week;
    }

    // 获取上月的MonthOfYear
    public static int getLastMonthYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return year * 100 + month;
    }

    // 判断是否为当前季度第一天
    public static boolean ifFirstDayOfQuarter(Calendar cal) {
        int month = cal.get(Calendar.MONTH);
        return (month % 3) == 0 && getMonthDay(cal) == 1;
    }

    /**
     * 当前季度的开始时间
     *
     * @return
     */

    public static Date getFirstDayOfQuarter(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int currentMonth = c.get(Calendar.MONTH) + 1;
        Date dt = null;
        try {
            if (currentMonth <= 3) {
                c.set(Calendar.MONTH, 0);
            } else if (currentMonth <= 6) {
                c.set(Calendar.MONTH, 3);
            } else if (currentMonth <= 9) {
                c.set(Calendar.MONTH, 6);
            } else if (currentMonth <= 12) {
                c.set(Calendar.MONTH, 9);
            }
            c.set(Calendar.DATE, 1);
            dt = DateUtils.dateTime(DateUtils.YYYY_MM_DD,DateUtils.dateTime(c.getTime()));
        } catch (Exception e) {

        }
        return dt;
    }

    /**
     * 获取当前日期后几个月的日期
     * @param startDate
     * @param month
     * @return
     */
    public static Date getMonthDate(Date startDate,int month){
        LocalDateTime localDateTime = startDate.toInstant()
                .atZone(ZoneId.systemDefault() )
                .toLocalDateTime().plusMonths(month);
        Date date = Date.from(localDateTime.atZone( ZoneId.systemDefault()).toInstant());
        return date;
    }

    /**
     *判断是否当前年月
     * @param dataMonth
     * @return
     */
    public static Boolean isNowMonth(String dataMonth) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH )+1;
        String nowMonth=year+"-"+(month<10?"0"+ month:""+month);
        return nowMonth.equals(dataMonth);
    }


    /**
     * 获取指定年月的第一天
     * @param year
     * @param month
     * @return
     */
    public static String getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month-1);
        //获取某月最小天数
        int firstDay = cal.getMinimum(Calendar.DATE);
        //设置日历中月份的最小天数
        cal.set(Calendar.DAY_OF_MONTH,firstDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 获取指定年月的最后一天
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month-1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DATE);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 返回指定日期的指定时间
     * @param date 日期
     * @param time 时间 格式 8:00
     * @return
     */
    public static Date getDateAppointTime(Date date, Time time){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY,time.toLocalTime().getHour());
        cal.set(Calendar.MINUTE,time.toLocalTime().getMinute());
        cal.set(Calendar.SECOND,0);
        return cal.getTime();
    }
    /**
     * 计算时间差
     *
     * @param endDate 最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }


    /**
     * 获取上一天，不指定开始时间就是当前时间的上一天
     *
     * @return
     */
    public static String getPreDay(Date date, String format) {
        Calendar cal = Calendar.getInstance();
        if (CheckObjectUtils.isNotEmpty(date)) {
            cal.setTime(date);
        }
        //设置年份
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat(CheckObjectUtils.isNotEmpty(format) ? format : "yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    /**
     * 获取下一天，不指定开始时间就是当前时间的下一天
     *
     * @return
     */
    public static String getNexDay(Date date, String format) {
        Calendar cal = Calendar.getInstance();
        if (CheckObjectUtils.isNotEmpty(date)) {
            cal.setTime(date);
        }
        //设置年份
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat(CheckObjectUtils.isNotEmpty(format) ? format : "yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }


    /**
     * 比较两个String的时间
     *
     * @return
     */
    public static boolean compareToString(String date1, String date2) {
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1Pars = sdf.parse(date1);
            Date date2Pars = sdf.parse(date2);
            return (date1Pars.compareTo(date2Pars) > 0);
        } catch (Exception e) {
            throw new RuntimeException("时间比较出错");
        }
    }


}

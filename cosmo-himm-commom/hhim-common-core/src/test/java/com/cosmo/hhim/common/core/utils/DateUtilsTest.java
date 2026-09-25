/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

/**
 * DateUtils 时间工具类单元测试
 */
public class DateUtilsTest {

    @Test
    public void testGetNowDate() {
        Date now = DateUtils.getNowDate();
        assertNotNull(now);
        long diff = System.currentTimeMillis() - now.getTime();
        assertTrue(diff < 1000);
    }

    @Test
    public void testGetDate() {
        String date = DateUtils.getDate();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testGetTime() {
        String time = DateUtils.getTime();
        assertNotNull(time);
        assertTrue(time.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testDateTimeNow() {
        String dt = DateUtils.dateTimeNow();
        assertNotNull(dt);
        assertEquals(14, dt.length());
    }

    @Test
    public void testDateTimeNowWithFormat() {
        String dt = DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD);
        assertTrue(dt.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testDateTime() {
        Date date = new Date();
        String result = DateUtils.dateTime(date);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testParseDateToStr() {
        Date date = new Date(0); // epoch
        String result = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
        // format will depend on timezone, so just verify it's non-null
        assertNotNull(result);
    }

    @Test
    public void testDateTimeParse() {
        Date date = DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, "2024-01-15 10:30:00");
        assertNotNull(date);
    }

    @Test(expected = RuntimeException.class)
    public void testDateTimeParse_invalid() {
        DateUtils.dateTime(DateUtils.YYYY_MM_DD, "invalid-date");
    }

    @Test
    public void testDatePath() {
        String path = DateUtils.datePath();
        assertNotNull(path);
        assertTrue(path.contains("/"));
    }

    @Test
    public void testParseDate_null() {
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    public void testParseDate_valid() {
        Date date = DateUtils.parseDate("2024-01-15");
        assertNotNull(date);
    }

    @Test
    public void testParseDate_invalid() {
        Date date = DateUtils.parseDate("not-a-date");
        assertNull(date);
    }

    @Test
    public void testGetServerStartDate() {
        Date startDate = DateUtils.getServerStartDate();
        assertNotNull(startDate);
        assertTrue(startDate.getTime() <= System.currentTimeMillis());
    }

    @Test
    public void testGetDatePoor() {
        Date end = new Date(1000 * 60 * 60 * 24 * 2 + 1000 * 60 * 60 * 3 + 1000 * 60 * 30);
        Date start = new Date(0);
        String poor = DateUtils.getDatePoor(end, start);
        assertNotNull(poor);
        assertTrue(poor.contains("天"));
        assertTrue(poor.contains("小时"));
        assertTrue(poor.contains("分钟"));
    }

    @Test
    public void testTimeDistance() {
        Date end = new Date(1000 * 60 * 60 * 24);
        Date start = new Date(0);
        String distance = DateUtils.timeDistance(end, start);
        assertNotNull(distance);
        assertTrue(distance.contains("天"));
    }

    @Test
    public void testGetFirstDayOfMonth() {
        String firstDay = DateUtils.getFirstDayOfMonth(2024, 6);
        assertEquals("2024-06-01", firstDay);
    }

    @Test
    public void testGetLastDayOfMonth() {
        String lastDay = DateUtils.getLastDayOfMonth(2024, 6);
        assertEquals("2024-06-30", lastDay);
    }

    @Test
    public void testGetLastDayOfMonth_february() {
        String lastDay = DateUtils.getLastDayOfMonth(2024, 2);
        assertEquals("2024-02-29", lastDay);
    }

    @Test
    public void testIsNowMonth_true() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        String nowMonth = year + "-" + (month < 10 ? "0" + month : "" + month);
        assertTrue(DateUtils.isNowMonth(nowMonth));
    }

    @Test
    public void testIsNowMonth_false() {
        assertFalse(DateUtils.isNowMonth("2020-01"));
    }

    @Test
    public void testGetMonthDate() {
        Date startDate = new Date(0);
        Date result = DateUtils.getMonthDate(startDate, 1);
        assertNotNull(result);
        assertTrue(result.getTime() > startDate.getTime());
    }

    @Test
    public void testCompareToString() {
        assertTrue(DateUtils.compareToString("2024-06-08", "2024-06-01"));
        assertFalse(DateUtils.compareToString("2024-06-01", "2024-06-08"));
    }

    @Test(expected = RuntimeException.class)
    public void testCompareToString_invalid() {
        DateUtils.compareToString("invalid", "2024-06-01");
    }

    @Test
    public void testGetPreDay() {
        String preDay = DateUtils.getPreDay(null, null);
        assertNotNull(preDay);
    }

    @Test
    public void testGetNexDay() {
        String nexDay = DateUtils.getNexDay(null, null);
        assertNotNull(nexDay);
    }
}

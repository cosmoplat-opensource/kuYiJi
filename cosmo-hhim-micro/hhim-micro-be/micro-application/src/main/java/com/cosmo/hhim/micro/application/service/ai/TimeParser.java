/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 问数 · 时间解析器（统一收归代码，LLM/规则都只做意图、不碰日期）
 *
 * <p>口语时间词 → 时间语义 + 日期范围（yyyy-MM-dd）。无匹配时置空，
 * 由执行层回退"默认本月"。避免 LLM 输出时间不可信导致查询错误。
 *
 * @author cosmo-hhim-open Team
 */
@Component
public class TimeParser {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 完整日期：2026-08-13 */
    private static final Pattern FULL_DATE = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
    /** 中文日期：8月13日 / 8 月 13 号（容忍空格） */
    private static final Pattern CN_DATE = Pattern.compile("(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*[日号]");

    /**
     * 依据问题文本解析时间，覆盖传入意图的时间字段（幂等）
     */
    public void apply(String question, AskIntentResult r) {
        String q = question == null ? "" : question.trim();
        LocalDate today = LocalDate.now();

        // 具体日期优先于周期词："8月13日"/"2026-08-13" → 单日（用户问单日产量，不能被"上个月"整月覆盖）
        if (applyExactDate(q, today, r)) {
            return;
        }
        // 裸月份表达："8月"/"8月份"/"九月份" → 该自然月（年份取最近一个不晚于今天的同月）
        // （之前不认这种表达，被判为"未含时间词"→ 回退本月，导致"8月"被算成 9 月）
        if (applyBareMonth(q, today, r)) {
            return;
        }
        // 上上月（必须优先于"上个月"——"上上个月"包含子串"上个月"，否则误命中）
        if (containsAny(q, "上上个月", "上上月", "大上个月")) {
            LocalDate first = today.minusMonths(2).withDayOfMonth(1);
            r.setTimeType("custom");
            r.setStartDate(fmt(first));
            r.setEndDate(fmt(first.withDayOfMonth(first.lengthOfMonth())));
            return;
        }
        // 最近 N 天（近三天/最近3天/N天内/这几天）
        if (applyRecentDays(q, today, r)) {
            return;
        }
        if (containsAny(q, "今天", "当日") || containsAny(q, "昨天", "昨日")) {
            r.setTimeType("custom");
            r.setStartDate(fmt(today));
            r.setEndDate(fmt(today));
        } else if (containsAny(q, "上星期", "上周")) {
            LocalDate monday = today.minusWeeks(1).with(DayOfWeek.MONDAY);
            r.setTimeType("custom");
            r.setStartDate(fmt(monday));
            r.setEndDate(fmt(monday.plusDays(6)));
        } else if (containsAny(q, "本星期", "这周", "本周")) {
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            r.setTimeType("custom");
            r.setStartDate(fmt(monday));
            r.setEndDate(fmt(today));
        } else if (containsAny(q, "上个月", "上月")) {
            LocalDate first = today.minusMonths(1).withDayOfMonth(1);
            r.setTimeType("custom");
            r.setStartDate(fmt(first));
            r.setEndDate(fmt(first.withDayOfMonth(first.lengthOfMonth())));
        } else if (containsAny(q, "这个月", "本月", "当月")) {
            LocalDate first = today.withDayOfMonth(1);
            r.setTimeType("month");
            r.setStartDate(fmt(first));
            r.setEndDate(fmt(first.withDayOfMonth(first.lengthOfMonth())));
        } else if (containsAny(q, "今年", "本年")) {
            r.setTimeType("year");
            r.setStartDate(fmt(today.withDayOfYear(1)));
            r.setEndDate(fmt(today.withDayOfYear(today.lengthOfYear())));
        } else {
            r.setTimeType(null);
            r.setStartDate(null);
            r.setEndDate(null);
        }
    }

    /** 最近 N 天：近三天/最近3天/近3日/这几天/近几天 → custom（今天往前 N-1 天，含今天）；支持汉字数字 */
    private boolean applyRecentDays(String q, LocalDate today, AskIntentResult r) {
        boolean hit = q.contains("最近") || q.contains("近") || q.contains("这");
        if (!hit) {
            return false;
        }
        int n = 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("([0-9]{1,3})[天日](内)?").matcher(q);
        if (m.find()) {
            n = Integer.parseInt(m.group(1));
        } else {
            n = cnDayOf(q);
        }
        if (n <= 0 && (q.contains("这几天") || q.contains("近几天"))) {
            n = 3;
        }
        if (n <= 0) {
            return false;
        }
        r.setTimeType("custom");
        r.setStartDate(fmt(today.minusDays(n - 1)));
        r.setEndDate(fmt(today));
        return true;
    }

    /**
     * 裸月份表达："8月" / "8月份" / "九月份" → 解析为该自然月。
     * <p>年份规则：取「最近一个不晚于今天的同月」——例如今天 2026-09-20 时，
     * "8月"→2026-08、"12月"→2025-12、"9月"→2026-09（当月）。
     * <p>注意：已在别处处理过的"上个月/本月/8月13日"不会走到这里（前置分支已 return）。
     */
    private boolean applyBareMonth(String q, LocalDate today, AskIntentResult r) {
        if (q == null || q.trim().isEmpty()) {
            return false;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?<![0-9])([0-9]{1,2}|[一二三四五六七八九十]{1,3})\\s*月(份)?(?![0-9日号])")
                .matcher(q);
        if (!m.find()) {
            return false;
        }
        int month = cnMonthToInt(m.group(1));
        if (month < 1 || month > 12) {
            return false;
        }
        int year = today.getYear();
        if (month > today.getMonthValue()) {
            year = year - 1;   // 说"12月"而当前是 9 月 → 指去年 12 月
        }
        LocalDate first = LocalDate.of(year, month, 1);
        r.setTimeType("custom");
        r.setStartDate(fmt(first));
        r.setEndDate(fmt(first.withDayOfMonth(first.lengthOfMonth())));
        return true;
    }

    /** 中文月份数字（八=8 / 十二=12 / 十=10）→ int；纯数字原样解析（失败 -1） */
    private int cnMonthToInt(String s) {
        if (s == null) {
            return -1;
        }
        String t = s.trim();
        if (t.matches("\\d{1,2}")) {
            return Integer.parseInt(t);
        }
        String digits = "零一二三四五六七八九";
        if ("十".equals(t)) {
            return 10;
        }
        if (t.startsWith("十")) {
            int u = t.length() > 1 ? digits.indexOf(t.charAt(1)) : 0;
            return 10 + (u < 0 ? 0 : u);
        }
        if (t.endsWith("十")) {
            int tens = digits.indexOf(t.charAt(0));
            return (tens < 0 ? 0 : tens) * 10;
        }
        int idx = t.indexOf('十');
        if (idx > 0 && idx + 1 < t.length()) {
            int tens = digits.indexOf(t.charAt(0));
            int unit = digits.indexOf(t.charAt(idx + 1));
            return (tens < 0 ? 0 : tens) * 10 + (unit < 0 ? 0 : unit);
        }
        int v = digits.indexOf(t.charAt(0));
        return v < 0 ? -1 : v;
    }

    /** 汉字天数：一=1 两/二=2 三=3 … 十=10（未命中返回 0） */
    private int cnDayOf(String q) {        String[] words = {"一", "两", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        int[] vals = {1, 2, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int i = 0; i < words.length; i++) {
            if (q.contains(words[i] + "天") || q.contains(words[i] + "日")) {
                return vals[i];
            }
        }
        return 0;
    }

    /**
     * 解析具体日期：中文字日期（8月13日/9月19号）按**当前年**解释。
     *
     * <p>只有"明显指向过去"时才回退一年：与今天相差超过 {@link #FUTURE_TOLERANCE_DAYS} 天的未来日期，
     * 视为用户说的是去年的同一天（如九月问"12月31日产量"）。
     * 历史问题：原来"晚于今天就回退一年"，导致九月问"9月19号"被算成去年同一天 → 查不到数据。
     */
    private boolean applyExactDate(String q, LocalDate today, AskIntentResult r) {
        Matcher m = FULL_DATE.matcher(q);
        if (m.find()) {
            try {
                LocalDate d = LocalDate.of(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)));
                setSingleDay(d, r);
                return true;
            } catch (Exception ignore) {
            }
        }
        m = CN_DATE.matcher(q);
        if (m.find()) {
            try {
                LocalDate d = LocalDate.of(today.getYear(), Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)));
                if (d.isAfter(today.plusDays(FUTURE_TOLERANCE_DAYS))) {
                    d = d.minusYears(1);
                }
                setSingleDay(d, r);
                return true;
            } catch (Exception ignore) {
            }
        }
        return false;
    }

    /** 未来容忍天数：超过这个天数才认为用户说的是去年（当月/近几天的日期一律按当年解释） */
    private static final int FUTURE_TOLERANCE_DAYS = 7;

    private void setSingleDay(LocalDate d, AskIntentResult r) {
        r.setTimeType("custom");
        r.setStartDate(fmt(d));
        r.setEndDate(fmt(d));
    }

    private boolean containsAny(String s, String... keys) {
        for (String k : keys) {
            if (s.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private String fmt(LocalDate d) {
        return d.format(FMT);
    }
}

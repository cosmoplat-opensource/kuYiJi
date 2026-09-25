/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.agent;

import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Agent 循环 · 空数据话术生成器
 *
 * <p>查询无数据时依据实际情况给出引导性回答（不干巴巴一句"暂无相关数据"）：
 * 1) 指定实体 → 提示该实体在时间段内无数据；
 * 2) 已知最近报工日 → 给出可查看的时间点；
 * 3) 从未有报工 → 业务引导文案。
 *
 * @author cosmo-hhim-open Team
 */
@Component
public class EmptyDataAnswer {

    /**
     * 生成空数据话术（时间范围明确展示 + 未审核报工弥合）
     *
     * @param intent      查询意图（实体）
     * @param latestDay   最近一次已审报工日（yyyy-MM-dd 或 null）
     * @param queryParams 实际查询参数（含 startDate/endDate，展示具体时间范围）
     * @param pending     未审核报工汇总（passNum/ngNum/totalNum；null=无）
     */
    public String build(AskIntentResult intent, String latestDay, java.util.Map<String, String> queryParams,
                        java.util.Map<String, Object> pending) {
        StringBuilder sb = new StringBuilder();

        String entity = firstEntity(intent);
        if (StringUtils.hasText(entity)) {
            sb.append("「").append(entity).append("」");
        }
        String range = describeRange(queryParams);
        if (StringUtils.hasText(range)) {
            sb.append("在 ").append(range).append(" 暂无已审核报工数据。");
        } else {
            sb.append("当前暂无已审核报工数据。");
        }

        // 口径弥合：有未审核报工时明确告知（用户视角"报了工"与统计"已审"的断层，全局生效）
        if (pending != null && pending.get("totalNum") instanceof Number
                && ((Number) pending.get("totalNum")).longValue() > 0) {
            sb.append("另有 ").append(numOf(pending.get("totalNum"))).append(" 件未审核报工")
                    .append("（良品 ").append(numOf(pending.get("passNum"))).append(" 件），审核后计入统计。");
        } else if (StringUtils.hasText(latestDay)) {
            sb.append("最近一次已审报工是 ").append(latestDay.substring(0, Math.min(10, latestDay.length())))
                    .append("，可尝试查看该时间段的数据。");
        } else {
            sb.append("录入报工后即可查询。");
        }
        return sb.toString();
    }

    private long numOf(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : 0;
    }

    /** 严格标准日期格式，否则原样返回（防御 Date.toString 等脏值） */
    private static final java.util.regex.Pattern ISO_DATE =
            java.util.regex.Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");

    /** 把查询参数中的日期转成用户可读的时间范围描述：[2026-08-01~2026-08-31] → "2026年8月"；单日 → "2026年8月13日" */
    private String describeRange(java.util.Map<String, String> params) {
        if (params == null) {
            return null;
        }
        String start = params.get("startDate");
        String end = params.get("endDate");
        if (!StringUtils.hasText(start) && !StringUtils.hasText(end)) {
            return null;
        }
        boolean startOk = StringUtils.hasText(start) && ISO_DATE.matcher(start).find();
        boolean endOk = StringUtils.hasText(end) && ISO_DATE.matcher(end).find();
        if (!startOk && !endOk) {
            return null;
        }
        if (startOk && endOk && start.equals(end)) {
            String[] p = start.split("-");
            return p[0] + "年" + Integer.parseInt(p[1]) + "月" + Integer.parseInt(p[2]) + "日";
        }
        if (startOk && endOk && start.substring(0, 7).equals(end.substring(0, 7))) {
            String ym = start.substring(0, 4) + "年" + Integer.parseInt(start.substring(5, 7)) + "月";
            return ym + "（" + start.substring(5) + " ~ " + end.substring(5) + "）";
        }
        return (startOk ? start : "?") + " ~ " + (endOk ? end : "?");
    }

    private String firstEntity(AskIntentResult intent) {
        for (Map.Entry<String, String> e : intent.getEntities().entrySet()) {
            if (StringUtils.hasText(e.getValue())) {
                return e.getValue();
            }
        }
        return null;
    }
}

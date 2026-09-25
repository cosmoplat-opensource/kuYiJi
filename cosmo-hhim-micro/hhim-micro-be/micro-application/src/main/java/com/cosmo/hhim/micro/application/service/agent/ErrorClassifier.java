/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.agent;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Agent 循环 · 错误分类器（可注入规则表）
 *
 * <p>登记实现场景的错误分类：确定性错误（登记缺陷/业务确认不可修）→ 直接诊断，
 * 不进入重规划；可修复错误（参数/组合/超时）→ 重规划（受预算约束）。
 *
 * @author cosmo-hhim-open Team
 */
@Component
public class ErrorClassifier {

    /** 确定性错误类别（不重试） */
    public static final String FATAL = "FATAL";
    /** 可修复错误类别（重规划，受预算） */
    public static final String FIXABLE = "FIXABLE";
    /** 未覆盖组合 */
    public static final String UNSUPPORTED = "UNSUPPORTED";
    /** 未知 */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Pattern[] FATAL_PATTERNS = {
            Pattern.compile("Unknown column", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Unknown table|doesn't exist", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Access denied|无权限|权限不足", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Table .* doesn't exist", Pattern.CASE_INSENSITIVE),
    };

    /** 分类：返回 category 与 tip（诊断/重规划文案） */
    public static Classified classify(String error) {
        if (error == null) {
            return new Classified(UNKNOWN, "未知错误");
        }
        for (Pattern p : FATAL_PATTERNS) {
            if (p.matcher(error).find()) {
                return new Classified(FATAL, "数据/配置问题（登记实现需修复）：" + error);
            }
        }
        if (error.contains("暂不支持") || error.contains("未登记")) {
            return new Classified(UNSUPPORTED, "该组合暂未登记，建议分步提问");
        }
        if (error.contains("服务繁忙") || error.contains("超时") || error.contains("执行异常") || error.contains("时间")) {
            return new Classified(FIXABLE, "执行可修复：" + error);
        }
        return new Classified(UNKNOWN, "未知错误：" + error);
    }

    public static class Classified {
        public final String category;
        public final String tip;

        public Classified(String category, String tip) {
            this.category = category;
            this.tip = tip;
        }
    }
}

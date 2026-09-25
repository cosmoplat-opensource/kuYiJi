/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.agent;

import lombok.Data;

/**
 * Agent 循环 · 反思决策（一级规则版）
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class ReflectDecision {

    /** 判定：PASS / NEEDS_REPLAN / NEEDS_CLARIFY / STOP（确定性错误，不重试） */
    private String judgment;

    /** 决策原因（活动日志/诊断文案） */
    private String reason = "";

    /** 是否确定性错误（STOP 时=true） */
    private boolean fatal;

    /**
     * 确定性错误的类别（可选）：UNSUPPORTED=未登记组合/未登记指标 → 用户话术走"还没学会+能问什么"，
     * 其他（FATAL 等）走中性话术、技术细节只进日志。取值与 {@code ErrorClassifier} 对齐。
     */
    private String category;

    public static final String PASS = "PASS";
    public static final String NEEDS_REPLAN = "NEEDS_REPLAN";
    public static final String NEEDS_CLARIFY = "NEEDS_CLARIFY";
    public static final String STOP = "STOP";

    /** 未覆盖类别（与 ErrorClassifier.UNSUPPORTED 同值） */
    public static final String CATEGORY_UNSUPPORTED = "UNSUPPORTED";

    public static ReflectDecision pass(String reason) {
        ReflectDecision d = new ReflectDecision();
        d.setJudgment(PASS);
        d.setReason(reason);
        return d;
    }

    public static ReflectDecision replan(String reason) {
        ReflectDecision d = new ReflectDecision();
        d.setJudgment(NEEDS_REPLAN);
        d.setReason(reason);
        return d;
    }

    public static ReflectDecision clarify(String reason) {
        ReflectDecision d = new ReflectDecision();
        d.setJudgment(NEEDS_CLARIFY);
        d.setReason(reason);
        return d;
    }

    public static ReflectDecision stop(String reason) {
        ReflectDecision d = new ReflectDecision();
        d.setJudgment(STOP);
        d.setReason(reason);
        d.setFatal(true);
        return d;
    }

    /** STOP + 未覆盖类别（未登记组合/未登记指标：给"能问什么"的引导话术，不是系统诊断） */
    public static ReflectDecision stopUnsupported(String reason) {
        ReflectDecision d = stop(reason);
        d.setCategory(CATEGORY_UNSUPPORTED);
        return d;
    }
}

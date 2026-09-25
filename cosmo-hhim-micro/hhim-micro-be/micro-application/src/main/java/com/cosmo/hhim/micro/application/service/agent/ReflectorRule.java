/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.agent;

import com.cosmo.hhim.micro.application.dto.agent.ExecutionResult;
import com.cosmo.hhim.micro.application.dto.agent.ReflectDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Agent 循环 · 反思器（一级：纯规则，无 LLM）
 *
 * <p>只读元信息判定（数据不透明）：
 * 1. 全部失败 → 错误分类：确定性 → STOP（诊断，不重试）；可修复 → REPLAN；
 * 2. 无数据（全部成功但 0 行）→ PASS（登记实现下空数据多为合法答案，输出"暂无相关数据"）；
 * 3. 有数据 → PASS（有数据即输出）。
 *
 * <p>二级 LLM 审视（P1 接入）：基于"问题+计划摘要+行数+样例"判断数据合理/执行正确/回答覆盖。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class ReflectorRule {

    public ReflectDecision judge(ExecutionResult exec, String question) {
        if (exec == null || exec.getSteps().isEmpty()) {
            return ReflectDecision.replan("无可执行步骤");
        }
        // 全部失败 → 错误分类
        if (exec.allFailed()) {
            ErrorClassifier.Classified c = ErrorClassifier.classify(exec.firstError());
            log.info("[Agent/Reflect] 全失败: category={} reason={}", c.category, exec.firstError());
            if (ErrorClassifier.FATAL.equals(c.category)) {
                return ReflectDecision.stop(c.tip);
            }
            if (ErrorClassifier.UNSUPPORTED.equals(c.category)) {
                // 未登记组合/未登记指标：确定性不可答，话术由编排层补成"能问什么"（不是系统诊断）
                return ReflectDecision.stopUnsupported(c.tip);
            }
            return ReflectDecision.replan(c.tip);
        }
        // 有数据（任一成功且行数>0）→ 有数据即输出
        if (exec.hasData()) {
            return ReflectDecision.pass("有数据");
        }
        // 全部成功但 0 行 → 合法空数据（登记实现口径）
        return ReflectDecision.pass("无数据（合法空结果）");
    }
}

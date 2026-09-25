/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.agent;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 循环 · 执行结果（归一化元信息）
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class ExecutionResult {

    /** 各步骤结果 */
    private List<StepResult> steps = new ArrayList<>();

    /** 是否有数据（任一成功步骤行数 > 0） */
    public boolean hasData() {
        for (StepResult s : steps) {
            if (s.isSuccess() && s.getRowCount() > 0) {
                return true;
            }
        }
        return false;
    }

    /** 是否全部失败 */
    public boolean allFailed() {
        return !steps.isEmpty() && steps.stream().noneMatch(StepResult::isSuccess);
    }

    /** 首个失败原因 */
    public String firstError() {
        for (StepResult s : steps) {
            if (!s.isSuccess()) {
                return s.getError();
            }
        }
        return "";
    }
}

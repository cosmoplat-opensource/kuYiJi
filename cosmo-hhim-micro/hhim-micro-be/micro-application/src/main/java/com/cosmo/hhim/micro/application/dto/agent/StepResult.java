/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.agent;

import lombok.Data;

import java.util.Map;

/**
 * Agent 循环 · 单步执行结果（元信息，数据对循环不透明）
 *
 * <p>循环只读 success/error/rowCount/metric/rows 投影行数；
 * 具体数据（rows）只供答案/反思样本阶段使用。
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class StepResult {

    /** 是否成功 */
    private boolean success;

    /** 失败原因（供错误分类；成功为空） */
    private String error = "";

    /** 返回行数 */
    private int rowCount;

    /** 指标编码（PRODUCT_PASS_RATE 等） */
    private String metric;

    /** 投影后样例行（前 N 行；仅答案/反思样本使用，循环决策不读内容） */
    private java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();

    public StepResult() {
    }

    public StepResult(boolean success, String error, int rowCount, String metric) {
        this.success = success;
        this.error = error;
        this.rowCount = rowCount;
        this.metric = metric;
    }
}

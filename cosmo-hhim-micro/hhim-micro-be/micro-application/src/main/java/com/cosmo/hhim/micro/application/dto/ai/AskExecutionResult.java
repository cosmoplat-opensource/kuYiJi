/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.application.dto.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 指标执行结果（接口返回 → 投影后的行数据）
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class AskExecutionResult {

    /** 指标编码 */
    private String metricCode;

    /** 登记接口路径（证据展示） */
    private String api;

    /** 实际调用参数（证据展示） */
    private Map<String, String> params = new LinkedHashMap<>();

    /** 投影后的行数据（Top N，字段已裁剪为可读 key） */
    private List<Map<String, Object>> rows = new ArrayList<>();

    /** 行总数（未截断前） */
    private Integer total;

    /**
     * 本次实际取到的记录数（未做 Top-N 裁剪前的行数）。
     *
     * <p>与 {@link #rows} 的区别：rows 是给答案用的投影（最多 limit 条），
     * 这个是"库里到底有没有数据"的真实条数 —— 依据卡片要靠它区分
     * 「取到 0 条」与「取到 20 条只展示 3 条」。
     */
    private Integer resultCount;

    /**
     * 本次结果**按哪个度量排序**（执行器投影时写入，如 passRate / totalNum / passNum / ngNum）。
     *
     * <p>用途：上层做"极值投影"（问「哪天良品率最低」只保留极值那一行）时必须知道度量是哪个字段 ——
     * 靠"取行内第一个数字"会取错列（实测把"良品率最低"按 passNum 取成了产量最少的那天 ✗）。
     */
    private String sortKey;

    /**
     * 投影时**算出来的派生度量**（如按天良品率的 passRate），行下标 → {度量名: 值}。
     *
     * <p>为什么要单独带出来：这类值是查询执行器在投影阶段算的（不在 SQL 行里、也不在 rows 的字段里），
     * 上层"极值投影"（问「哪天良品率最低」）如果按 rows 里的字段取极值，就会取错列
     * （实测按 passNum 取成"产量最少的那天"✗）。
     */
    private List<Map<String, Object>> derived = new ArrayList<>();

    /** 执行失败信息（非空表示失败，调用方给"服务繁忙"话术） */
    private String error;
    /** 执行备注（诊断用：为什么没进入取数 / 依据来源等，面向排查不进答案） */
    private String note;

    /**
     * 执行器回填的「依据」（指标口径 / 数据来源 / 结果快照）。
     *
     * <p>由 {@code IntentExecutor} 统一组装：登记指标走的是既有接口与登记 SQL，
     * 答案投影形状与依据快照形状可能不同，集中在这里产出才能保证"答案里的每个数，
     * 依据卡片里都核得到"。合成层（AnswerComposer/AgentLoopService）只在取不到时兜底。
     */
    private Map<String, Object> evidence;
}

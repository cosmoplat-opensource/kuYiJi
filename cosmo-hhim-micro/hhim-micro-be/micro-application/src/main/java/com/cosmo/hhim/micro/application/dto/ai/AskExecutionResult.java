/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
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

    /** 执行失败信息（非空表示失败，调用方给"服务繁忙"话术） */
    private String error;
}

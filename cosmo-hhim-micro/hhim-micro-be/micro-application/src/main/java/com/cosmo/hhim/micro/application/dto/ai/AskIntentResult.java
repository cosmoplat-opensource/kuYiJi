/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 意图解析结果
 *
 * <p>意图解析（规则版/LLM 版）的统一产物；时间与 limit 等已由代码规范化，
 * 实体仍为"候选名"，由执行层（EntityResolver）按租户字典校验。
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class AskIntentResult {

    /** 意图编码（本体 metrics.code；NOT_SUPPORTED=超范围） */
    private String intent;

    /** 分组粒度（组合式能力）：null / day / product / process / employee / ngType（须在指标 dims 白名单内） */
    private String groupBy;

    /** 指标内语义模式（如 DELIVERY_RISK 的 soon=近期交付 / overdue=延期） */
    private String mode;

    /** 统计口径（第一性）：production=已审产出（默认）| submission=含未审核报工行为 */
    private String statScope = "production";

    /** NOT_SUPPORTED 时的原因（LLM 结合上下文语义判断，如"话题超范围/信息不足"） */
    private String notSupportedReason;

    /** NOT_SUPPORTED 时的建议（可问的示例或补全提示） */
    private String notSupportedHint;

    /** 实体候选（productNameOrCode / processNameOrCode / employeeName / ngType） */
    private Map<String, String> entities = new HashMap<>();

    /**
     * 因字典未命中而被丢弃的实体（如"法兰盘"没找到）。
     *
     * <p>非空 = 本次查询是"全量口径"，必须在答案里明确披露、且**不做 LLM 润色**
     * （否则润色会把问题里的实体名和未过滤的数字拼成一句假事实）。
     */
    private String droppedEntity;

    /** 被丢弃实体所属槽位（productNameOrCode / processNameOrCode / employeeName），用于按维度措辞披露 */
    private String droppedEntityField;

    /** 时间语义：today | month | year | custom | null（默认本月） */
    private String timeType;

    /** 时间换算结果（yyyy-MM-dd，代码计算） */
    private String startDate;

    private String endDate;

    /** 排序字段（如 passRate / submitNum，可为空） */
    private String orderBy;

    /** asc | desc */
    private String orderDir = "asc";

    /** 返回条数上限（默认 3，≤10） */
    private Integer limit = 3;

    /** 歧义澄清（实体指代不清时） */
    private String clarifyQuestion;

    private List<String> clarifyOptions = new ArrayList<>();

    /** 解析来源：LLM | RULE | NONE */
    private String source;

    /**
     * 问题性质（LLM 的**语义判断**，不是关键词命中），只有两种：
     * biz=与本系统业务数据相关（要数据/事实，含口语化、省略、指代）；
     * non_biz=非业务问题（文本创作/翻译/闲聊/主观评价…，即使句中出现业务词）。
     * 只有 biz 才进入能力匹配；non_biz 直接给普通指引性回答（详见 AgentLoopService）。
     */
    private String task;

    /**
     * 用户原问题（解析后原样挂在意图上，供执行器按措辞做确定性分支，例如库存：在制 vs 成品）。
     * 只读语义：链路中不得改写。
     */
    private String question;

    /** LLM 原始输出（审计/调试） */
    private String rawLlm;
}

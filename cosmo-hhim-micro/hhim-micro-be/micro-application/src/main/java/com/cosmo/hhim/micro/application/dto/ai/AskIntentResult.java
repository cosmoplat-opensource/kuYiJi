/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
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

    /**
     * 这一项**想要什么**（人话，如「今天报工总数与良品率」）——来自 needs[] 的 want。
     *
     * <p>用途：合并骨架时写段头、依据里说明"这段数据是为什么查的"。
     * 旧格式（intents[]）没有该字段，为空即可（执行层不得依赖它做分支）。
     */
    private String want;

    /**
     * 取数方式（needs[] 的 how）：operator / registered / self / compute。
     *
     * <p>与 {@link #route} 的关系：how 是新契约（四态），route 是旧契约（registered/self/both）。
     * 解析时把 route 映射成 how（self→self，其余→registered）；执行层统一只认 how。
     */
    private String how;

    /**
     * 路由决策（框架 v2 · 判断驱动）：
     * registered=用登记能力（权威口径）/ self=自己生成 SQL / both=两者都用。
     * 由 LLM 对照能力的「★能力规格」按六条判据判断"这份已登记的 SQL 是否满足回答需求"。
     */
    private String route;

    /** 路由理由（尤其"放弃登记能力自己生成"时必须写明哪条判据不满足，供审计与依据展示） */
    private String routeReason;

    /** 限定条件（"某个产品/某道工序/某个员工"），与 entities 分开，便于执行层直接落到 SQL 的 WHERE */
    private java.util.Map<String, String> filter = new java.util.HashMap<>();

    /** 依赖的前置诉求下标（如"他的良品率"依赖第 0 个诉求定出的实体） */
    private java.util.List<Integer> dependsOn = new java.util.ArrayList<>();

    /**
     * 一句多诉求时，"主诉求"之外的其余诉求（能力 code，最多 2 个）。
     * <p>语义判断允许一句话包含多个诉求（如"最低的产品是什么？为什么？"= 数值 + 原因），
     * 主诉求驱动主流程，其余用于补答或提示 —— 不静默丢弃。
     */
    private java.util.List<String> extraIntents = new java.util.ArrayList<>();

    /**
     * 其余诉求的**完整槽位**（复用本类承载每个诉求：intent/groupBy/entities/time/order/statScope）。
     * <p>上层据此逐个执行（每个诉求走同一条闸门+通道），最后把数据拼成**一段输出**。
     */
    private java.util.List<AskIntentResult> extraSlots = new java.util.ArrayList<>();

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

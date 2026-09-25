/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.agent;

import com.cosmo.hhim.micro.application.dto.ai.AskExecutionResult;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.application.dto.ai.AskReplyVO;
import com.cosmo.hhim.micro.application.dto.agent.ExecutionResult;
import com.cosmo.hhim.micro.application.dto.agent.ReflectDecision;
import com.cosmo.hhim.micro.application.dto.agent.StepResult;
import com.cosmo.hhim.micro.application.service.ai.AnswerComposer;
import com.cosmo.hhim.micro.application.service.ai.AnswerGenerator;
import com.cosmo.hhim.micro.application.service.ai.ChatSessionService;
import com.cosmo.hhim.micro.application.service.ai.EntityResolver;
import com.cosmo.hhim.micro.application.service.ai.IntentExecutor;
import com.cosmo.hhim.micro.application.service.ai.LlmIntentResolver;
import com.cosmo.hhim.micro.application.service.ai.OntologyService;
import com.cosmo.hhim.micro.application.service.ai.RuleIntentResolver;
import com.cosmo.hhim.micro.application.service.ai.TimeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agent 循环 · 核心编排（Plan-Act-Reflect-（Replan））
 *
 * <p>P0 骨架：Router 分流 → 计划（意图解析，LLM 优先/规则兜底）→ 代码归一（时间/实体/组合白名单）
 * → Act（登记实现执行，归一化元信息，数据不透明）→ Reflect 一级规则 → 决策：
 * PASS 输出 / CLARIFY 澄清 / REPLAN 规则重试（预算内）/ STOP 确定性诊断。
 * 四道死循环闸：①失败指标跟踪 ②确定性错误不重试 ③全失败无数据停 ④有数据即输出。
 *
 * <p>P1 增强点（预留）：LlmReflector 二级审视、Evaluator、LlmReplan、
 * 通道感知预算（registry=0 / plan=2 / free=3）、token 预算、Observer 活动日志落库。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentLoopService {

    /**
     * 问数链路版本标记（每次改链路就+1）——启动时打印。
     *
     * <p>用途：排查"改了没生效"时，**一眼确认跑的是不是最新代码**，不用再靠行为反推。
     * 历史教训：多次出现"日志里看不到新分支"，最后都是运行的是旧 jar。
     */
    public static final String CHAIN_VERSION = "2026-09-18.6（泛问库存含在制+润色禁'暂无'误述）";

    /** 启动横幅：把关键开关与版本打出来 */
    @javax.annotation.PostConstruct
    public void logStartupBanner() {
        log.info("[AI启动] 问数链路版本={} ｜ 能力分流：登记指标/分析算子/生成SQL ｜ "
                + "问题性质判定=biz|non_biz（LLM 语义判定，独立调用）", CHAIN_VERSION);
    }

    /** 重规划次数上限（全局硬上限默认 5；P0 规则重试取 1，P1 按通道扩展） */
    private static final int MAX_REPLAN_ROUNDS = 5;
    /** P0 规则重试上限（P1 改为通道感知：min(channel+1, 全局)） */
    private static final int RULE_REPLAN_BUDGET = 1;

    private final LlmIntentResolver llmIntentResolver;
    private final RuleIntentResolver ruleIntentResolver;
    private final TimeParser timeParser;
    private final EntityResolver entityResolver;
    private final IntentExecutor intentExecutor;
    private final AnswerComposer answerComposer;
    private final AnswerGenerator answerGenerator;
    private final ChatSessionService chatSessionService;
    private final OntologyService ontologyService;
    private final AgentRouter agentRouter;
    private final ReflectorRule reflectorRule;
    private final EmptyDataAnswer emptyDataAnswer;
    private final com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDailyMapper dailyMapper;
    /** 分析层：确定性取数（登记聚合 SQL）+ 归因计算（MiniAnalyst），回答"为什么"类问题 */
    private final com.cosmo.hhim.micro.application.service.ai.analysis.AnalysisService analysisService;
    /** 通道 C：LLM 生成 SQL（过 SqlGuard 闸 + 审计 + 只读执行）—— 本体覆盖不到时的兜底 */
    private final com.cosmo.hhim.micro.application.service.ai.sql.GeneratedSqlChannel generatedSqlChannel;
    /** 能力命中校验（唯一决策点）：指标/算子/未命中 → 三通道分流 */
    private final com.cosmo.hhim.micro.application.service.ai.CapabilityGate capabilityGate;

    /**
     * 问数主入口（Router + 核心循环 + 输出）
     */
    /**
     * 入口：多问句分流 + 单问句处理。
     *
     * <p>一句话里塞多个问题（"今天产量怎么样 / 各个工序的返修率排名"）时，按"**一次只答一个**"处理：
     * 只答第一个子问题，并在答案末尾提示分开问。这样行为可预期，也避免"只满足了后半句"这种随机结果。
     */
    public AskReplyVO run(String question, String sessionId) {
        String[] split = splitMultiAsk(question);
        if (split[1] == null) {
            return runOne(question, sessionId);
        }
        log.info("[AI决策] 检测到多问句 → 只回答第一个：「{}」", split[0]);
        AskReplyVO vo = runOne(split[0], sessionId);
        if (StringUtils.hasText(vo.getAnswer())) {
            vo.setAnswer(vo.getAnswer() + "\n\n（你一次问了多个问题，我先回答第一个；其余请分开问。）");
        }
        return vo;
    }

    /** 问句分隔后的"像一个问题"的判定标记：两侧都要有这些词才算两个问题，避免误切单问句 */
    private static final String[] ASK_MARKERS = {
            "?", "？", "多少", "几个", "几件", "几条", "哪", "怎么样", "如何", "吗", "呢",
            "排名", "对比", "最高", "最低", "为什么", "有没有", "是不是", "降了", "涨了", "情况"};

    /** 多问句切分：**不再只答第一个** —— 用户一次问了多个业务问题，就把整句交给下游逐条回答
     *  （生成 SQL 通道最多 3 条 SQL，可分别覆盖各子问题；润色提示词也要求逐条作答）。 */
    private String[] splitMultiAsk(String question) {
        return new String[]{question, null};
    }

    private boolean looksLikeAsk(String segment) {
        if (segment.length() < 3) {
            return false;
        }
        for (String marker : ASK_MARKERS) {
            if (segment.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private AskReplyVO runOne(String question, String sessionId) {
        long t0 = System.currentTimeMillis();
        question = clean(question);

        // ── Router（确定性，无 LLM）──
        String route = agentRouter.route(question);
        if (AgentRouter.BOUNDARY.equals(route)) {
            AskReplyVO vo = boundaryReply();
            vo.setElapsedMs(System.currentTimeMillis() - t0);
            persist(sessionId, question, vo);
            return vo;
        }
        if (AgentRouter.SMALLTALK.equals(route)) {
            AskReplyVO vo = simpleReply("您好，我是 Ku易记 问数助手，可以问我产量、良品率、库存、延期等问题。",
                    "NOT_SUPPORTED", "NONE");
            vo.setElapsedMs(System.currentTimeMillis() - t0);
            persist(sessionId, question, vo);
            return vo;
        }

        // 输入判断到此结束：只有"敏感拒绝"（Router 的 FORBIDDEN）与"空/超长"（Router 的 BOUNDARY）两类硬拦截。
        // 不再有"创作类""归因类"专项规则：它们走正常链路，命中不了任何能力 → 生成 SQL 也取不到 → 普通指引性回答。
        // （历史：semanticGuard 曾在这里拦"为什么/写诗"，导致下游新增的分析能力永远到不了，已删除）

        // ── 计划（Plan）：意图解析（LLM 优先，带对话上下文；LLM 判定追问/新话题）→ 规则兜底 + 代码归一 ──
        AskIntentResult intent = resolve(question, chatSessionService.recentContext(sessionId, 4));
        intent.setQuestion(question);   // 原问题随意图透传（执行器按措辞做确定性分支，如库存：在制 vs 成品）
        timeParser.apply(question, intent);
        applyDefaultTimeRange(intent);
        validateGroupBy(intent);
        normalizeOrder(question, intent);
        normalizeDeliveryMode(question, intent);
        if (!"NOT_SUPPORTED".equals(intent.getIntent())) {
            try {
                intent = handleEntities(intent);
            } catch (Exception e) {
                // 实体查证异常兜底：降级全量查询，不阻断回答
                log.warn("[Agent/实体] 查证异常，降级全量: {}", e.getMessage());
            }
        }
        // ── 问题性质（语义判断，非关键词）：只有"与本系统业务数据相关"的问题才进入能力匹配 ──
        // 两态：biz=业务问题；non_biz=非业务（创作/翻译/闲聊/主观评价…）。
        // 依据用户意图而非字面命中：「写一首关于产量的诗」含业务词但不是业务问题 → 普通指引性回答，
        // 不选能力、不生成 SQL（历史 bug：被硬套成 SUMMARY，还真的用数据写了一首诗）。
        if (StringUtils.hasText(intent.getTask()) && !"biz".equals(intent.getTask())) {
            log.info("[AI决策] task={} → 非业务问题，直接普通指引性回答", intent.getTask());
            AskReplyVO vo = boundaryReply(intent);
            vo.setElapsedMs(System.currentTimeMillis() - t0);
            persist(sessionId, question, vo, intent);
            return vo;
        }

        // ── 能力命中校验（唯一决策点，三通道共用）──
        // LLM 只负责"选"（指标或算子），这里负责"验"：槽位白名单 / 指标词一致 / 维度登记。
        // 不合格不再事后纠正，而是直接降级为 GENERATED —— 未登记的东西只有一条归宿。
        com.cosmo.hhim.micro.application.service.ai.CapabilityGate.Decision decision =
                capabilityGate.decide(question, intent);
        log.info("[AI决策] mode={} capability={} ｜ {}", decision.getMode(),
                decision.getCapability(), decision.getReason());
        // 把"闸门最终判定"写回意图对象：元数据/审计要记**最终走哪条通道**，
        // 而不是模型初选的能力（初选可能已被闸门否决，记它会误导排查）
        intent.setMode(String.valueOf(decision.getMode()));

        if (com.cosmo.hhim.micro.application.service.ai.CapabilityGate.Mode.ANALYSIS == decision.getMode()) {
            // 通道 B：命中分析算子（归因/对比/集中度）→ 算子计算（确定性、可对账）
            AskReplyVO vo = analysisReply(question, intent, decision.getCapability());
            if (vo != null) {
                vo.setElapsedMs(System.currentTimeMillis() - t0);
                persist(sessionId, question, vo, intent);
                return vo;
            }
            // 算子组织不起来（数据不足/区间为空）→ **真的**转生成 SQL 通道，成功就返回；
            // 都失败才给普通指引性回答。
            // 历史 bug：这里只打了日志没真调用，导致流程掉进登记循环 → "该指标暂未支持执行器: ATTRIBUTION"。
            log.info("[AI决策] 算子未能组织结论 → 转生成 SQL 通道");
            AskReplyVO fallbackGenerated = generatedSqlReply(question, intent, sessionId);
            AskReplyVO vos = fallbackGenerated != null ? fallbackGenerated : boundaryReply(intent);
            vos.setElapsedMs(System.currentTimeMillis() - t0);
            persist(sessionId, question, vos, intent);
            return vos;
        } else if (com.cosmo.hhim.micro.application.service.ai.CapabilityGate.Mode.GENERATED == decision.getMode()) {
            // 通道 C：未命中任何已登记能力 → LLM 生成 SQL 取数（探索性）；失败才走普通指引性回答
            AskReplyVO generated = generatedSqlReply(question, intent, sessionId);
            if (generated != null) {
                generated.setElapsedMs(System.currentTimeMillis() - t0);
                persist(sessionId, question, generated, intent);
                return generated;
            }
            AskReplyVO vo = boundaryReply(intent);
            vo.setElapsedMs(System.currentTimeMillis() - t0);
            persist(sessionId, question, vo);
            return vo;
        }
        if (intent.getClarifyQuestion() != null) {
            AskReplyVO vo = clarifyReply(intent);
            vo.setElapsedMs(System.currentTimeMillis() - t0);
            persist(sessionId, question, vo);
            return vo;
        }

        // ── 核心循环（Act → Reflect → 决策）──
        int replan = 0;
        AskExecutionResult exec = null;
        while (replan <= MAX_REPLAN_ROUNDS) {
            // Act：登记实现执行 → 归一化（数据对循环不透明）
            exec = intentExecutor.execute(intent.getIntent(), intent);
            ExecutionResult meta = normalize(exec);
            logAgent(replan, intent, meta);

            // Reflect 一级（规则，无 LLM）
            ReflectDecision judge = reflectorRule.judge(meta, question);

            if (ReflectDecision.PASS.equals(judge.getJudgment())) {
                break; // 有数据 / 合法空数据 → 输出
            }
            if (ReflectDecision.NEEDS_CLARIFY.equals(judge.getJudgment())) {
                AskReplyVO vo = simpleReply(judge.getReason(), intent.getIntent(), intent.getSource());
                vo.setElapsedMs(System.currentTimeMillis() - t0);
                persist(sessionId, question, vo);
                return vo;
            }
            // STOP：确定性错误（登记缺陷/未覆盖组合）→ 诊断，不重试（闸②）
            if (ReflectDecision.STOP.equals(judge.getJudgment())) {
                // 未登记组合/未登记指标 → 用户视角"能问什么"；其他确定性错误 → 中性话术（技术细节只进日志）
                boolean unsupported = ReflectDecision.CATEGORY_UNSUPPORTED.equals(judge.getCategory());
                AskReplyVO vo = unsupported ? unregisteredReply(intent) : diagnosticReply(intent.getIntent());
                vo.setElapsedMs(System.currentTimeMillis() - t0);
                // 未登记：落库意图记 NOT_SUPPORTED（与本次回复一致，前端追问建议回退到通用示例，
                // 避免"点建议 → 又被拒"的循环）；指标×粒度明细留在服务端日志（[AI缺口]）与后续反馈表
                persist(sessionId, question, vo, unsupported ? null : intent);
                return vo;
            }
            // NEEDS_REPLAN：可修复错误 → 规则重试（预算内，闸① 失败指标跟踪：二次同因不重试）
            if (replan >= RULE_REPLAN_BUDGET) {
                AskReplyVO vo = diagnosticReply(intent.getIntent());
                vo.setElapsedMs(System.currentTimeMillis() - t0);
                persist(sessionId, question, vo, intent);
                return vo;
            }
            // 闸④：有数据即输出（本轮策略优先于继续重试）
            if (meta.hasData()) {
                break;
            }
            log.info("[Agent/Loop] REPLAN({}): {}", replan + 1, judge.getReason());
            intent = replanRule(intent);
            replan++;
        }

        // ── 输出：骨架答案 + LLM 润色（数字校验）+ 会话落库 ──
        AskReplyVO vo;
        if (exec == null || exec.getRows() == null || exec.getRows().isEmpty()) {
            if (isWorkDataMetric(intent.getIntent())) {
                // 报工类指标空数据：明确时间范围 + 最近报工日；若有未审核报工则告知（口径弥合，全局生效）
                vo = new AskReplyVO();
                vo.setAnswer(emptyDataAnswer.build(intent, latestSubmitDay(), exec.getParams(),
                        pendingSummary(exec.getParams())));
                vo.setIntent(intent.getIntent());
                vo.setFallback(true);
                vo.setSource(intent.getSource());
            } else {
                // 非报工类（延期/库存/清单等）：走各自模板的空文案（如"近期 7 天内没有待交付的订单"）
                // 空数据不进 LLM 生成（话术由模板确定，保持确定性）
                vo = answerComposer.compose(intent, exec);
                vo.setFallback(true);
            }
        } else {
            vo = answerComposer.compose(intent, exec);
            // 实体被丢弃（字典未命中 → 本次是全量口径）时不走 LLM 润色：骨架 + 披露，避免润色编出假事实
            if (!StringUtils.hasText(intent.getDroppedEntity())) {
                vo.setAnswer(answerGenerator.generate(question, intent, exec, vo.getAnswer()));
            }
        }
        appendDroppedEntityNote(vo, intent);
        appendSnapshotNote(vo, intent);
        vo.setTrust("authority");   // 通道 A：登记 SQL，与页面同源
        vo.setElapsedMs(System.currentTimeMillis() - t0);
        persist(sessionId, question, vo, intent);   // 完整意图落库（追问槽位继承依据）
        return vo;
    }

    /**
     * 实体未命中披露：把"以上是全量口径"写进答案
     *
     * <p>历史问题：问"上个月法兰盘产量"，字典没匹配上 → 静默降级全量 → 答案却是全公司 152，
     * 且润色还会写成"法兰盘报工总数为 152"（假事实）。
     */
    /**
     * 快照类指标的口径说明。
     *
     * <p>库存/在制是**时点快照**，与所选时间区间无关。这句放在润色之后追加：
     * 润色可能把它丢掉（实测被改写掉，还多加了"2026年9月"这种误导性月份前缀）。
     */
    private void appendSnapshotNote(AskReplyVO vo, AskIntentResult intent) {
        if (intent == null || vo == null || !StringUtils.hasText(vo.getAnswer())) {
            return;
        }
        if (!"STOCK".equals(intent.getIntent()) || vo.getAnswer().contains("快照")) {
            return;
        }
        vo.setAnswer(vo.getAnswer() + "\n\n（库存为当前时点快照，与所选时间区间无关）");
    }

    /**
     * 实体未命中披露：把"以上是全量口径"写进答案
     *
     * <p>历史问题：问"上个月法兰盘产量"，字典没匹配上 → 静默降级全量 → 答案却是全公司 152，
     * 且润色还会写成"法兰盘报工总数为 152"（假事实）。
     */
    private void appendDroppedEntityNote(AskReplyVO vo, AskIntentResult intent) {
        if (vo == null || !StringUtils.hasText(intent.getDroppedEntity())) {
            return;
        }
        // 按维度措辞：排名类问题说"合计"是错的（"上面是全部工序的数据"才对）
        String scope;
        String field = intent.getDroppedEntityField();
        if ("processNameOrCode".equals(field)) {
            scope = "全部工序的数据";
        } else if ("employeeName".equals(field)) {
            scope = "全部员工的数据";
        } else if ("productNameOrCode".equals(field)) {
            scope = "全部产品的数据";
        } else {
            scope = "全部范围的数据";
        }
        String note = "（注：没找到「" + intent.getDroppedEntity() + "」，上面给的是" + scope + "。）";
        vo.setAnswer((vo.getAnswer() == null ? "" : vo.getAnswer()) + note);
        vo.setFallback(true);
    }

    /**
     * 语义防线（已废弃：保留空实现以避免调用方编译不过；新架构不用它）。
     *
     * <p>删除原因：它曾在这里拦"创作类 / 归因类（为什么…）"，而下游新增的分析算子能力永远到不了这里之后，
     * 于是"为什么"被拒、只能靠关键词补丁；按新架构，未命中的问题统一走"生成 SQL → 普通指引性回答"，
     * 不需要这类前置语义规则。
     */
    @Deprecated
    private AskIntentResult semanticGuard(String q) {
        return null;
    }

    /**
     * 默认时间范围回退（TimeParser 无匹配时置空 → 这里补"本月"，与页面默认一致）
     *
     * <p>不补的话，登记 SQL 的 {@code submit_day between #{startDate} and #{endDate}} 会变成
     * {@code BETWEEN NULL AND NULL} → 0 行 → 被误报成"暂无已审核报工数据"
     * （历史现象："哪天良品率最低""车削工序的良品率"明明有数据却答没数据）。
     */
    private void applyDefaultTimeRange(AskIntentResult intent) {
        if (StringUtils.hasText(intent.getStartDate()) && StringUtils.hasText(intent.getEndDate())) {
            return;
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate first = today.withDayOfMonth(1);
        intent.setStartDate(first.toString());
        intent.setEndDate(first.withDayOfMonth(first.lengthOfMonth()).toString());
        intent.setTimeType("month");
        log.info("[Agent/时间] 问题未含时间词 → 默认本月: {} ~ {}", intent.getStartDate(), intent.getEndDate());
    }

    /** 最近一次报工日（空数据引导；失败返回 null 不影响回答） */
    private String latestSubmitDay() {
        try {
            return dailyMapper.selectLatestSubmitDay(tenantCodeOf());
        } catch (Exception e) {
            return null;
        }
    }

    /** 未审核报工汇总（空数据话术弥合用；失败返回 null） */
    private java.util.Map<String, Object> pendingSummary(java.util.Map<String, String> params) {
        try {
            String start = params == null ? null : params.get("startDate");
            String end = params == null ? null : params.get("endDate");
            if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) {
                return null;
            }
            return dailyMapper.selectPendingSummary(tenantCodeOf(), start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String tenantCodeOf() {
        Object v = com.cosmo.hhim.common.core.threadlocal.ThreadContext.get(
                com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
        return v == null ? "" : v.toString();
    }

    /* ---------------- Act：归一化 ---------------- */

    /** 执行结果 → 循环元信息（只读 success/error/rowCount/metric，数据不透明） */
    private ExecutionResult normalize(AskExecutionResult exec) {
        ExecutionResult meta = new ExecutionResult();
        StepResult sr = new StepResult();
        sr.setMetric(exec.getMetricCode());
        sr.setRows(exec.getRows() != null ? exec.getRows()
                : new java.util.ArrayList<>());
        if (exec.getError() != null) {
            sr.setSuccess(false);
            sr.setError(exec.getError());
        } else {
            sr.setSuccess(true);
            sr.setRowCount(exec.getRows() == null ? 0 : exec.getRows().size());
        }
        meta.getSteps().add(sr);
        return meta;
    }

    /* ---------------- Replan（P0 规则版；P1 升级 LLM Replan） ---------------- */

    private AskIntentResult replanRule(AskIntentResult intent) {
        // 简化降级：去除分组粒度、扩大条数，重试一次（P1 改为 LLM 依据反思理由重规划）
        intent.setGroupBy(null);
        intent.setLimit(3);
        log.info("[Agent/Replan] 规则降级: groupBy=null, limit=3");
        return intent;
    }

    /* ---------------- 计划辅助（复用原编排逻辑） ---------------- */

    /** 报工类指标（空数据时走"报工引导话术"）；非报工类走各自模板空文案 */
    private boolean isWorkDataMetric(String code) {
        return "SUMMARY".equals(code) || "PRODUCT_PASS_RATE".equals(code) || "PROCESS_PASS_RATE".equals(code)
                || "EMPLOYEE_PASS_RATE".equals(code) || "SUBMIT_RANK".equals(code) || "NG_DETAIL".equals(code);
    }

    private AskIntentResult resolve(String question, java.util.List<Map<String, String>> context) {
        AskIntentResult r = llmIntentResolver.resolve(question, context);
        // 【必须短路】LLM 明确判定"非业务问题"（task=non_biz）时直接采纳，**不得**再用规则兜底覆盖。
        // 历史 bug：模型正确判了 non_biz（intent=NOT_SUPPORTED），却被下面的规则兜底按关键词"产量"套成 SUMMARY，
        // 结果「写一首关于产量的诗」照样答了数据 —— 规则只能看字面，不能推翻语义判断。
        if (r != null && "non_biz".equals(r.getTask())) {
            log.info("[AI意图] task=non_biz（LLM 语义判定）→ 不进入能力匹配，跳过规则兜底");
            return r;
        }
        // LLM 未命中/输出澄清（且属于业务问题）时，才用规则兜底
        if (r == null || "NOT_SUPPORTED".equals(r.getIntent()) || !StringUtils.hasText(r.getIntent())
                || r.getClarifyQuestion() != null) {
            AskIntentResult rule = ruleIntentResolver.resolve(question);
            rule.setRawLlm(r == null ? null : r.getRawLlm());
            // 保留问题性质：规则结果不带 task，默认按业务问题（biz）处理
            rule.setTask(r == null || !StringUtils.hasText(r.getTask()) ? "biz" : r.getTask());
            return rule;
        }
        return r;
    }

    /** 排序方向代码归一：问题词强制（最多/最高/最大→desc；最少/最低/最小→asc）；否则默认 desc（覆盖 LLM 的 dir 猜测） */
    private void normalizeOrder(String question, AskIntentResult intent) {
        if (question != null) {
            if (question.contains("最多") || question.contains("最高") || question.contains("最大")
                    || question.contains("第一") || question.contains("最前") || question.contains("榜首")) {
                intent.setOrderDir("desc");
                return;
            }
            if (question.contains("最少") || question.contains("最低") || question.contains("最小")) {
                intent.setOrderDir("asc");
                return;
            }
        }
        if (intent.getOrderDir() == null) {
            intent.setOrderDir("desc");
        }
    }

    /** "快交付"语义归一：DELIVERY_RISK 下区分 soon（近期交付）与 overdue（延期清单）——mode 为指标内模式，不放实体 */
    private void normalizeDeliveryMode(String question, AskIntentResult intent) {
        if (!"DELIVERY_RISK".equals(intent.getIntent())) {
            return;
        }
        if (question != null && (question.contains("快交付") || question.contains("即将交付")
                || question.contains("快到期") || question.contains("临近") || question.contains("马上交付"))) {
            intent.setMode("soon");
        }
    }

    /** groupBy 白名单校验：不在该指标 dims 内的粒度 → 置空（本体是语义承诺清单，dims 外多为 LLM 理解偏差） */
    private void validateGroupBy(AskIntentResult intent) {
        if (intent.getGroupBy() == null) {
            return;
        }
        try {
            com.alibaba.fastjson.JSONObject m = ontologyService.metric(intent.getIntent());
            com.alibaba.fastjson.JSONArray dims = m != null ? m.getJSONArray("dims") : null;
            if (dims == null || !dims.contains(intent.getGroupBy())) {
                // 记录缺口（保持原行为：静默置空等价于"按该指标默认形状回答"，避免误伤时间粒度被当成分组的情况）
                log.warn("[AI缺口] type=GROUP_BY_OUT_OF_DIMS intent={} groupBy={} dims={}（已置空，按默认形状回答）",
                        intent.getIntent(), intent.getGroupBy(), dims);
                intent.setGroupBy(null);
            }
        } catch (Exception e) {
            intent.setGroupBy(null);
        }
    }

    /** 实体查证（歧义 → 澄清；未命中 → 降级全量但**必须披露**，不静默） */
    private AskIntentResult handleEntities(AskIntentResult intent) {
        EntityResolver.Resolved er = entityResolver.resolveEntity(intent);
        switch (er.status) {
            case AMBIGUOUS:
                intent.setClarifyQuestion(er.ambiguousQuestion);
                intent.setClarifyOptions(er.candidates);
                return intent;
            case NOT_FOUND:
                // 关键：不能"静默按全量回答"。记下被丢弃的实体，输出层会追加披露、并跳过 LLM 润色
                // （否则润色会把问题里的实体名和未过滤的数字拼成一句假事实）
                String dropped = intent.getEntities().get(er.field);
                log.warn("[AI缺口] type=ENTITY_NOT_FOUND field={} value={}（降级全量 + 答案披露）", er.field, dropped);
                intent.setDroppedEntity(dropped);
                intent.setDroppedEntityField(er.field);
                intent.getEntities().remove(er.field);
                intent.setClarifyQuestion(null);
                return intent;
            case OK:
                intent.getEntities().put(er.field, er.value);
                return intent;
            default:
                return intent;
        }
    }

    /* ---------------- 输出话术 ---------------- */

    private AskReplyVO boundaryReply() {
        return boundaryReply(null);
    }

    private AskReplyVO boundaryReply(AskIntentResult intent) {
        AskReplyVO vo = new AskReplyVO();
        // 语义化边界：LLM 结合上下文给出的原因+建议；降级（无语义信息）时用模板
        String hint = intent != null ? intent.getNotSupportedHint() : null;
        String reason = intent != null ? intent.getNotSupportedReason() : null;
        if (StringUtils.hasText(hint) || StringUtils.hasText(reason)) {
            StringBuilder sb = new StringBuilder("这个问题我暂时还不会回答。");
            if (StringUtils.hasText(reason)) {
                sb.append(reason).append("。");
            }
            if (StringUtils.hasText(hint)) {
                sb.append("你可以试试：").append(hint).append("。");
            } else {
                sb.append("你可以试试问：「今天产量怎么样？」「哪个产品良品率最低？」「哪些订单要延期？」");
            }
            vo.setAnswer(tidy(sb.toString()));
        } else {
            vo.setAnswer("这个问题我暂时还不会回答。你可以试试问：「今天产量怎么样？」「哪个产品良品率最低？」"
                    + "「哪些订单要延期？」，或到分析页面查看。");
        }
        vo.setIntent("NOT_SUPPORTED");
        vo.setFallback(true);
        vo.setSource("NONE");
        return vo;
    }

    /**
     * 话术整理：模型给的 reason/hint 常自带句末标点，拼接后会出现「。。」「：。」，这里收敛掉。
     */
    private String tidy(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("([。！？；，、])\\1+", "$1")
                .replace("：。", "。")
                .replace("：，", "，")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 未登记组合 / 未登记指标：用户视角话术
     *
     * <p>说清三件事：这个问法没学会、该指标目前能看什么、可以先问什么。
     * 技术原因（指标×粒度）只进日志（[AI缺口]），不抛给用户。
     * intent 置 NOT_SUPPORTED：前端追问建议回退到通用示例，避免"点建议→又被拒"的循环。
     */
    private AskReplyVO unregisteredReply(AskIntentResult intent) {
        String code = intent.getIntent();
        String name = metricName(code);
        String gb = intent.getGroupBy();
        StringBuilder sb = new StringBuilder("这个问法我还没学会。");
        if (!IntentExecutor.isRegistered(code)) {
            // 指标整体没有执行器（本体已发布但实现缺失）
            sb.append("「").append(name).append("」还没有可用的查询实现");
        } else if (StringUtils.hasText(gb)) {
            sb.append("「").append(name).append("」目前不支持按").append(IntentExecutor.groupByLabel(gb)).append("看");
        } else {
            sb.append("「").append(name).append("」这个口径我暂时给不了");
        }
        String supported = supportedGroupByText(code);
        if (StringUtils.hasText(supported)) {
            sb.append("（可用粒度：").append(supported).append("）");
        }
        String example = metricExample(code);
        if (StringUtils.hasText(example)) {
            sb.append("；你可以先问「").append(example).append("」");
            // 引号内已是问号/句号/叹号时不再补句号（避免"…？」。"这种重复标点）
            if (!endsWithSentencePunct(example)) {
                sb.append("。");
            }
        } else {
            sb.append("。");
        }

        AskReplyVO vo = new AskReplyVO();
        vo.setAnswer(sb.toString());
        vo.setIntent("NOT_SUPPORTED");
        vo.setFallback(true);
        vo.setSource("AGENT");
        log.warn("[AI缺口] type=REPLY_UNREGISTERED intent={} groupBy={} scope={}", code, gb, intent.getStatScope());
        return vo;
    }

    /** 未登记组合的用户话术：可用粒度（空=只有汇总/清单形状，不给粒度括号） */
    private String supportedGroupByText(String code) {
        java.util.Set<String> dims = new java.util.LinkedHashSet<>(IntentExecutor.consumedGroupBys(code));
        dims.addAll(IntentExecutor.toleratedGroupBys(code));
        if (dims.isEmpty()) {
            return "";
        }
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (String d : dims) {
            labels.add("按" + IntentExecutor.groupByLabel(d));
        }
        return String.join("/", labels);
    }

    /** 指标人话名（本体 name；取不到退回 code） */
    private String metricName(String code) {
        try {
            com.alibaba.fastjson.JSONObject m = ontologyService.metric(code);
            if (m != null && StringUtils.hasText(m.getString("name"))) {
                return m.getString("name");
            }
        } catch (Exception e) {
            // 本体不可用时退回 code
        }
        return code == null ? "该指标" : code;
    }

    /** 指标首个问题模板（已知可答的示例；取不到退回通用示例） */
    private String metricExample(String code) {
        try {
            com.alibaba.fastjson.JSONObject m = ontologyService.metric(code);
            com.alibaba.fastjson.JSONArray tpl = m == null ? null : m.getJSONArray("questionTemplates");
            if (tpl != null && !tpl.isEmpty()) {
                String first = tpl.getString(0);
                if (StringUtils.hasText(first)) {
                    return first;
                }
            }
        } catch (Exception e) {
            // 忽略：走通用示例
        }
        return "今天产量怎么样？";
    }

    /** 已是句末标点（问号/句号/叹号/分号）——引号内自带标点时外层不再补句号 */
    private boolean endsWithSentencePunct(String s) {
        if (!StringUtils.hasText(s)) {
            return false;
        }
        char c = s.charAt(s.length() - 1);
        return c == '？' || c == '?' || c == '！' || c == '!' || c == '。' || c == '；' || c == ';';
    }

    /**
     * 其他确定性失败（登记缺陷/执行异常等）：中性话术，技术原因只在日志里（[Agent/Reflect] 已记录）
     */
    private AskReplyVO diagnosticReply(String intentCode) {
        AskReplyVO vo = new AskReplyVO();
        vo.setAnswer("这个查询我没跑通（已记录）。换个时间范围或换个问法再试试，比如「今天产量怎么样？」「哪些订单要延期？」。");
        vo.setIntent(intentCode);
        vo.setFallback(true);
        vo.setSource("AGENT");
        return vo;
    }

    private AskReplyVO clarifyReply(AskIntentResult intent) {
        AskReplyVO vo = new AskReplyVO();
        vo.setAnswer("请确认您的查询范围：");
        vo.setIntent(intent.getIntent());
        vo.setFallback(true);
        vo.setSource(intent.getSource());
        Map<String, Object> clarify = new LinkedHashMap<>();
        clarify.put("question", intent.getClarifyQuestion() != null ? intent.getClarifyQuestion() : "请确认您的查询范围");
        clarify.put("options", intent.getClarifyOptions());
        vo.setClarify(clarify);
        return vo;
    }

    /**
     * 集中度算子：回答"不良主要集中在哪一类/哪道工序"。
     *
     * <p>质检开关未开（无质检记录）时，AnalysisService 会返回 notice → 这里返回 null 交给生成 SQL 兜底。
     */
    private AskReplyVO concentrationReply(String question, AskIntentResult intent, String operator) {
        String start = intent.getStartDate();
        String end = intent.getEndDate();
        if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) {
            return null;
        }
        java.util.Map<String, String> filters = new java.util.HashMap<>();
        if (StringUtils.hasText(intent.getEntities().get("productNameOrCode"))) {
            filters.put("productNameOrCode", intent.getEntities().get("productNameOrCode"));
        }
        if (StringUtils.hasText(intent.getEntities().get("processNameOrCode"))) {
            filters.put("processNameOrCode", intent.getEntities().get("processNameOrCode"));
        }
        com.cosmo.hhim.micro.application.service.ai.analysis.AnalysisService.AnalysisOutcome out =
                analysisService.ngTypeConcentration("不良集中度", start, end, filters);
        if (out.brief == null) {
            log.info("[AI分析] 集中度算子无数据：{}", out.notice);
            return null;
        }
        String skeleton = out.brief;
        String answer = answerGenerator.polishAnalysis(question, skeleton);
        AskReplyVO vo = simpleReply(answer, operator, intent.getSource());
        vo.setFallback(skeleton.equals(answer));
        vo.setTrust("authority");
        java.util.Map<String, Object> evidence = new java.util.LinkedHashMap<>();
        java.util.Map<String, Object> metric = new java.util.LinkedHashMap<>();
        metric.put("name", "不良集中度");
        metric.put("formula", "按不良类型/工序汇总不良数并计算 Top-N 占比（源=质检记录）");
        evidence.put("metric", metric);
        java.util.Map<String, Object> source = new java.util.LinkedHashMap<>();
        source.put("api", out.api);
        source.put("params", out.params);
        source.put("note", "与本页统计同源（质检记录口径）");
        evidence.put("source", source);
        vo.setEvidence(evidence);
        return vo;
    }

    /* ---------------- 通道 C：LLM 生成 SQL（探索性） ---------------- */

    /**
     * 本体覆盖不到时的兜底通道：让 LLM 生成只读 SQL → 过 SqlGuard 闸 → 执行 → 润色。
     *
     * <p>返回 null 表示"没产出来"（未开启/生成被拒/执行失败），由调用方落回边界话术。
     * <p>信任分级：结果标注"探索性"，依据里写清"已过安全闸"与"未经登记口径校验"。
     */
    private AskReplyVO generatedSqlReply(String question, AskIntentResult intent, String sessionId) {
        try {
            if (!generatedSqlChannel.enabled()) {
                return null;
            }
            com.cosmo.hhim.micro.application.service.ai.sql.GeneratedSqlChannel.Outcome out =
                    generatedSqlChannel.run(question, tenantCodeOf(), sessionId, intent.getIntent());
            if (!out.isOk() || !StringUtils.hasText(out.getBrief())) {
                log.info("[AI-SQL] 通道C 未产出结果：{}", out.getReason());
                return null;
            }
            String skeleton = out.getBrief();
            String answer = answerGenerator.polishExploratory(question, skeleton);
            AskReplyVO vo = simpleReply(answer, "GENERATED_SQL", "GENERATED_SQL");
            vo.setFallback(skeleton.equals(answer));
            vo.setTrust("exploratory");   // 通道 C：LLM 生成 SQL（下面按"是否命中登记口径/关系"细化文案）
            // 模型在 purpose 里会自报依据：命中登记口径/登记关系时，文案应说明"已按登记口径"，
            // 否则用户看到"未经登记口径校验"会误以为数字不可信（实际口径与页面同源）
            String purposeText = String.valueOf(out.getParams() == null ? "" : out.getParams().get("purpose"));
            boolean registered = purposeText.contains("登记口径") || purposeText.contains("口径登记")
                    || purposeText.contains("已登记") || purposeText.contains("登记关系");
            java.util.Map<String, Object> evidence = new java.util.LinkedHashMap<>();
            java.util.Map<String, Object> metric = new java.util.LinkedHashMap<>();
            metric.put("name", registered ? "系统查询结果（已按登记口径）" : "系统查询结果（口径由本次查询确定）");
            metric.put("formula", registered
                    ? "本次查询已按本体登记的口径与登记关系计算（与页面口径同源）；可在“依据”里核对所用口径与联结方式"
                    : "本次查询口径由系统按问题自行确定（本体未登记该口径）；建议与页面数据核对");
            evidence.put("metric", metric);
            java.util.Map<String, Object> source = new java.util.LinkedHashMap<>();
            source.put("api", out.getApi());
            source.put("params", out.getParams());
            source.put("note", registered
                    ? "系统查询结果（已通过安全闸：仅 SELECT / 表白名单 / 敏感列拦截 / 租户隔离 / 行数上限；口径已对齐本体登记）"
                    : "系统查询结果（未登记口径，仅供参考；已通过安全闸：仅 SELECT / 表白名单 / 敏感列拦截 / 租户隔离 / 行数上限）");
            evidence.put("source", source);
            vo.setEvidence(evidence);
            return vo;
        } catch (Exception e) {
            log.warn("[AI-SQL] 通道C 异常：{}", e.getMessage());
            return null;
        }
    }

    /* ---------------- 分析分支（"为什么"类问题） ---------------- */

    /** 是否"为什么"类问题——**已废弃**：分析能力现在由 CapabilityGate 命中判定（算子槽位），不再用关键词 */
    @Deprecated
    private boolean isWhyQuestion(String q) {
        return false;
    }

    /**
     * 通道 B：分析算子作答（由 CapabilityGate 判定 mode=ANALYSIS 后进入）。
     *
     * <p>算子是**一等能力槽位**：LLM 选到 ATTRIBUTION/CONCENTRATE 就在这里执行，数字全部由
     * AnalysisService + MiniAnalyst 算出（可对账、可下钻），LLM 只负责措辞。
     *
     * @param operator 命中的算子编码（ATTRIBUTION / CONCENTRATE）
     * @return null 表示"算子组织不起来"（数据不足），由调用方转生成 SQL 通道
     */
    private AskReplyVO analysisReply(String question, AskIntentResult intent, String operator) {
        // 集中度算子：回答"不良主要集中在哪一类"
        if ("CONCENTRATE".equals(operator)) {
            return concentrationReply(question, intent, operator);
        }
        // 对比算子：只给"本期 vs 基期"的结论（回答"降了吗/差多少"），**不做原因归因、不下钻**
        boolean compareOnly = "COMPARE".equals(operator);
        // 归因算子（默认）：本期 vs 基期 → 维度贡献 → 下钻一层
        // 被分析的指标由 LLM 放在 entities.metricCode（如"良品率为什么降"→ PRODUCT_PASS_RATE），缺省按产品良品率
        String code = intent.getEntities() == null ? null : intent.getEntities().get("metricCode");
        boolean rateMetric = "PRODUCT_PASS_RATE".equals(code) || "PROCESS_PASS_RATE".equals(code)
                || "EMPLOYEE_PASS_RATE".equals(code);
        if (!rateMetric) {
            code = "PRODUCT_PASS_RATE";   // 归因算子当前只对良品率类指标做分解
        }
        String bStart = intent.getStartDate();
        String bEnd = intent.getEndDate();
        if (!StringUtils.hasText(bStart) || !StringUtils.hasText(bEnd)) {
            return null;
        }
        String[] prev = previousPeriod(bStart, bEnd);
        String aStart = prev[0];
        String aEnd = prev[1];

        // 分组维度与过滤：问题里点名的实体作为过滤条件，分组取"下一层"，这样才是在找原因而不是重复该实体
        java.util.Map<String, String> filters = new java.util.HashMap<>();
        String product = intent.getEntities().get("productNameOrCode");
        String process = intent.getEntities().get("processNameOrCode");
        String employee = intent.getEntities().get("employeeName");
        String dim;
        String dimLabel;
        if (StringUtils.hasText(process)) {
            filters.put("processNameOrCode", process);
            dim = "product";
            dimLabel = "产品";
        } else if (StringUtils.hasText(product)) {
            filters.put("productNameOrCode", product);
            dim = "process";
            dimLabel = "工序";
        } else if (StringUtils.hasText(employee)) {
            filters.put("employeeName", employee);
            dim = "product";
            dimLabel = "产品";
        } else {
            dim = guessDim(question);
            dimLabel = dimText(dim);
        }

        // 指标标签：分析算子是对"整个查询范围"做分解（不限定产品），因此用「良品率」而不是本体的「产品良品率」，
        // 否则按工序分解时会写成"产品良品率"，让人误解成某个产品（实测踩到）
        String metricLabel = "良品率";
        String labelA = monthLabel(aStart, aEnd);
        String labelB = monthLabel(bStart, bEnd);
        com.cosmo.hhim.micro.application.service.ai.analysis.AnalysisService.AnalysisOutcome cmp =
                analysisService.compareByDim(dim, metricLabel, labelA, aStart, aEnd, labelB, bStart, bEnd, filters);
        if (cmp.compare == null || cmp.compare.rateA == null) {
            log.info("[AI分析] 组织不起来（数据不足）dim={} {}~{} / {}~{}", dim, aStart, aEnd, bStart, bEnd);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(metricLabel).append(compareOnly ? "两期对比：" : "按" + dimLabel + "看变化：").append("\n").append(cmp.brief);

        // 下钻一层：主因确定时再看它内部是谁（对比算子不做：它只回答"降了吗/差多少"）
        String child = "process".equals(dim) ? "product" : "process";
        if (!compareOnly && StringUtils.hasText(cmp.compare.mainGroup) && !"day".equals(dim)) {
            com.cosmo.hhim.micro.application.service.ai.analysis.AnalysisService.AnalysisOutcome sub =
                    analysisService.drilldown(dim, cmp.compare.mainGroup, child, metricLabel,
                            labelA, aStart, aEnd, labelB, bStart, bEnd);
            if (sub.compare != null && sub.compare.rateA != null && sub.brief != null) {
                sb.append("\n下钻到「").append(dimText(child)).append("」：\n").append(sub.brief);
            }
        }
        // 不良类型集中度：仅当质检记录有数据时才提（否则如实跳过，不编 0）
        com.cosmo.hhim.micro.application.service.ai.analysis.AnalysisService.AnalysisOutcome ng =
                analysisService.ngTypeConcentration("不良类型集中度", bStart, bEnd, filters);
        if (ng.brief != null) {
            sb.append("\n").append(ng.brief);
        }

        String skeleton = sb.toString();
        // LLM 润色（与分析场景专用提示词）：允许讲原因，但数字集合 = 事实清单里的数字，越界整段回退清单
        String answer = answerGenerator.polishAnalysis(question, skeleton);
        boolean polished = !skeleton.equals(answer);
        AskReplyVO vo = simpleReply(answer, code, intent.getSource());
        vo.setFallback(!polished);   // 润色成功=LLM 生成；失败=模板（与既有 fallback 语义一致）

        // 路由：带本期区间进质量趋势页（与既有良品率类问题一致）
        java.util.Map<String, Object> route = new java.util.LinkedHashMap<>();
        route.put("path", "/pages-analysis/quality/index");
        java.util.Map<String, Object> routeParams = new java.util.LinkedHashMap<>();
        routeParams.put("startDate", bStart);
        routeParams.put("endDate", bEnd);
        route.put("params", routeParams);
        vo.setRoute(route);

        // 依据：口径 + 取数来源 + 事实快照（数字与答案文本同源，可对账）
        java.util.Map<String, Object> evidence = new java.util.LinkedHashMap<>();
        java.util.Map<String, Object> metric = new java.util.LinkedHashMap<>();
        metric.put("name", metricLabel);
        metric.put("formula", "良品数 / 质检总数 × 100%（按" + dimLabel + "分组；贡献度合计等于总变化）");
        evidence.put("metric", metric);
        java.util.Map<String, Object> source = new java.util.LinkedHashMap<>();
        source.put("api", cmp.api);
        source.put("params", cmp.params);
        source.put("note", "与本页统计同源（已审口径，比率按页面口径截断 3 位）");
        evidence.put("source", source);
        java.util.List<java.util.Map<String, Object>> snapshot = new java.util.ArrayList<>();
        java.util.Map<String, Object> total = new java.util.LinkedHashMap<>();
        total.put("label", labelA + " → " + labelB);
        total.put("value", "总良品率 " + com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(cmp.compare.rateA)
                + " → " + com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(cmp.compare.rateB)
                + "（" + com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pp(cmp.compare.deltaPp) + "）");
        snapshot.add(total);
        int topN = 0;
        for (java.util.Map<String, Object> row : cmp.rows) {
            if (topN++ >= 3) {
                break;
            }
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("label", String.valueOf(row.get("group")));
            item.put("value", "贡献 " + com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pp(
                    (java.math.BigDecimal) row.get("contributionPp"))
                    + "（样本 " + row.get("rowsA") + " → " + row.get("rowsB") + " 条）");
            snapshot.add(item);
        }
        evidence.put("snapshot", snapshot);
        evidence.put("asOf", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        vo.setEvidence(evidence);
        vo.setTrust("authority");   // 通道 B：归因算子（贡献度可对账）
        return vo;
    }

    /** 上一期：整月 → 上月整月；否则按同样天数前移 */
    private String[] previousPeriod(String start, String end) {
        java.time.LocalDate s = java.time.LocalDate.parse(start);
        java.time.LocalDate e = java.time.LocalDate.parse(end);
        boolean wholeMonth = s.getDayOfMonth() == 1 && e.equals(s.withDayOfMonth(s.lengthOfMonth()));
        if (wholeMonth) {
            java.time.LocalDate ps = s.minusMonths(1);
            return new String[]{ps.toString(), ps.withDayOfMonth(ps.lengthOfMonth()).toString()};
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1;
        java.time.LocalDate pe = s.minusDays(1);
        return new String[]{pe.minusDays(days - 1).toString(), pe.toString()};
    }

    /** 问题里出现维度词就按它分组，否则默认按工序（"为什么降"最常见是工序问题） */
    private String guessDim(String q) {
        if (q == null) {
            return "process";
        }
        if (q.contains("产品") || q.contains("物料") || q.contains("哪个货")) {
            return "product";
        }
        if (q.contains("员工") || q.contains("谁") || q.contains("人")) {
            return "employee";
        }
        if (q.contains("哪天") || q.contains("哪一天") || q.contains("什么时候")) {
            return "day";
        }
        return "process";
    }

    private String dimText(String dim) {
        if ("product".equals(dim)) {
            return "产品";
        }
        if ("employee".equals(dim)) {
            return "员工";
        }
        if ("day".equals(dim)) {
            return "日期";
        }
        return "工序";
    }

    /** 期间标签：整月用 yyyy-MM，否则用起止日 */
    private String monthLabel(String start, String end) {
        try {
            java.time.LocalDate s = java.time.LocalDate.parse(start);
            java.time.LocalDate e = java.time.LocalDate.parse(end);
            if (s.getDayOfMonth() == 1 && e.equals(s.withDayOfMonth(s.lengthOfMonth()))) {
                return start.substring(0, 7);
            }
        } catch (Exception ignore) {
            // 解析失败则退回原始字符串
        }
        return start + "~" + end;
    }

    private AskReplyVO simpleReply(String text, String intentCode, String source) {
        AskReplyVO vo = new AskReplyVO();
        vo.setAnswer(text);
        vo.setIntent(intentCode);
        vo.setFallback(true);
        vo.setSource(source);
        return vo;
    }

    /* ---------------- 工具 ---------------- */

    /** 活动日志（每轮 plan/act/reflect 摘要；P1 落库微 ai_agent_run） */
    private void logAgent(int round, AskIntentResult intent, ExecutionResult meta) {
        // 实体必打进日志：跨轮继承/键漂移/未命中披露都靠它定位（历史"法兰盘→全公司合计"就是这么查出来的）
        log.info("[Agent/Log] round={} intent={} groupBy={} time={}~{} entities={} scope={} ｜ success={} rows={} allFailed={}",
                round, intent.getIntent(), intent.getGroupBy(), intent.getStartDate(), intent.getEndDate(),
                intent.getEntities(), intent.getStatScope(),
                meta.getSteps().stream().anyMatch(StepResult::isSuccess), meta.hasData(), meta.allFailed());
    }

    private void persist(String sessionId, String question, AskReplyVO vo) {
        persist(sessionId, question, vo, null);
    }

    /** 落库：intent 全量 JSON（追问槽位继承的数据底座）；无完整意图时退化存 code */
    private void persist(String sessionId, String question, AskReplyVO vo, AskIntentResult intent) {
        if (!StringUtils.hasText(sessionId)) {
            return;
        }
        try {
            chatSessionService.appendMessage(sessionId, "user", question, null, null, null, null, null);
            String intentCol = intent != null ? slotJson(intent) : vo.getIntent();
            chatSessionService.appendMessage(sessionId, "ai", vo.getAnswer(),
                    toJson(vo.getRoute()), toJson(vo.getEvidence()), toJson(vo.getClarify()),
                    intentCol, vo.getElapsedMs() == null ? null : vo.getElapsedMs().intValue());
        } catch (Exception e) {
            log.warn("[AI会话] 落库失败（不影响回答）: {}", e.getMessage());
        }
    }

    /** 槽位紧凑 JSON（仅语义字段；不存 rawLlm，控制列长度） */
    private String slotJson(AskIntentResult i) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("intent", i.getIntent());
        if (i.getGroupBy() != null) {
            m.put("groupBy", i.getGroupBy());
        }
        if (i.getMode() != null) {
            m.put("mode", i.getMode());
        }
        if (i.getEntities() != null && !i.getEntities().isEmpty()) {
            m.put("entities", i.getEntities());
        }
        if (i.getStartDate() != null) {
            m.put("startDate", i.getStartDate());
        }
        if (i.getEndDate() != null) {
            m.put("endDate", i.getEndDate());
        }
        return com.alibaba.fastjson.JSON.toJSONString(m);
    }

    private String toJson(Object o) {
        return o == null ? null : com.alibaba.fastjson.JSON.toJSONString(o);
    }

    private String clean(String question) {
        return question == null ? "" : question.trim();
    }
}

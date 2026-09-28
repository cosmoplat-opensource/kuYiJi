/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
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
import java.util.List;
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
    public static final String CHAIN_VERSION = "2026-09-22.14（调度层 LLM 决策点：回填纠偏/失败重路由/缺口补取，预算≤5次·≤3轮）";

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
     * 问数主入口（Router + 计划 + 取数 + 输出）。
     *
     * <p>P2 架构：**解析一次 → 列出数据需求 → 取数 → 合成一段答案**。
     * 一句话里要几份数据就查几份，但它们**不是"几个问题"**：最终只有一段答案、
     * 一份合并依据、一次落库（没有"主诉求 / 补答"之分，也没有二次解析）。
     */
    public AskReplyVO run(String question, String sessionId) {
        long t0 = System.currentTimeMillis();
        // 每次提问必打印一行：便于确认①链路是否走到 ②后续 [AI需求]/[AI决策] 是否缺失
        log.info("[AI链路] 收到提问：{}（sessionId={}）", question, sessionId);

        // ── Router（确定性，无 LLM；空/超长/工资类/闲聊在这里截断）──
        String clean = clean(question);
        String route = agentRouter.route(clean);
        if (AgentRouter.BOUNDARY.equals(route)) {
            AskReplyVO vo = boundaryReply();
            persist(sessionId, clean, vo);
            return vo;
        }
        if (AgentRouter.SMALLTALK.equals(route)) {
            AskReplyVO vo = simpleReply("您好，我是 Ku易记 问数助手，可以问我产量、良品率、库存、延期等问题。",
                    "NOT_SUPPORTED", "NONE");
            persist(sessionId, clean, vo);
            return vo;
        }
        question = clean;

        // ── ① 解析一次：needs[]（数据需求清单）+ 主槽位 ──
        AskIntentResult primary = resolve(question, needContext(question, sessionId));
        java.util.List<AskIntentResult> needs = LlmIntentResolver.takeNeeds();
        // 诊断：主槽位最终形态（LLM 槽位 or 规则兜底）+ 是否被澄清短路 —— 排查"意图被换掉"用
        log.info("[AI需求] 主槽位就绪：intent={} how={} groupBy={} 澄清={} 其余需求={} 项",
                primary == null ? null : primary.getIntent(),
                primary == null ? null : primary.getHow(),
                primary == null ? null : primary.getGroupBy(),
                primary == null || primary.getClarifyQuestion() == null ? "无" : "有",
                needs.size());

        // 问题性质：非业务问题（创作/翻译/闲聊）直接给指引，不进能力匹配
        if (StringUtils.hasText(primary.getTask()) && !"biz".equals(primary.getTask())) {
            log.info("[AI决策] task={} → 非业务问题，直接普通指引性回答", primary.getTask());
            AskReplyVO vo = boundaryReply(primary);
            persist(sessionId, question, vo, primary);
            return vo;
        }

        // ── ② 取数：统一依赖图（主项=n0 与并列需求同图调度：该并行的并行、该串行的串行、该等待的等待）──
        // 子任务内**一律不落库**（只由主线程落一次）
        List<AskIntentResult> graphNodes = new java.util.ArrayList<>();
        graphNodes.add(primary);
        graphNodes.addAll(needs);
        DecisionBudget budget = new DecisionBudget();   // .14：本问的 LLM 决策预算（决策点共用）
        List<NeedOutcome> results = scheduleGraph(question, sessionId, graphNodes, budget);
        int needCount = results.size();   // 真实需求项数（日志/落库统计用，别写死）

        // ── ③ 合并骨架：按**原顺序**（第 1 段=主需求，其余按 needs 下标），段首带范围标签 ──
        StringBuilder skeleton = new StringBuilder();
        StringBuilder userSkeleton = new StringBuilder();
        StringBuilder segmentLog = new StringBuilder();
        java.util.Set<String> seenSeg = new java.util.HashSet<>();
        for (int i = 0; i < results.size(); i++) {
            NeedOutcome out = results.get(i);
            if (out == null || out.vo == null || !StringUtils.hasText(out.vo.getAnswer())) {
                continue;
            }
            // 同一段内容只进骨架一次：实测第二项需求因指代落空与主需求跑了同一查询，两段整段重复
            String segNorm = out.vo.getAnswer().trim().replaceAll("\\s+", "");
            if (!seenSeg.add(segNorm)) {
                log.info("[AI决策] 第 {} 项与前面内容完全相同（同一查询跑了多遍）→ 骨架去重跳过", i + 1);
                continue;
            }
            if (skeleton.length() > 0) {
                skeleton.append("\n\n");
            }
            if (userSkeleton.length() > 0) {
                userSkeleton.append("\n\n");
            }
            // ★算子段：**人话结论在前、完整明细在后**。
            //   历史 bug：优先用 params.facts（601 字原始清单）→ 我写的人话结论（含"按自身降幅谁降得最多"）
            //   反被忽略 ✗ 模型按清单里的"贡献度"理解，答成"自身良品率上升的产品是下降最多的" ✗
            //   → 改成结论在前（模型优先采信），明细在后（提供可核对的数字）。
            String seg;
            Object facts = out.vo.getParams() == null ? null : out.vo.getParams().get("facts");
            if (facts != null && StringUtils.hasText(String.valueOf(facts))) {
                seg = out.vo.getAnswer().trim() + "（明细：" + facts + "）";
            } else {
                seg = out.vo.getAnswer().trim();
            }
            seg = seg.trim();
            if (seg.length() > SEGMENT_MAX_CHARS) {
                seg = seg.substring(0, SEGMENT_MAX_CHARS) + "…";
            }
            skeleton.append("【第 ").append(i + 1).append(" 项数据｜")
                    .append(scopeLabel(out.intent)).append("】").append(seg);
            // 用户版骨架：只留人话结论。明细里的★指令/口径提示是喂模型的，润色失败回退时不给用户看
            String segUser = out.vo.getAnswer().trim();
            if (segUser.length() > SEGMENT_MAX_CHARS) {
                segUser = segUser.substring(0, SEGMENT_MAX_CHARS) + "…";
            }
            userSkeleton.append("【第 ").append(i + 1).append(" 项数据｜")
                    .append(scopeLabel(out.intent)).append("】").append(segUser);
            segmentLog.append(i + 1).append("=").append(seg.length()).append("字 ");
        }
        // 主 VO = 第 1 项的合成结果（路由/信任级以它为准），答案换成合并后的骨架
        NeedOutcome head = results.isEmpty() ? null : results.get(0);
        AskReplyVO vo = head != null && head.vo != null ? head.vo : boundaryReply(primary);
        String combined = skeleton.length() > 0 ? skeleton.toString() : vo.getAnswer();
        String userCombined = userSkeleton.length() > 0 ? userSkeleton.toString() : combined;

        // ★依据合并：把**每一项**需求的口径/来源/结果快照都追加进主 VO 的依据里
        //   （前端「依据」卡片只渲染 metric/source/snapshot，合并后用户能对着每段数逐项核对）
        if (results.size() > 1) {
            if (vo.getEvidence() == null) {
                vo.setEvidence(new java.util.LinkedHashMap<String, Object>());
            }
            for (int i = 1; i < results.size(); i++) {
                NeedOutcome part = results.get(i);
                if (part == null || part.vo == null) {
                    continue;
                }
                int before = snapshotSize(vo.getEvidence());
                mergeEvidence(vo, part.vo, needScope(part.intent));
                log.info("[AI决策] 合并第 {} 项依据：主 VO 依据 {} → {} 项",
                        i + 1, before, snapshotSize(vo.getEvidence()));
            }
        }

        log.info("[AI决策] 取数完成：{} 项数据需求（并发上限 {}）｜ 各段字数 {}",
                needCount, MAX_NEEDS_PARALLEL, segmentLog.toString().trim());

        // ── ④ 一次润色：以合并后的全部数据为摘要 → 一段不矛盾的整体回答 ──
        // 数字护栏不变（只能用摘要里出现过的数字，含其算术结果）
        // ★先过一遍"界面友好"清洗：骨架里可能带 SQL/异常原文/文件路径（技术细节只进日志），
        //   清洗是**兜底**——即便润色失败、直接回退骨架，用户也看不到开发层信息。
        String safeSkeleton = humanize(combined);
        // ★把"本次的主对象"（第 1 项取到的那个对象，如「2026-08-05」）显式写进摘要：
        //   后续归因/下钻段讲的是**这个对象**的原因 ✗ 不给主语标记时，模型会用归因段自己挑的对象
        //   （实测：主语被写成 08-12，而"哪天最低"的真值是 08-05 → 两个对象混进同一句结论）。
        String subject = subjectOf(results);
        if (StringUtils.hasText(subject)) {
            safeSkeleton = "【本次主对象】" + subject + "\n" + safeSkeleton;
        }
        boolean polished = false;
        try {
            String merged = answerGenerator.polishAnalysis(question, safeSkeleton);
            // polishAnalysis 失败时会把骨架原样返回 → 用"是否仍等于骨架"判断润色是否真的发生
            if (StringUtils.hasText(merged) && !merged.equals(safeSkeleton)) {
                combined = merged;
                polished = true;
                log.info("[AI决策] 合并润色完成（{} 项 → 1 段）", results.size());
            } else {
                log.info("[AI决策] 润色未生效（数字护栏未过/未启用）→ 回退骨架并清理内部标记");
            }
        } catch (Exception e) {
            log.warn("[AI决策] 合并润色失败，返回分项结果: {}", e.toString());
        }
        // 实体被丢弃（字典未命中→全量口径）时不做润色，骨架 + 披露，避免润色编出假事实
        if (StringUtils.hasText(primary.getDroppedEntity())) {
            combined = safeSkeleton;
            polished = false;
        }
        // 兜底出口：未润色时把骨架里的**内部标记**清干净（"【第 N 项数据｜…】"、"查询1返回 1 行"），
        // 否则用户会看到开发视角的措辞（实测踩到）。主对象行是给模型的标注，也不给用户看。
        vo.setAnswer(polished ? formatDecimals(combined) : cleanSkeletonForUser(humanize(userCombined)));
        appendDroppedEntityNote(vo, primary);
        appendSnapshotNote(vo, primary);
        vo.setElapsedMs(System.currentTimeMillis() - t0);

        // ── ⑤ 落库一次 ──
        persist(sessionId, question, vo, primary);
        return vo;
    }

    /**
     * 界面友好清洗（**输出前最后一道**）：把不该给用户看的技术细节清掉。
     *
     * <p>为什么必须有：骨架里可能含 SQL、异常栈、类名、文件路径、数据库报错原文
     * （实测泄漏过 {"### Error querying database…"} 与 D:\cos\…\Mapper.xml）。
     * 硬约定是"界面上不许出现表名/字段名/SQL/API/编码"——**润色失败时骨架会直接展示**，
     * 所以清洗必须在出口做，而不能只依赖提示词约束。
     */
    private String humanize(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String s = text;
        // ① 数据库异常块（### 起头的技术明细）整段剔除
        s = s.replaceAll("(?s)###.*?(?=(###|\\n|$))", " ");
        // ② 压成一行后再按行清理技术行
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\\r?\\n")) {
            String t = line.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (t.contains("Error querying database") || t.contains("SQLSyntaxErrorException")
                    || t.contains("BadSqlGrammar") || t.contains("nested exception")) {
                continue;
            }
            if (t.matches("(?s).*\\b(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE|GROUP BY|ORDER BY|LIMIT)\\b.*")) {
                continue;
            }
            if (t.matches("(?s).*[A-Za-z]:\\\\[^\\s]+.*") || t.contains(".xml") || t.contains(".java:")) {
                continue;
            }
            // ③ 行内残留的类名/栈帧（如 GeneratedSqlChannel.run:128）与反引号标识
            t = t.replaceAll("[A-Za-z][A-Za-z0-9_.]*\\.(java|xml):\\d+", " ");
            t = t.replaceAll("`[^`]*`", " ");
            t = t.replaceAll("\\s{2,}", " ").trim();
            if (t.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(t);
        }
        String out = sb.length() > 0 ? sb.toString() : text;
        // ④ 行首的方括号内容若含技术词，去掉方括号但保留信息（如 [【按 process 维度看】] → 按 process 维度看）
        out = out.replace("[【", "【").replace("】]", "】");
        return out;
    }

    /** 依据段头（人话）：范围标签（按 X 维度看 / 仅限某对象）——用户一眼看出这段依据对应哪一项需求 */
    private String needScope(AskIntentResult intent) {
        if (intent == null) {
            return "本次查询";
        }
        StringBuilder sb = new StringBuilder();
        if (intent.getFilter() != null && !intent.getFilter().isEmpty()) {
            sb.append("【范围：仅限 ").append(String.join("、", intent.getFilter().values())).append("】");
        }
        if (StringUtils.hasText(intent.getGroupBy())) {
            sb.append("【按 ").append(intent.getGroupBy()).append(" 维度看】");
        }
        if (sb.length() == 0 && StringUtils.hasText(intent.getWant())) {
            sb.append(intent.getWant());
        }
        if (sb.length() == 0) {
            sb.append(StringUtils.hasText(intent.getIntent()) ? intent.getIntent() : "本次查询");
        }
        return sb.toString();
    }

    /** 单项需求喂给"合并润色"的摘要上限（够表达事实即可，防止算子骨架上千字稀释重点） */
    private static final int SEGMENT_MAX_CHARS = 600;

    /** 需求段头（人话）：指标名 + 过滤/分组范围，防多段张冠李戴 */
    private String scopeLabel(AskIntentResult intent) {        if (intent == null) {
            return "本次查询";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.hasText(intent.getGroupBy()) ? intent.getGroupBy() : "result");
        if (StringUtils.hasText(intent.getWant())) {
            sb.append("｜").append(intent.getWant());
        }
        if (intent.getFilter() != null && !intent.getFilter().isEmpty()) {
            sb.append("（范围：").append(String.join("、", intent.getFilter().values())).append("）");
        }
        return sb.toString();
    }

    /* ---------------- ② 取数（按 need 执行；子任务内不落库） ---------------- */

    /** 并发取数上限（无依赖的需求同时最多跑几项） */
    private static final int MAX_NEEDS_PARALLEL = 5;
    /**
     * 单项取数超时（毫秒）：超时标记"该项未取到"，不影响其它项。
     *
     * <p>为什么是 20s 而不是 8s：自生成 SQL 的单项要 **LLM 生成 + 过闸 + 执行**（还可能第 2 轮补充），
     * 实测单次要 3~5 秒；并发时多个子任务抢同一个 LLM，更慢。8s 会造成**偶发误判超时**
     * （实测：主线程那条跑了 7 秒成功，子线程同一件事却因 8s 被杀 → 答案里出现"该项未取到（取数超时）"）。
     * 20s 与 LLM 客户端 12s 超时 + 一轮重试的量级匹配，既不会误杀，也不会让请求无限拖。
     */
    private static final long NEED_TIMEOUT_MS = 20000L;

    /** 取数线程池：固定 5、守护线程（超时/异常都只影响该项自己） */
    private static final java.util.concurrent.ExecutorService NEED_POOL =
            new java.util.concurrent.ThreadPoolExecutor(MAX_NEEDS_PARALLEL, MAX_NEEDS_PARALLEL,
                    60L, java.util.concurrent.TimeUnit.SECONDS,
                    new java.util.concurrent.LinkedBlockingQueue<>(64),
                    runnable -> {
                        Thread t = new Thread(runnable, "ai-need");
                        t.setDaemon(true);
                        return t;
                    },
                    new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());

    /**
     * 子任务落库抑制：跨线程会失效 —— 所以在**每个子任务的线程内**显式设置，
     * 确保"子任务内一律不落库、只由主线程落一次"。
     */
    private static final ThreadLocal<Boolean> WORKER_MODE = new ThreadLocal<>();

    /** 一项需求的取数结果 */
    private static class NeedOutcome {
        private final AskReplyVO vo;
        private final AskIntentResult intent;
        private final String error;

        NeedOutcome(AskReplyVO vo, AskIntentResult intent, String error) {
            this.vo = vo;
            this.intent = intent;
            this.error = error;
        }
    }

    /* ────────── ②取数调度器：依赖图 + 事件驱动（该并行的并行、该串行的串行、该等待的等待）────────── */

    /**
     * 整图总时延预算（毫秒）：防失控上限，不是目标时延 —— 旧两批式最坏已可 2×20s=40s，
     * 图调度由关键路径决定（理论上更快），预算放宽到 60s；到点未完成的项如实标「未取到」。
     */
    private static final long GRAPH_TOTAL_BUDGET_MS = 60000L;

    /** 图节点状态机：PENDING（等依赖）→ RUNNING（执行中）→ DONE / FAILED */
    private enum NodeState { PENDING, RUNNING, DONE, FAILED }

    /**
     * 依赖图节点：主项与并列需求**统一建模** —— 主项=n0、无依赖 → 首轮即就绪，
     * 没有「主项先单独跑」的特例（.10~.12 三个断点同源于「plan 里含主项」的错误假设，这里从结构上消除）。
     * ★nodeId == 模型 dependsOn 的全局下标（0=主项；提示词约定归因项 dependsOn=[0]），天然对齐无需换算。
     */
    private static class SchedNode {
        final int id;
        AskIntentResult need;
        /** 已收口的合法依赖（建图时丢弃非法引用；指代主项但漏标的补虚拟依赖 0） */
        final java.util.Set<Integer> deps = new java.util.LinkedHashSet<>();
        NodeState state = NodeState.PENDING;
        NeedOutcome outcome;
        /** C-2 防环标志：每个节点最多修正重试一次，再失败就如实「未取到」 */
        boolean retried;

        SchedNode(int id, AskIntentResult need) {
            this.id = id;
            this.need = need;
        }
    }

    /**
     * 调度层 LLM 决策预算（.14 第②步）：一次提问内，三个决策点（C-1 回填纠偏 / C-2 失败重路由 /
     * C-3 缺口补取）共用同一本账。
     *
     * <p>为什么必须有：每个决策点都是一次真实 LLM 往返（秒级），不设上限的话最坏情况
     * 一次提问拖出十几次调用，时延与费用都失控。上限：≤{@link #MAX_CALLS} 次决策调用、
     * ≤{@link #MAX_ROUNDS} 轮重调度（C-2 修正重试 / C-3 缺口补取各占一轮）；
     * 预算耗尽 → 一律走原确定性路径（.13 行为），不会更差。
     */
    private static class DecisionBudget {
        /** LLM 决策调用上限（次/问） */
        static final int MAX_CALLS = 5;
        /** 重调度轮数上限（轮/问） */
        static final int MAX_ROUNDS = 3;

        private int calls;
        private int rounds;

        /** 消耗一次 LLM 决策调用；预算尽 → false */
        boolean tryCall() {
            if (calls >= MAX_CALLS) {
                return false;
            }
            calls++;
            return true;
        }

        /** 消耗一轮重调度（C-2 / C-3 各算一轮）；预算尽 → false */
        boolean tryRound() {
            if (rounds >= MAX_ROUNDS) {
                return false;
            }
            rounds++;
            return true;
        }

        boolean roundLeft() {
            return rounds < MAX_ROUNDS;
        }

        int callsUsed() {
            return calls;
        }

        int roundsUsed() {
            return rounds;
        }
    }

    /**
     * 依赖图事件驱动调度。循环 = 唤醒扫描（依赖全 DONE 的 PENDING → ready）→ 提交前逐个从 DONE
     * 依赖回填主对象 → 整波提交并发 → waitAny 等任一完成 → 完成事件 → 回到扫描，直到全部落定或超预算。
     *
     * <p>确定性收口（LLM 声明可错，代码兜底，最差退化为旧行为）：
     * ① dependsOn 非法引用（null/越界/自环）→ 丢弃该声明降级并行；
     * ② 指代主项但漏标 dependsOn → 补虚拟依赖 n0（实测「哪道工序导致该产品…」漏标概率性出现）；
     * ③ 环 / 前置失败 → 下游如实标「未取到」，失败隔离不拖垮整答；
     * ④ 有依赖声明的项不参与 mergeSameScope（合并改下标会让 dependsOn 对不上），无依赖时维持旧合并收口。
     *
     * <p>输出顺序**按节点原下标**（不按完成时间），骨架层「第 i+1 项」标签稳定可复现。
     */
    private List<NeedOutcome> scheduleGraph(String question, String sessionId,
                                            List<AskIntentResult> graphNodes, DecisionBudget budget) {
        // 有依赖声明的项存在 → 跳过合并（保下标）；全无依赖 → 维持旧合并收口（对并列需求子列表做，与旧一致）
        boolean anyDepMarked = false;
        for (int i = 1; i < graphNodes.size(); i++) {
            if (graphNodes.get(i).getDependsOn() != null && !graphNodes.get(i).getDependsOn().isEmpty()) {
                anyDepMarked = true;
                break;
            }
        }
        if (!anyDepMarked && graphNodes.size() > 1) {
            List<AskIntentResult> merged =
                    mergeSameScope(new java.util.ArrayList<>(graphNodes.subList(1, graphNodes.size())));
            if (merged.size() != graphNodes.size() - 1) {
                List<AskIntentResult> rebuilt = new java.util.ArrayList<>(graphNodes.subList(0, 1));
                rebuilt.addAll(merged);
                graphNodes = rebuilt;
            }
        }
        // 建节点 + 依赖声明收口
        List<SchedNode> nodes = new java.util.ArrayList<>();
        for (int i = 0; i < graphNodes.size(); i++) {
            AskIntentResult need = graphNodes.get(i);
            SchedNode node = new SchedNode(i, need);
            if (need.getDependsOn() != null) {
                for (Integer d : need.getDependsOn()) {
                    if (d == null || d < 0 || d >= graphNodes.size() || d.intValue() == i) {
                        log.warn("[AI调度] {} dependsOn={} 非法（越界/自环）→ 丢弃该声明，降级并行", needTag(i), d);
                        continue;
                    }
                    node.deps.add(d);
                }
            }
            // 模型有时不给指代项标 dependsOn → 代码确定性兜底：指代主项且没声明任何依赖 → 挂到 n0
            if (i != 0 && node.deps.isEmpty() && referencesPrimary(need)) {
                node.deps.add(0);
                log.info("[AI调度] {} 指代主项（代码判定）→ 挂起等待主项完成", needTag(i));
            }
            nodes.add(node);
        }
        java.util.Map<Integer, java.util.concurrent.Future<NeedOutcome>> running = new java.util.LinkedHashMap<>();
        long deadline = System.currentTimeMillis() + GRAPH_TOTAL_BUDGET_MS;
        while (true) {
            // ── 唤醒扫描：依赖全 DONE 的 PENDING → ready；前置 FAILED → 下游如实标未取到（失败隔离）──
            List<SchedNode> ready = new java.util.ArrayList<>();
            for (SchedNode node : nodes) {
                if (node.state != NodeState.PENDING) {
                    continue;
                }
                boolean blocked = false;
                boolean depFailed = false;
                for (Integer d : node.deps) {
                    NodeState ds = nodes.get(d).state;
                    if (ds == NodeState.FAILED) {
                        depFailed = true;
                    } else if (ds != NodeState.DONE) {
                        blocked = true;
                    }
                    if (depFailed || blocked) {
                        break;
                    }
                }
                if (depFailed) {
                    node.state = NodeState.FAILED;
                    node.outcome = notFetched(node.need, "该项未取到（依赖的前置未完成）");
                    log.info("[AI调度] {} 前置未完成 → 如实标记，不拖垮整答", needTag(node.id));
                } else if (!blocked) {
                    // 提交前回填（确定性，无 LLM）：从每个 DONE 依赖把主对象写进实体槽
                    for (Integer d : node.deps) {
                        backfillFromNode(nodes.get(d), node, budget);
                    }
                    ready.add(node);
                }
            }
            // ── 整波提交：本轮就绪的一起并发跑（该并行的并行；并发度由 NEED_POOL 上限保证）──
            for (SchedNode node : ready) {
                node.state = NodeState.RUNNING;
                running.put(node.id, submitNeed(question, sessionId, node.need, node.id));
            }
            if (running.isEmpty()) {
                // 无在跑且无可提交 → 剩余 PENDING 全是环/依赖无法满足 → 收口
                for (SchedNode node : nodes) {
                    if (node.state == NodeState.PENDING) {
                        node.state = NodeState.FAILED;
                        node.outcome = notFetched(node.need, "该项未取到（依赖无法满足）");
                        log.warn("[AI调度] {} 依赖无法满足（环/前置缺失）→ 如实标记", needTag(node.id));
                    }
                    if (node.outcome == null) {
                        node.outcome = notFetched(node.need, "该项未取到（内部错误）");
                    }
                }
                // ── C-3 缺口补取：全部落定 → LLM 对照原问题判缺口 → 不足则补一个需求重入调度 ──
                if (System.currentTimeMillis() < deadline && gapFillAndAppend(question, nodes, budget)) {
                    continue;
                }
                break;
            }
            if (System.currentTimeMillis() >= deadline) {
                // 总预算到点：取消在跑项、未启动项如实标记（防失控，不是目标时延）
                log.warn("[AI调度] 总预算 {} ms 到点 → 未完成项如实标记", GRAPH_TOTAL_BUDGET_MS);
                for (java.util.Map.Entry<Integer, java.util.concurrent.Future<NeedOutcome>> e : running.entrySet()) {
                    e.getValue().cancel(true);
                    SchedNode node = nodes.get(e.getKey());
                    node.state = NodeState.FAILED;
                    node.outcome = notFetched(node.need, "该项未取到（总时延预算到点）");
                }
                running.clear();
                for (SchedNode node : nodes) {
                    if (node.state == NodeState.PENDING) {
                        node.state = NodeState.FAILED;
                        node.outcome = notFetched(node.need, "该项未取到（总时延预算到点）");
                    }
                    if (node.outcome == null) {
                        node.outcome = notFetched(node.need, "该项未取到（内部错误）");
                    }
                }
                break;
            }
            // ── waitAny：等任一完成（该等待的等待），完成事件驱动下一轮扫描 ──
            waitAny(running, nodes, deadline);
            // ── C-2 失败重路由：本项自身取数失败 → 预算内一次 LLM 修正后重试（.14 决策点）──
            retryFailed(question, nodes, budget, deadline);
        }
        log.info("[AI决策点] 图调度结束：{} 节点｜LLM 决策调用 {}/{} 次、重调度 {}/{} 轮",
                nodes.size(), budget.callsUsed(), DecisionBudget.MAX_CALLS,
                budget.roundsUsed(), DecisionBudget.MAX_ROUNDS);
        // 输出按原下标（不按完成时间），骨架「第 i+1 项」标签稳定可复现
        List<NeedOutcome> out = new java.util.ArrayList<>(nodes.size());
        for (SchedNode node : nodes) {
            out.add(node.outcome != null ? node.outcome : notFetched(node.need, "该项未取到（内部错误）"));
        }
        return out;
    }

    /**
     * 等待任一在跑任务落定（50ms 轮询，deadline 感知）；返回前收割**所有**已落定项（完成/取消都算）。
     * 单项超时沿用 {@link #NEED_TIMEOUT_MS}：只取消超时项自己，不连坐其它项。
     * 单项业务异常已在 submitNeed 内兜成 notFetched outcome，这里只需取回。
     */
    private void waitAny(java.util.Map<Integer, java.util.concurrent.Future<NeedOutcome>> running,
                         List<SchedNode> nodes, long deadline) {
        long waveStart = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() < deadline) {
                boolean anySettled = false;
                for (java.util.Map.Entry<Integer, java.util.concurrent.Future<NeedOutcome>> e : running.entrySet()) {
                    java.util.concurrent.Future<NeedOutcome> f = e.getValue();
                    if (!f.isDone() && System.currentTimeMillis() - waveStart >= NEED_TIMEOUT_MS) {
                        log.warn("[AI需求] {} 取数超时（{} ms）→ 该项未取到，不影响其它项",
                                needTag(nodes.get(e.getKey()).id), NEED_TIMEOUT_MS);
                        f.cancel(true);
                    }
                    if (f.isDone()) {
                        anySettled = true;
                    }
                }
                if (anySettled) {
                    break;
                }
                Thread.sleep(50L);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        java.util.Iterator<java.util.Map.Entry<Integer, java.util.concurrent.Future<NeedOutcome>>> it =
                running.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<Integer, java.util.concurrent.Future<NeedOutcome>> e = it.next();
            java.util.concurrent.Future<NeedOutcome> f = e.getValue();
            if (!f.isDone()) {
                continue;   // 还在跑，留给下一轮
            }
            it.remove();
            SchedNode node = nodes.get(e.getKey());
            NeedOutcome outcome;
            if (f.isCancelled()) {
                outcome = notFetched(node.need, "该项未取到（取数超时）");
            } else {
                try {
                    outcome = f.get();
                } catch (Exception ex) {
                    log.warn("[AI需求] {} 取数失败：{}", needTag(node.id), ex.toString());
                    outcome = notFetched(node.need, "该项未取到（取数失败）");
                }
            }
            node.outcome = outcome;
            node.state = outcome.error != null ? NodeState.FAILED : NodeState.DONE;
            log.info("[AI调度] {} 完成（{}）→ 唤醒扫描下游", needTag(node.id),
                    node.state == NodeState.FAILED ? "未取到" : "成功");
        }
    }

    /** 统一日志口径：nodeId 从 0 起（0=主项 →「第 1 项」），与解析器/骨架层「第 i+1 项」编号一致 */
    private String needTag(Integer nodeId) {
        return nodeId == null ? "主需求" : ("第 " + (nodeId + 1) + " 项");
    }

    /* ────────── .14 调度层 LLM 决策点（预算内生效，缺席一律退回确定性路径）────────── */

    /**
     * C-2 失败重路由：本项自身取数失败（超时/异常/未产出）→ 预算内一次 LLM 修正需求后重试。
     *
     * <p>为什么 LLM 只当「修参数的」：失败多为时间窗/过滤/分组选错（如该按年查却给了月窗），
     * LLM 看得懂失败原因并改槽位；要不要重试、重试后槽位是否合法仍由代码收口
     * （retried 标志防环：每节点最多修正一次；失败类型不可重试的——依赖级联/预算到点——直接跳过）。
     * 决策缺席（LLM 挂了/输出解析不出/giveup）→ 保持 FAILED 如实标注，最差退化为 .13 行为。
     */
    private void retryFailed(String question, List<SchedNode> nodes, DecisionBudget budget, long deadline) {
        for (SchedNode node : nodes) {
            if (node.state != NodeState.FAILED || node.retried || node.outcome == null
                    || !retryableError(node.outcome.error) || System.currentTimeMillis() >= deadline) {
                continue;
            }
            if (!budget.tryCall() || !budget.tryRound()) {
                log.info("[AI决策点] C-2 预算耗尽 → {} 保持失败（如实标注）", needTag(node.id));
                return;
            }
            node.retried = true;
            AskIntentResult fixed = llmRefineNeed(question, node);
            if (fixed == null) {
                log.info("[AI决策点] C-2 决策缺席/放弃修正 → {} 保持失败（如实标注）", needTag(node.id));
                continue;
            }
            log.info("[AI决策点] C-2 失败重路由生效：{} 修正后重试（原失败：{}）",
                    needTag(node.id), node.outcome.error);
            node.need = fixed;
            node.state = NodeState.PENDING;
            node.outcome = null;   // 下一轮扫描：依赖已 DONE → 回填 → 重新提交
        }
    }

    /** C-2 可重试的失败类型：本项自身的取数失败（依赖级联/总预算到点重试无意义，不进） */
    private boolean retryableError(String error) {
        if (!StringUtils.hasText(error)) {
            return false;
        }
        return error.contains("取数失败") || error.contains("取数超时") || error.contains("取数异常")
                || error.contains("执行异常") || error.contains("未产出结果");
    }

    /** C-2 的 LLM 修正：看失败原因改需求槽位（时间/过滤/分组/取数方式），或明确放弃（返回 null） */
    private AskIntentResult llmRefineNeed(String question, SchedNode node) {
        AskIntentResult need = node.need;
        String sys = "【你在什么系统里】企业生产管理系统的问数助手刚有一条数据查询失败了，"
                + "你负责修正这条需求，让它更可能查到数据。\n"
                + "需求字段约束：intent=能力code或NOT_SUPPORTED；how 只能 operator/registered/self/compute；"
                + "groupBy 只能 day/product/process/employee 或 null；"
                + "time.type 只能 today/month/year/custom（custom 必须给 startDate/endDate）；"
                + "filter/entities 的实体名用用户原话。\n"
                + "只输出 JSON，不要解释。";
        String user = "【用户原问题】" + question + "\n"
                + "【失败的需求】" + needJson(need) + "\n"
                + "【失败原因】" + node.outcome.error + "\n"
                + "【常见修法】时间窗放宽（month→year）；去掉查不到数据的过滤条件；"
                + "groupBy 与能力不匹配时去掉；实体名保留原样；registered 反复空结果可改 how=self 自行生成查询。\n"
                + "若这条需求本身无法成立（查什么都不会有），action 给 giveup。\n"
                + "输出：{\"action\":\"retry|giveup\",\"reason\":\"一句话理由\","
                + "\"need\":{\"want\":…,\"intent\":…,\"how\":…,\"groupBy\":…,\"filter\":{},\"entities\":{},"
                + "\"time\":{\"type\":…,\"startDate\":…,\"endDate\":…},"
                + "\"order\":{\"by\":…,\"dir\":…,\"limit\":…},\"statScope\":\"production\"}}";
        com.alibaba.fastjson.JSONObject o = llmIntentResolver.decideJson(sys, user);
        if (o == null || !"retry".equalsIgnoreCase(o.getString("action"))) {
            return null;
        }
        com.alibaba.fastjson.JSONObject n = o.getJSONObject("need");
        if (n == null) {
            return null;
        }
        // 修正需求以原需求为底（LLM 只需给要改的槽位；空缺槽位继承原值，applySlot 覆盖给了的）
        AskIntentResult fixed = new AskIntentResult();
        fixed.setIntent(need.getIntent());
        fixed.setWant(need.getWant());
        fixed.setHow(need.getHow());
        fixed.setRoute(need.getRoute());
        fixed.setRouteReason(need.getRouteReason());
        fixed.setGroupBy(need.getGroupBy());
        fixed.setStatScope(need.getStatScope());
        fixed.setTimeType(need.getTimeType());
        fixed.setStartDate(need.getStartDate());
        fixed.setEndDate(need.getEndDate());
        fixed.setOrderBy(need.getOrderBy());
        fixed.setOrderDir(need.getOrderDir());
        fixed.setLimit(need.getLimit());
        fixed.setTask(need.getTask());
        fixed.setQuestion(need.getQuestion());
        fixed.setSource(need.getSource());
        if (need.getFilter() != null) {
            fixed.getFilter().putAll(need.getFilter());
        }
        if (need.getEntities() != null) {
            fixed.getEntities().putAll(need.getEntities());
        }
        fixed.getDependsOn().addAll(need.getDependsOn());
        llmIntentResolver.applySlot(fixed, n);
        if (!StringUtils.hasText(fixed.getHow())) {
            fixed.setHow("registered");
        }
        log.info("[AI决策点] C-2 修正需求：intent={} how={} groupBy={} 时间={}~{} filter={}（理由：{}）",
                fixed.getIntent(), fixed.getHow(), fixed.getGroupBy(),
                fixed.getStartDate(), fixed.getEndDate(), fixed.getFilter(), o.getString("reason"));
        return fixed;
    }

    /** 需求槽位的紧凑 JSON（喂给 LLM 决策用：只给关键字段，不带内部审计字段） */
    private String needJson(AskIntentResult need) {
        com.alibaba.fastjson.JSONObject o = new com.alibaba.fastjson.JSONObject();
        o.put("want", need.getWant());
        o.put("intent", need.getIntent());
        o.put("how", need.getHow());
        o.put("groupBy", need.getGroupBy());
        if (need.getFilter() != null && !need.getFilter().isEmpty()) {
            o.put("filter", need.getFilter());
        }
        if (need.getEntities() != null && !need.getEntities().isEmpty()) {
            o.put("entities", need.getEntities());
        }
        com.alibaba.fastjson.JSONObject time = new com.alibaba.fastjson.JSONObject();
        time.put("type", need.getTimeType());
        time.put("startDate", need.getStartDate());
        time.put("endDate", need.getEndDate());
        o.put("time", time);
        return o.toJSONString();
    }

    /**
     * C-3 缺口补取：全部节点落定后，让 LLM 对照原问题检查各段数据是否足以回答；
     * 不足且预算允许 → 产出**一个**补充需求，追加为图节点并重入事件循环。
     *
     * <p>为什么只补一个：缺口通常是「答案里缺一段关键数据」（如漏了对比基期、
     * 问题点名的对象没有任何一段讲到），一个补充节点足够；多补会逼近预算上限且稀释合成重点。
     * 追加节点的 dependsOn 由代码收口：非法/越界/自环/指向未取到的项 → 丢弃该声明（并行跑）。
     *
     * @return true=追加了补充节点（调用方重入事件循环）
     */
    private boolean gapFillAndAppend(String question, List<SchedNode> nodes, DecisionBudget budget) {
        if (!budget.roundLeft() || !budget.tryCall()) {
            return false;
        }
        StringBuilder digest = new StringBuilder();
        for (SchedNode node : nodes) {
            String ans = node.outcome != null && node.outcome.vo != null ? node.outcome.vo.getAnswer() : null;
            if (StringUtils.hasText(ans) && ans.length() > 160) {
                ans = ans.substring(0, 160) + "…";
            }
            digest.append("第 ").append(node.id + 1).append(" 项")
                    .append(node.state == NodeState.DONE ? "（已取到）" : "（未取到）")
                    .append("：").append(node.need == null ? "" : node.need.getWant())
                    .append(" → ").append(StringUtils.hasText(ans) ? ans : "(无数据)").append("\n");
        }
        String sys = "【你在什么系统里】企业生产管理系统的问数助手已按计划取完数，"
                + "你负责检查：已取到的数据**是否足以回答用户的问题**。\n"
                + "判断标准：回答里是否还缺一段**必须查库才能补上**的关键数据"
                + "（如问题点名的对象/维度没有任何一段讲到、缺对比基期）。\n"
                + "数据齐了、或缺口无法靠查库补足（如需要人话解释）→ gap=false。\n"
                + "需求字段约束：intent=能力code或NOT_SUPPORTED；how 只能 operator/registered/self/compute；"
                + "groupBy 只能 day/product/process/employee 或 null；"
                + "time.type 只能 today/month/year/custom（custom 必须给 startDate/endDate）；"
                + "dependsOn 下标从 0 起（0=第 1 项），只能指向已取到数据的项。\n"
                + "只输出 JSON，不要解释。";
        String user = "【用户原问题】" + question + "\n【已取到的数据】\n" + digest
                + "【输出】足以回答 → {\"gap\":false}\n"
                + "缺一段数据 → {\"gap\":true,\"reason\":\"缺什么\","
                + "\"need\":{\"want\":…,\"intent\":…,\"how\":…,\"groupBy\":…,\"filter\":{},\"entities\":{},"
                + "\"time\":{\"type\":…,\"startDate\":…,\"endDate\":…},"
                + "\"order\":{\"by\":…,\"dir\":…,\"limit\":…},\"statScope\":\"production\",\"dependsOn\":[]}}";
        com.alibaba.fastjson.JSONObject o = llmIntentResolver.decideJson(sys, user);
        if (o == null || !o.getBooleanValue("gap")) {
            log.info("[AI决策点] C-3 缺口检查：数据足以回答（或决策缺席）→ 不补取");
            return false;
        }
        com.alibaba.fastjson.JSONObject n = o.getJSONObject("need");
        if (n == null) {
            log.info("[AI决策点] C-3 判定有缺口但未给补充需求 → 不补取");
            return false;
        }
        AskIntentResult extra = new AskIntentResult();
        llmIntentResolver.applySlot(extra, n);
        if (!StringUtils.hasText(extra.getHow())) {
            extra.setHow("registered");
        }
        if (!StringUtils.hasText(extra.getIntent()) && !"self".equals(extra.getHow())) {
            log.info("[AI决策点] C-3 补充需求缺 intent → 不补取");
            return false;
        }
        budget.tryRound();
        SchedNode node = new SchedNode(nodes.size(), extra);
        int size = nodes.size();
        if (extra.getDependsOn() != null) {
            for (Integer d : extra.getDependsOn()) {
                if (d == null || d < 0 || d >= size || d.intValue() == node.id) {
                    log.warn("[AI决策点] C-3 补充项 dependsOn={} 非法 → 丢弃该声明", d);
                    continue;
                }
                if (nodes.get(d).state != NodeState.DONE) {
                    log.warn("[AI决策点] C-3 补充项 dependsOn={} 指向未取到的项 → 丢弃该声明（并行跑）", d);
                    continue;
                }
                node.deps.add(d);
            }
        }
        nodes.add(node);
        log.info("[AI决策点] C-3 缺口补取生效（{}）：追加第 {} 项「{}」how={} → 重入调度",
                o.getString("reason"), node.id + 1, extra.getWant(), extra.getHow());
        return true;
    }

    /**
     * 依赖回填（确定性，无 LLM）：「该产品良品率下降主要来自哪道工序」这类依赖项，
     * 「该产品」是代词——字典查不出、维度兜底 guessDim 会认错（实测被跑成与主项相同的查询）。
     * 前置节点（如主项）答案里的主对象就是它指代的对象 → 回填进依赖项实体槽，
     * 让执行层走「有产品过滤 → 按工序分解」的现成分支，精确回答该产品内部的归因。
     *
     * <p>.13 统一图版：主项与并列需求同为图节点，回填发生在「依赖全 DONE → 节点就绪」的提交前，
     * 槽位判定 = 指代词优先（「该产品」→ 产品槽）、无指代词但依赖该前置的按前置 groupBy 对应槽兜底。
     * 已有具体实体名不覆盖；主对象取前置答案首个「」引号名（跳过日期/百分比类引号内容）。
     * 回填名过不了字典查证时会被 resolveEntities 清掉、退回原维度兜底——最差等同旧行为，不会更差。
     */
    private void backfillFromNode(SchedNode src, SchedNode target, DecisionBudget budget) {
        if (src == null || target == null || target.need == null
                || src.outcome == null || src.outcome.vo == null
                || !StringUtils.hasText(src.outcome.vo.getAnswer())) {
            return;
        }
        boolean depSrc = target.deps.contains(src.id);
        String field = referencedField(target.need);
        if (field == null && depSrc && src.need != null) {
            String groupBy = src.need.getGroupBy();
            field = "product".equalsIgnoreCase(groupBy) ? "productNameOrCode"
                    : "process".equalsIgnoreCase(groupBy) ? "processNameOrCode"
                    : "employee".equalsIgnoreCase(groupBy) ? "employeeName" : null;
        }
        if (field == null) {
            return;
        }
        if (StringUtils.hasText(target.need.getEntities().get(field))) {
            return;   // 已解析出具体名，不覆盖
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("「([^「」]{2,20})」").matcher(src.outcome.vo.getAnswer());
        String name = null;
        while (m.find()) {
            String cand = m.group(1).trim();
            if (cand.matches(".*\\d{4}.*") || cand.matches("\\d+(\\.\\d+)?%?")) {
                continue;   // 跳过日期/纯数字类引号内容（如「2026-08」）
            }
            name = cand;
            break;
        }
        if (!StringUtils.hasText(name)) {
            return;
        }
        // ── C-1 回填纠偏（.14 决策点）：回填名先过字典；NOT_FOUND 时预算内一次 LLM 从候选里重选 ──
        //   纠偏失败仍写原名（后续 runNeed 的 resolveEntities 查证失败 → 如实披露），最差退化为 .13 行为。
        EntityResolver.Resolved er = resolveQuietly(target.need, field, name);
        if (er != null && er.status == EntityResolver.Status.OK) {
            target.need.getEntities().put(field, er.value);
            log.info("[AI调度] 依赖完成回填 {}=「{}」→ {}（字典命中）", field, er.value, needTag(target.id));
            return;
        }
        if (er != null && er.status == EntityResolver.Status.NOT_FOUND && budget.tryCall()) {
            String better = llmPickEntity(src.outcome.vo.getAnswer(), field, target.need.getWant());
            if (StringUtils.hasText(better) && !better.equals(name)) {
                EntityResolver.Resolved er2 = resolveQuietly(target.need, field, better);
                if (er2 != null && er2.status == EntityResolver.Status.OK) {
                    target.need.getEntities().put(field, er2.value);
                    log.info("[AI决策点] C-1 回填纠偏生效：「{}」字典未命中 → LLM 改选「{}」→ {}（{}）",
                            name, er2.value, needTag(target.id), field);
                    return;
                }
            }
            log.info("[AI决策点] C-1 回填纠偏未找到更好候选 → 维持原回填「{}」（查证失败将如实披露）", name);
        }
        target.need.getEntities().put(field, name);
        log.info("[AI调度] 依赖完成回填 {}=「{}」→ {}", field, name, needTag(target.id));
    }

    /** 字典查证的静默包装（异常只打日志返回 null；C-1 只想「试一下」，不让字典故障打断调度） */
    private EntityResolver.Resolved resolveQuietly(AskIntentResult need, String field, String raw) {
        try {
            return entityResolver.resolve(need, field, raw);
        } catch (Exception e) {
            log.warn("[AI决策点] C-1 字典预查证异常（忽略）：{}", e.getMessage());
            return null;
        }
    }

    /**
     * C-1 的 LLM 选名：前置答案里的引号内容有时是数值/日期/别的对象，
     * 正则抽出的第一个名字字典查无时，让 LLM 从候选里挑一个最像真实名称的。
     * 失败/解析不出返回 null（调用方维持原回填）。
     */
    private String llmPickEntity(String srcAnswer, String field, String want) {
        java.util.LinkedHashSet<String> cands = new java.util.LinkedHashSet<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("「([^「」]{2,20})」").matcher(srcAnswer);
        while (m.find() && cands.size() < 8) {
            cands.add(m.group(1).trim());
        }
        if (cands.isEmpty()) {
            return null;
        }
        String fieldCn = "productNameOrCode".equals(field) ? "产品"
                : "processNameOrCode".equals(field) ? "工序" : "员工";
        String sys = "【你在什么系统里】企业生产管理系统的问数助手正在做「回填纠偏」："
                + "从上一步查询答案里抽出的主对象名，在字典里没查到，需要你从候选里重新挑一个。\n"
                + "只输出 JSON，不要解释。";
        String user = "【下一步数据需求】" + (want == null ? "(未说明)" : want) + "\n"
                + "它需要限定" + fieldCn + "（该槽位抽出的名字没通过字典查证）。\n"
                + "【候选名】" + String.join("、", cands) + "\n"
                + "【要求】挑一个**最像真实" + fieldCn + "名称**的候选（剔除日期、百分比、指标数值）；"
                + "没有合适的就给空字符串。\n"
                + "输出：{\"name\":\"候选中的某一个或空字符串\"}";
        com.alibaba.fastjson.JSONObject o = llmIntentResolver.decideJson(sys, user);
        return o == null ? null : o.getString("name");
    }

    /**
     * 指代词 → 实体槽字段名：want 含「该/此/这/那（个/款/种/道）+ 产品/工序/员工」时，
     * 返回它指代的实体槽（如「该产品」→ productNameOrCode）；无指代词返回 null。
     * 判定保守：只认显式指示词；「哪个产品」这类疑问词不匹配（"哪"不在指示词表里）。
     */
    private String referencedField(AskIntentResult need) {
        if (need == null || !StringUtils.hasText(need.getWant())) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("[该此这那][个款种道]?(产品|工序|员工)").matcher(need.getWant());
        if (!m.find()) {
            return null;
        }
        String dim = m.group(1);
        // dim 是正则捕获的中文（产品/工序/员工），必须用中文比对——
        // 实测用英文"product"比对永远不等 → 全部掉进 employeeName 兜底（产品名进员工槽）
        return "产品".equals(dim) ? "productNameOrCode"
                : "工序".equals(dim) ? "processNameOrCode" : "employeeName";
    }

    /**
     * 确定性判定：该项是否在指代主项的对象（指代词命中 + 对应实体槽还是空的）。
     * 为什么不能等模型标 dependsOn：实测同一措辞模型有时标有时不标
     * （本项目原则：提示词约束不可靠，分批/回填由代码收口）。
     */
    private boolean referencesPrimary(AskIntentResult need) {
        String field = referencedField(need);
        return field != null && !StringUtils.hasText(need.getEntities().get(field));
    }

    /**
     * **同一件事只取一次**（确定性合并，不依赖模型自觉）：时间窗 + 过滤 + 分组**完全相同**的多项需求，
     * 合并成一项取数，把各自的"想要什么"并进限定条件。
     *
     * <p>为什么必须做：实测同一句「本月的返修率和报废率」，模型有时正确地列成 1 项、
     * 有时拆成 2 项；拆开后每条只生成一列 SQL（`SELECT repair_rate`），另一条没产出
     * → 答案变成"报废率未取到"✗（其实库里两个都是 0 ✓）。这类"同一件事被拆成两件"必须由代码收口。
     *
     * <p>合并只改提示词里的一句话（限定条件），**不合并 `time`/`filter`/`groupBy` 本身**：
     * 三者相同是合并的前提，所以合并后语义不变。
     */
    private List<AskIntentResult> mergeSameScope(List<AskIntentResult> needs) {
        if (needs == null || needs.size() < 2) {
            return needs == null ? new java.util.ArrayList<>() : needs;
        }
        // 无论是否合并都打一行：让"这段兜底到底走没走到、走成了什么"在日志里可证
        // （历史痛点：逻辑写了却没被触发过，事后无法判断是"没生效"还是"没遇上场景"）
        int asked = needs.size();
        List<AskIntentResult> out = new java.util.ArrayList<>();
        java.util.Set<Integer> merged = new java.util.LinkedHashSet<>();
        for (int i = 0; i < needs.size(); i++) {
            if (merged.contains(i)) {
                continue;
            }
            AskIntentResult base = needs.get(i);
            StringBuilder wants = new StringBuilder(base.getWant() == null ? "" : base.getWant());
            for (int j = i + 1; j < needs.size(); j++) {
                if (merged.contains(j)) {
                    continue;
                }
                AskIntentResult other = needs.get(j);
                if (other.getDependsOn() != null && !other.getDependsOn().isEmpty()) {
                    continue;   // 有依赖的项单独跑，不参与合并（它可能要等前置）
                }
                if (sameScope(base, other)) {
                    merged.add(j);
                    if (StringUtils.hasText(other.getWant())) {
                        wants.append("；").append(other.getWant());
                    }
                    log.info("[AI需求] 第 {} 项与第 {} 项同时间窗/同过滤/同分组 → 合并为一次取数（{}）",
                            i + 1, j + 1, other.getWant());
                }
            }
            if (merged.contains(i)) {
                continue;
            }
            AskIntentResult mergedNeed = base;
            if (!wants.toString().equals(base.getWant() == null ? "" : base.getWant())) {
                mergedNeed = copyWithWant(base, wants.toString());
            }
            out.add(mergedNeed);
        }
        log.info("[AI需求] 合并收口：{}{}", out.size() == asked
                        ? "本次无需合并（" + asked + " 项，时间窗/过滤/分组各不相同）"
                        : "合并生效 " + asked + " 项 → " + out.size() + " 项（同时间窗/同过滤/同分组）",
                needs.size() >= 2 ? "" : "");
        return out;
    }

    /** 两个需求是否"同一件事"：时间窗、过滤、分组完全一致 */
    private boolean sameScope(AskIntentResult a, AskIntentResult b) {
        if (a == null || b == null) {
            return false;
        }
        if (!eq(a.getStartDate(), b.getStartDate()) || !eq(a.getEndDate(), b.getEndDate())) {
            return false;
        }
        if (!eq(a.getGroupBy(), b.getGroupBy())) {
            return false;
        }
        java.util.Map<String, String> f1 = a.getFilter() == null ? java.util.Collections.emptyMap() : a.getFilter();
        java.util.Map<String, String> f2 = b.getFilter() == null ? java.util.Collections.emptyMap() : b.getFilter();
        return f1.equals(f2);
    }

    private boolean eq(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }

    /** 复制槽位并把合并后的"想要什么"写进 want（原对象不原地改，避免影响其它引用） */
    private AskIntentResult copyWithWant(AskIntentResult src, String want) {
        AskIntentResult copy = new AskIntentResult();
        copy.setIntent(src.getIntent());
        copy.setHow(src.getHow());
        copy.setRoute(src.getRoute());
        copy.setRouteReason(src.getRouteReason());
        copy.setGroupBy(src.getGroupBy());
        copy.setStatScope(src.getStatScope());
        copy.setTimeType(src.getTimeType());
        copy.setStartDate(src.getStartDate());
        copy.setEndDate(src.getEndDate());
        copy.setOrderBy(src.getOrderBy());
        copy.setOrderDir(src.getOrderDir());
        copy.setLimit(src.getLimit());
        copy.setMode(src.getMode());
        copy.setTask(src.getTask());
        copy.setQuestion(src.getQuestion());
        copy.setSource(src.getSource());
        copy.setWant(want);
        if (src.getFilter() != null) {
            copy.getFilter().putAll(src.getFilter());
        }
        if (src.getEntities() != null) {
            copy.getEntities().putAll(src.getEntities());
        }
        if (src.getDependsOn() != null) {
            copy.getDependsOn().addAll(src.getDependsOn());
        }
        return copy;
    }

    private java.util.concurrent.Future<NeedOutcome> submitNeed(String question, String sessionId,
                                                               AskIntentResult need, Integer index) {
        final String tenant = tenantCodeOf();   // ★ ThreadLocal 不跨线程：租户上下文在主线程取出后显式带给子任务
        return NEED_POOL.submit(() -> {
            String oldTenant = null;
            try {
                Object v = com.cosmo.hhim.common.core.threadlocal.ThreadContext.get(
                        com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
                oldTenant = v == null ? null : v.toString();
                com.cosmo.hhim.common.core.threadlocal.ThreadContext.put(
                        com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER, tenant);
                SUPPRESS_PERSIST.set(Boolean.TRUE);   // 子任务内不落库（跨线程也要显式设）
                return runNeed(question, sessionId, need, index);
            } catch (Exception e) {
                log.warn("[AI需求] {} 取数异常：{}", needTag(index), e.toString());
                return notFetched(need, "该项未取到（取数异常）");
            } finally {
                SUPPRESS_PERSIST.remove();
                if (oldTenant != null) {
                    com.cosmo.hhim.common.core.threadlocal.ThreadContext.put(
                            com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER, oldTenant);
                } else {
                    com.cosmo.hhim.common.core.threadlocal.ThreadContext.remove(
                            com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
                }
            }
        });
    }

    /** 未取到时的如实说明（不静默丢弃：用户能看到"这项没查到"） */
    private NeedOutcome notFetched(AskIntentResult need, String reason) {
        AskReplyVO vo = new AskReplyVO();
        vo.setIntent(need == null ? "NOT_SUPPORTED" : need.getIntent());
        vo.setSource("NONE");
        vo.setFallback(true);
        vo.setAnswer(reason + (need != null && StringUtils.hasText(need.getWant()) ? "：" + need.getWant() : ""));
        return new NeedOutcome(vo, need, reason);
    }

    /**
     * 执行**一项数据需求**：按 how 选通道（operator / registered / self / compute），
     * 走与单需求问题完全相同的闸门与执行器 —— 只是**不落库**、也不做逐项润色（最后统一润色一次）。
     */
    private NeedOutcome runNeed(String question, String sessionId, AskIntentResult need, Integer index) {
        if (need == null) {
            return notFetched(null, "该项未取到（需求为空）");
        }
        String howHint = need.getHow() == null ? "" : need.getHow().toLowerCase();
        // how=self 的项允许没有登记能力 code（它就是"登记能力都不合适"的意思）→ 不当作空需求丢弃
        if (!StringUtils.hasText(need.getIntent()) && !"self".equals(howHint)) {
            return notFetched(need, "该项未取到（需求为空）");
        }
        String tag = index == null ? "主需求" : ("第 " + (index + 1) + " 项");
        try {
            // ── 归一（与单需求链路完全一致）──
            need.setQuestion(question);
            timeParser.apply(question, need);
            applyDefaultTimeRange(need);
            validateGroupBy(need);
            normalizeOrder(question, need);
            normalizeDeliveryMode(question, need);
            // 实体查证：how=self 的项也做（它同样可能带限定实体；intent 为空时也要查）
            if ("self".equals(howHint) || !"NOT_SUPPORTED".equals(need.getIntent())) {
                need = resolveEntities(need);
            }
            String how = StringUtils.hasText(need.getHow()) ? need.getHow().toLowerCase() : "registered";
            log.info("[AI需求] {} 执行：intent={} how={} groupBy={} 时间={}~{} filter={}",
                    tag, need.getIntent(), how, need.getGroupBy(),
                    need.getStartDate(), need.getEndDate(), need.getFilter());

            // compute：不查库，用已取到的数做算术（由最终润色步骤完成），这里不出骨架段
            if ("compute".equals(how)) {
                AskReplyVO vo = new AskReplyVO();
                vo.setIntent("COMPUTE");
                vo.setSource("COMPUTE");
                vo.setFallback(false);
                vo.setAnswer("");
                return new NeedOutcome(vo, need, null);
            }
            // self：LLM 自生成 SQL（探索性）；intent 可能为空（LLM 明说"登记能力都不合适"）→ 给个中性标签
            if ("self".equals(how)) {
                if (!StringUtils.hasText(need.getIntent())) {
                    need.setIntent("NOT_SUPPORTED");
                }
                AskReplyVO vo = generatedSqlReply(withFilter(question, need), need, sessionId);
                return vo == null ? notFetched(need, "该项未取到（自生成查询未产出结果）") : new NeedOutcome(vo, need, null);
            }

            // ── 能力命中校验（唯一决策点，三通道共用）──
            com.cosmo.hhim.micro.application.service.ai.CapabilityGate.Decision decision =
                    capabilityGate.decide(question, need);
            log.info("[AI需求] {} mode={} capability={} ｜ {}", tag, decision.getMode(),
                    decision.getCapability(), decision.getReason());
            need.setMode(String.valueOf(decision.getMode()));

            // operator：分析算子（归因/对比/集中度）——确定性计算，可对账
            if (com.cosmo.hhim.micro.application.service.ai.CapabilityGate.Mode.ANALYSIS == decision.getMode()) {
                AskReplyVO vo = analysisReply(question, need, decision.getCapability());
                if (vo == null) {
                    log.info("[AI需求] {} 算子组织不起来 → 转自生成 SQL", tag);
                    vo = generatedSqlReply(question, need, sessionId);
                }
                return vo == null ? notFetched(need, "该项未取到（算子与自生成查询都没产出）") : new NeedOutcome(vo, need, null);
            }
            // 未登记 → 自生成 SQL
            if (com.cosmo.hhim.micro.application.service.ai.CapabilityGate.Mode.GENERATED == decision.getMode()) {
                AskReplyVO vo = generatedSqlReply(question, need, sessionId);
                return vo == null ? notFetched(need, "该项未取到（自生成查询未产出结果）") : new NeedOutcome(vo, need, null);
            }
            // LLM 判定登记能力不满足（how=registered 但 route=self）→ 自生成 SQL
            if ("self".equalsIgnoreCase(need.getRoute())) {
                log.info("[AI需求] {} 判定登记能力不满足（route=self ｜ 理由：{}）→ 改走自生成 SQL",
                        tag, need.getRouteReason() == null ? "(未说明)" : need.getRouteReason());
                AskReplyVO selfVo = generatedSqlReply(withFilter(question, need), need, sessionId);
                if (selfVo != null) {
                    return new NeedOutcome(selfVo, need, null);
                }
            }

            // ── registered：登记能力固定 SQL（核心循环 Act → Reflect）──
            int replan = 0;
            AskExecutionResult exec = null;
            while (replan <= MAX_REPLAN_ROUNDS) {
                exec = intentExecutor.execute(need.getIntent(), need);
                ExecutionResult meta = normalize(exec);
                logAgent(replan, need, meta);
                ReflectDecision judge = reflectorRule.judge(meta, question);
                if (ReflectDecision.PASS.equals(judge.getJudgment())) {
                    break;
                }
                if (ReflectDecision.NEEDS_CLARIFY.equals(judge.getJudgment())) {
                    return new NeedOutcome(simpleReply(judge.getReason(), need.getIntent(), need.getSource()), need, null);
                }
                if (ReflectDecision.STOP.equals(judge.getJudgment())) {
                    boolean unsupported = ReflectDecision.CATEGORY_UNSUPPORTED.equals(judge.getCategory());
                    AskIntentResult finalNeed = need;
                    return new NeedOutcome(unsupported ? unregisteredReply(need) : diagnosticReply(need.getIntent()), finalNeed, null);
                }
                if (replan >= RULE_REPLAN_BUDGET) {
                    return new NeedOutcome(diagnosticReply(need.getIntent()), need, null);
                }
                if (meta.hasData()) {
                    break;
                }
                log.info("[Agent/Loop] REPLAN({}): {}", replan + 1, judge.getReason());
                need = replanRule(need);
                replan++;
            }
            // 输出：骨架答案（不做逐项润色——最后统一润色一次）
            AskReplyVO vo;
            if (exec == null || exec.getRows() == null || exec.getRows().isEmpty()) {
                if (isWorkDataMetric(need.getIntent())) {
                    vo = new AskReplyVO();
                    vo.setAnswer(emptyDataAnswer.build(need, latestSubmitDay(), exec.getParams(),
                            pendingSummary(exec.getParams())));
                    vo.setIntent(need.getIntent());
                    vo.setFallback(true);
                    vo.setSource(need.getSource());
                } else {
                    vo = answerComposer.compose(need, exec);
                    vo.setFallback(true);
                }
            } else {
                vo = answerComposer.compose(need, exec);
                if (!StringUtils.hasText(need.getDroppedEntity())) {
                    vo.setAnswer(answerGenerator.generate(question, need, exec, vo.getAnswer()));
                }
                // ★极值需求（问的是"最高/最低的那一个"）：把骨架收成**极值那几行**，
                //   否则排名里所有行都会进摘要，模型可能挑错主语
                //   （实测：问「上月哪天良品率最低」→ 摘要首行是 08-23 → 答案主语写成 08-23 ✗ 真值 08-05 ✗）。
                //   注意：这里按**数值列**取极值，不能按行数截断（行是按时序排的，截前 N 行会丢掉极值日）。
                if (keepExtremum(need, exec)) {
                    log.info("[AI需求] {} 极值需求（{}）→ 骨架只保留极值行 {} 条",
                            tag, need.getWant(), exec.getRows().size());
                    vo = answerComposer.compose(need, exec);
                }
            }
            vo.setTrust("authority");   // 通道 A：登记 SQL，与页面同源
            return new NeedOutcome(vo, need, null);
        } catch (Exception e) {
            log.warn("[AI需求] {} 执行异常（不影响其它项）：{}", tag, e.toString());
            return notFetched(need, "该项未取到（执行异常）");
        }
    }

    /**
     * 极值投影：需求问的是"最高/最低/最好/最差的那一个"时，把结果收成**极值行**（并列全留）。
     *
     * <p>为什么必须做：排名类结果整段进摘要时，模型可能挑错主语（实测问"上月哪天良品率最低"，
     * 摘要首行是 08-23，答案就把 08-23 当成"最低那天"✗ 真值 08-05 ✗）。
     * 收成极值行后主语唯一，模型无从挑错；依据也随之重算，保证答案与依据同源。
     *
     * @return true 表示行集合已被改写
     */
    private boolean keepExtremum(AskIntentResult need, AskExecutionResult exec) {
        if (need == null || exec == null || exec.getRows() == null || exec.getRows().size() <= 1) {
            return false;
        }
        String want = (need.getWant() == null ? "" : need.getWant()) + (need.getQuestion() == null ? "" : need.getQuestion());
        boolean lowest = want.contains("最低") || want.contains("最差") || want.contains("最少") || want.contains("最小");
        boolean highest = want.contains("最高") || want.contains("最多") || want.contains("最好") || want.contains("最大");
        if (!lowest && !highest) {
            return false;
        }
        // 数值列：先看"用户问的那个度量"（问良品率就按 passRate 取极值），
        // 再看执行器回填的排序键与派生度量，最后才退化为行内第一个数值字段
        String numericKey = extremumKey(need, exec);
        Double best = null;
        for (int i = 0; i < exec.getRows().size(); i++) {
            Double v = valueAt(exec, i, numericKey);
            if (v == null) {
                continue;
            }
            if (best == null || (lowest ? v < best : v > best)) {
                best = v;
            }
        }
        if (best == null) {
            return false;
        }
        java.util.List<java.util.Map<String, Object>> kept = new java.util.ArrayList<>();
        for (int i = 0; i < exec.getRows().size(); i++) {
            Double v = valueAt(exec, i, numericKey);
            if (v != null && Math.abs(v - best) < 1e-9) {
                kept.add(exec.getRows().get(i));
            }
        }
        if (kept.isEmpty() || kept.size() == exec.getRows().size()) {
            return false;   // 全是极值（并列）＝无需裁剪
        }
        exec.setRows(kept);
        exec.setResultCount(kept.size());
        exec.setEvidence(null);   // 行变了 → 让 AnswerComposer 依据重算（答案与依据同源）
        return true;
    }

    /** 取一行里的数值（优先指定键，否则第一个 Number 值） */
    private Double numericOf(java.util.Map<String, Object> row, String key) {
        if (row == null) {
            return null;
        }
        if (StringUtils.hasText(key) && row.get(key) instanceof Number) {
            return ((Number) row.get(key)).doubleValue();
        }
        for (Object v : row.values()) {
            if (v instanceof Number) {
                return ((Number) v).doubleValue();
            }
        }
        return null;
    }

    /** 取第 i 行的度量值：行内字段 → 派生度量（投影阶段算的，如 passRate） */
    private Double valueAt(AskExecutionResult exec, int i, String key) {
        Double v = numericOf(exec.getRows().get(i), key);
        if (v != null) {
            return v;
        }
        if (exec.getDerived() != null && i < exec.getDerived().size()) {
            java.util.Map<String, Object> d = exec.getDerived().get(i);
            if (d != null && d.get(key) instanceof Number) {
                return ((Number) d.get(key)).doubleValue();
            }
        }
        return null;
    }

    /** 极值投影该按哪个度量取：用户问的度量（良品率/不良率/产量…）→ 执行器排序键 → 派生度量 → null */
    private String extremumKey(AskIntentResult need, AskExecutionResult exec) {
        String want = (need.getWant() == null ? "" : need.getWant())
                + " " + (need.getQuestion() == null ? "" : need.getQuestion());
        String[][] words = {
                {"良品率", "passRate"}, {"合格率", "passRate"}, {"良率", "passRate"},
                {"不良率", "ngRate"}, {"不良数", "ngNum"}, {"不良", "ngNum"},
                {"返修率", "repairRate"}, {"报废率", "abandonedRate"},
                {"产量", "passNum"}, {"良品", "passNum"}, {"报工", "totalNum"}, {"记工", "totalNum"},
                {"库存", "finishedNum"},
        };
        for (String[] pair : words) {
            if (want.contains(pair[0])) {
                String key = pair[1];
                // 行里或派生度量里真的有这个度量才用（否则退回排序键）
                if (exec.getRows() != null && !exec.getRows().isEmpty()) {
                    if (exec.getRows().get(0).get(key) instanceof Number) {
                        return key;
                    }
                    if (exec.getDerived() != null && !exec.getDerived().isEmpty()
                            && exec.getDerived().get(0) != null
                            && exec.getDerived().get(0).get(key) instanceof Number) {
                        return key;
                    }
                }
            }
        }
        return StringUtils.hasText(exec.getSortKey()) ? exec.getSortKey() : null;
    }

    /**
     * 把限定条件并进问题文本（自生成 SQL 通道按问题取数，需要看到 filter）。
     *
     * <p>另外带上 **`want`（这一项想要什么）**：需求合并后（如"返修率；报废率"）只靠 filter
     * 表达不出"要哪几列"，模型可能只 SELECT 一列（实测于是答"报废率未取到"✗）。
     */
    private String withFilter(String question, AskIntentResult need) {
        java.util.Map<String, String> flt = need == null ? null : need.getFilter();
        StringBuilder sb = new StringBuilder(question == null ? "" : question);
        if (flt != null && !flt.isEmpty()) {
            sb.append("（限定条件：").append(flt).append("）");
        }
        if (need != null && StringUtils.hasText(need.getWant())) {
            sb.append("（这一项要取的是：").append(need.getWant()).append("）");
        }
        return sb.toString();
    }

    /** 依据里 snapshot 的条目数（诊断用：0 表示该段没带依据） */
    @SuppressWarnings("unchecked")
    private int snapshotSize(java.util.Map<String, Object> evidence) {
        if (evidence == null) {
            return 0;
        }
        Object snap = evidence.get("snapshot");
        return (snap instanceof java.util.List) ? ((java.util.List<Object>) snap).size() : 0;
    }

    /** 补答阶段抑制落库（ThreadLocal：与请求同线程；用完必须 remove） */
    private static final ThreadLocal<Boolean> SUPPRESS_PERSIST = new ThreadLocal<>();

    /**
     * 依据合并：把补答段的 evidence 追加进主 VO 的依据里。
     *
     * <p>前端「依据」卡片只渲染 metric/source/snapshot 三块，这里把**每段**的指标口径、
     * 取数来源与结果快照都追加进去（前端零改动）：
     * <ul>
     *   <li>段头：范围标签（如「按 process 维度看」）+ 该段的指标名与口径说明；</li>
     *   <li>段体：该段的快照项（如「攻丝 不良数 5 · 良品数 29 · 良品率 85.2%」）。</li>
     * </ul>
     * 这样用户展开依据，能对着**每一段**的数逐项核对，而不是只有第一段的数。
     */
    @SuppressWarnings("unchecked")
    private void mergeEvidence(AskReplyVO vo, AskReplyVO part, String scope) {
        if (vo == null || part == null || vo.getEvidence() == null || part.getEvidence() == null) {
            return;
        }
        try {
            java.util.Map<String, Object> target = vo.getEvidence();
            java.util.List<Object> merged = new java.util.ArrayList<>();
            Object exist = target.get("snapshot");
            if (exist instanceof java.util.List) {
                merged.addAll((java.util.List<Object>) exist);
            }
            // 段头：范围标签 + 该段指标名/口径/条数（一行说清"这段依据是哪来的"）
            java.util.Map<String, Object> head = new java.util.LinkedHashMap<>();
            head.put("label", scope == null || scope.isEmpty() ? "补充段" : scope);
            head.put("value", describeEvidence(part.getEvidence()));
            merged.add(head);
            Object ps = part.getEvidence().get("snapshot");
            if (ps instanceof java.util.List) {
                merged.addAll((java.util.List<Object>) ps);
            }
            target.put("snapshot", merged);
            Object pn = part.getEvidence().get("note");
            if (pn != null) {
                Object tn = target.get("note");
                target.put("note", (tn == null ? "" : tn + "；") + pn);
            }
        } catch (Exception e) {
            log.warn("[AI决策] 依据合并失败（不影响答案）: {}", e.toString());
        }
    }

    /**
     * 依据段头的人话描述：指标名 + 口径/条数说明（都来自该段自己的依据，不臆造）。
     * 历史问题：段头 value 为空 → 用户只看到「【按 process 维度看】」后面什么都没有，核不到这段是什么数。
     */
    private String describeEvidence(java.util.Map<String, Object> evidence) {
        if (evidence == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Object metric = evidence.get("metric");
        if (metric instanceof java.util.Map) {
            Object name = ((java.util.Map<String, Object>) metric).get("name");
            if (name != null && !String.valueOf(name).isEmpty()) {
                sb.append(name);
            }
        }
        Object source = evidence.get("source");
        if (source instanceof java.util.Map) {
            Object note = ((java.util.Map<String, Object>) source).get("note");
            if (note != null && !String.valueOf(note).isEmpty()) {
                sb.append(sb.length() > 0 ? " ｜ " : "").append(note);
            }
        }
        return sb.toString();
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
            scope = "，已按全部工序查询";
        } else if ("employeeName".equals(field)) {
            scope = "，已按全部员工查询";
        } else if ("productNameOrCode".equals(field)) {
            scope = "，已按全部产品查询";
        } else {
            scope = "，已按全部范围查询";
        }
        // 答案本身是"未取到/暂无"时不能说"上面给的是…数据"（自相矛盾，实测踩到：
        // 「未能取到DN10各工序不良数量的统计结果。（注：…上面给的是全部产品的数据。）」）
        String ans = vo.getAnswer() == null ? "" : vo.getAnswer();
        boolean noData = ans.contains("未取到") || ans.contains("暂无") || ans.contains("没有")
                || ans.contains("未能") || ans.contains("无数据");
        if (noData) {
            scope = scope + "，本次也没有取到符合条件的记录";
        } else {
            scope = "，上面给的是" + scope.substring(1);
        }
        String note = "（注：没找到「" + intent.getDroppedEntity() + "」" + scope + "。）";
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

    /**
     * 是否需要把对话历史塞进解析提示词（P2 · needContext）。
     *
     * <p>历史不必每次都塞：只有**依赖上文**的问题才需要（指代 / 省略 / 追问）。
     * 两级判定：① 指代/追问措辞 → 确定性**强制**带历史（这类问题脱离上文无解，不能交给模型否决）；
     * ② 其余交给 LLM 判 needContext（判 no 就不塞 —— 省 token，也避免上一问的实体/时间被继承）。
     */
    private static final String[] CONTEXT_HINTS = {
            "他", "她", "它", "那个", "这个", "这些", "那些", "上面", "上述", "刚才", "刚刚",
            "呢", "再", "换成", "换成", "还有", "另外", "继续", "然后", "同样", "也是",
            "为什么", "原因", "降了", "涨了", "差多少", "对比", "相比"};

    private java.util.List<Map<String, String>> needContext(String question, String sessionId) {
        if (!StringUtils.hasText(question)) {
            return null;
        }
        // ① 确定性兜底：命中指代/追问措辞 → **必须**带历史（不能交给模型否决，
        //    「那上个月呢」脱离上文根本无法解析）
        for (String hint : CONTEXT_HINTS) {
            if (question.contains(hint)) {
                log.info("[AI上下文] 命中依赖上文的措辞（{}）→ 携带最近对话", hint);
                return chatSessionService.recentContext(sessionId, 6);
            }
        }
        // ② 其余交给 LLM 判 needContext（不需要就不把历史塞进 prompt：省 token、且避免上一问实体/时间被继承）
        java.util.List<Map<String, String>> ctx = chatSessionService.recentContext(sessionId, 6);
        if (ctx == null || ctx.isEmpty()) {
            return null;
        }
        boolean need = llmIntentResolver.needContext(question, ctx);
        if (need) {
            log.info("[AI上下文] needContext=yes → 携带最近对话");
            return ctx;
        }
        log.info("[AI上下文] needContext=no → 不携带历史（避免上一问的实体与时间被继承）");
        return null;
    }

    private AskIntentResult resolve(String question, java.util.List<Map<String, String>> context) {
        AskIntentResult r = llmIntentResolver.resolve(question, context);
        // ★把 LLM 给出的**取数方式**回填到返回的主槽位：
        //   LlmIntentResolver 把第 1 项灌进 r 自己（intent/want/how），上层直接拿 r 当主槽位用；
        //   若这里不回填，主槽位 how 为空 → runNeed 按默认 registered 走 → 闸门会把问题套到
        //   "名字沾边"的登记能力上（实测：问「本月的返修率和不良数」被套成 NG_DETAIL 不良明细 ✗，
        //   答出"法兰盘DN20 不良数 1"这种某个分组的一行 ✗）。
        //   注意：只回填"路由类"字段，不覆盖 LLM 已经解析出的其它槽位。
        if (r != null && "LLM".equals(r.getSource()) && !StringUtils.hasText(r.getHow())) {
            r.setHow("self".equalsIgnoreCase(r.getRoute()) ? "self" : "registered");
            log.info("[AI需求] 主槽位 how 缺省回填：{}（route={}）", r.getHow(), r.getRoute());
        }
        // 【必须短路】LLM 明确判定"非业务问题"（task=non_biz）时直接采纳，**不得**再用规则兜底覆盖。
        // 历史 bug：模型正确判了 non_biz（intent=NOT_SUPPORTED），却被下面的规则兜底按关键词"产量"套成 SUMMARY，
        // 结果「写一首关于产量的诗」照样答了数据 —— 规则只能看字面，不能推翻语义判断。
        if (r != null && "non_biz".equals(r.getTask())) {
            log.info("[AI意图] task=non_biz（LLM 语义判定）→ 不进入能力匹配，跳过规则兜底");
            return r;
        }
        // 【必须短路 ②】LLM 明确说「登记能力都不合适、自己生成 SQL」时（how=self），
        // 不得再用规则兜底覆盖它的判断。
        // 历史 bug（2026-09-22 实测）：问「本月的返修率和不良数分别是多少」→ LLM 正确判定
        // 「没有语义等价的登记能力，how=self（自行生成 SQL 做整体聚合）」→ 却被下面规则兜底
        // 按关键词「不良」套成 NG_DETAIL（不良明细清单）✗ → 答案变成"法兰盘DN20 不良数 1"
        // （某个分组的一行）✗ 而用户要的是全厂合计 ✗。
        // 规则只能看字面、判不了"这份登记 SQL 满不满足需求"，所以 how=self 时以 LLM 为准。
        // 注意：只短路 self —— intent=NOT_SUPPORTED 且**没给 how** 时仍走规则兜底（模型没看懂时的第二次机会）。
        if (r != null && "self".equalsIgnoreCase(r.getHow())) {
            log.info("[AI意图] LLM 判定自行取数（how=self，intent={}）→ 跳过规则兜底", r.getIntent());
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
        return resolveEntities(intent);
    }

    /** 需要做字典查证的实体槽位（顺序：产品 → 工序 → 员工；与 EntityResolver 的取槽顺序一致） */
    private static final String[] ENTITY_FIELDS = {"productNameOrCode", "processNameOrCode", "employeeName"};

    /**
     * 逐槽位字典查证 + **规范名同步写回两处**（entities 与 filter）。
     *
     * <p>为什么两处都要写：`entities` 是登记 SQL / 分析算子的参数来源，`filter` 是**生成 SQL 通道**
     * 拼提示词的来源（`withFilter()`）。两者并行存在，只规范化其中一个就会出现
     * 「字典查到了规范名 ✓ 但取数还是用原话 ✗」——实测问「DN15 各工序哪道不良最多」，
     * 字典命中「法兰盘DN15」但 filter 仍是 DN15 → 生成 SQL 写 `product_name = 'DN15'` → 0 行 → 答"未取到" ✗
     *
     * <p>逐槽位的第二个原因：LLM 有时**只把实体填进 filter 而 entities 留空**（实测出现过），
     * 只按"第一个 entities 槽位"查证就会漏掉这种形态。
     */
    private AskIntentResult resolveEntities(AskIntentResult intent) {
        for (String field : ENTITY_FIELDS) {
            if (intent.getClarifyQuestion() != null || StringUtils.hasText(intent.getDroppedEntity())) {
                return intent;   // 已在等用户澄清 / 已披露过未命中：不再继续查，避免多轮话术冲突
            }
            intent = handleEntities(intent, field);
        }
        return intent;
    }

    /** 单个槽位的字典查证（找不到该槽位的值就跳过） */
    private AskIntentResult handleEntities(AskIntentResult intent, String field) {
        String raw = intent.getEntities() != null ? intent.getEntities().get(field) : null;
        if (!StringUtils.hasText(raw) && intent.getFilter() != null) {
            raw = intent.getFilter().get(field);   // 兼容"只填了 filter"的形态
        }
        if (!StringUtils.hasText(raw)) {
            return intent;
        }
        EntityResolver.Resolved er = entityResolver.resolve(intent, field, raw);
        switch (er.status) {
            case AMBIGUOUS:
                intent.setClarifyQuestion(er.ambiguousQuestion);
                intent.setClarifyOptions(er.candidates);
                return intent;
            case NOT_FOUND:
                // 关键：不能"静默按全量回答"。记下被丢弃的实体，输出层会追加披露、并跳过 LLM 润色
                // （否则润色会把问题里的实体名和未过滤的数字拼成一句假事实）。
                // ★同时要把 filter 里的同一个词清掉：生成 SQL 通道是拿 filter 拼提示词的，
                //   留着它模型就会写 `product_name = 'DN10'` 这种等值过滤 → 查不到任何行 → 被说成"没有数据" ✗
                log.warn("[AI缺口] type=ENTITY_NOT_FOUND field={} value={}（降级全量 + 答案披露）", field, raw);
                intent.setDroppedEntity(raw);
                intent.setDroppedEntityField(field);
                intent.getEntities().remove(field);
                if (intent.getFilter() != null) {
                    intent.getFilter().remove(field);
                }
                intent.setClarifyQuestion(null);
                return intent;
            case OK:
                // ★字典说了算：规范名必须**同时写回 entities（登记 SQL/算子用）与 filter（生成 SQL 用）**
                intent.getEntities().put(field, er.value);
                if (intent.getFilter() != null && intent.getFilter().containsKey(field)) {
                    intent.getFilter().put(field, er.value);
                }
                if (!raw.equals(er.value)) {
                    log.info("[AI实体] {} 字典规范化：{} → {}（登记参数与生成 SQL 统一用规范名）",
                            field, raw, er.value);
                }
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
            String answer = answerGenerator.polishExploratory(question, humanize(skeleton));
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
            // ③ 结果快照：把**本次实查到的行**放进依据。
            //    历史问题：生成通道只给「口径 + 来源」，卡片核不到任何一个数 ✗ 用户无法对账 ✗
            java.util.List<java.util.Map<String, Object>> snapshot = buildGeneratedSnapshot(out);
            evidence.put("snapshot", snapshot);
            evidence.put("asOf", java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            vo.setEvidence(evidence);
            return vo;
        } catch (Exception e) {
            log.warn("[AI-SQL] 通道C 异常：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成 SQL 通道的结果快照（依据卡片的"结果快照"）。
     *
     * <p>**按查询分组**：一次需求可能生成多条查询（各查一个指标），每条查询一条快照项，
     * label="查询 N"，value=该查询的结果（键值对）。
     * 历史问题：把多条查询的行**混在一起平铺**，会出现"答案只答了一个指标、依据却列出两个比率"
     * 这种对不上账的观感（实测「返修率和报废率」）→ 按查询分组后，用户能一眼看出每个数来自哪条查询。
     */
    private java.util.List<java.util.Map<String, Object>> buildGeneratedSnapshot(
            com.cosmo.hhim.micro.application.service.ai.sql.GeneratedSqlChannel.Outcome out) {
        java.util.List<java.util.Map<String, Object>> snapshot = new java.util.ArrayList<>();
        if (out == null || out.getResults() == null) {
            return snapshot;
        }
        java.util.Set<Integer> failed = new java.util.LinkedHashSet<>();
        for (Object item : out.getResults()) {
            if (snapshot.size() >= MAX_SNAPSHOT_QUERIES) {
                break;
            }
            if (!(item instanceof java.util.Map)) {
                continue;
            }
            java.util.Map<?, ?> q = (java.util.Map<?, ?>) item;
            int idx = q.get("sqlIndex") instanceof Number ? ((Number) q.get("sqlIndex")).intValue() : (snapshot.size() + 1);
            Object data = q.get("data");
            if (!(data instanceof java.util.List)) {
                failed.add(idx);
                continue;   // 该条查询执行失败：不伪造快照
            }
            java.util.List<?> rows = (java.util.List<?>) data;
            java.util.Map<String, Object> one = new java.util.LinkedHashMap<>();
            one.put("label", "查询 " + idx);
            if (rows.isEmpty()) {
                one.put("value", "本条未取到记录");
            } else {
                StringBuilder val = new StringBuilder();
                int shown = 0;
                for (Object rowObj : rows) {
                    if (shown >= MAX_SNAPSHOT_ROWS) {
                        break;
                    }
                    if (!(rowObj instanceof java.util.Map)) {
                        continue;
                    }
                    if (val.length() > 0) {
                        val.append("；");
                    }
                    val.append(renderSnapshotRow((java.util.Map<?, ?>) rowObj));
                    shown++;
                }
                one.put("value", val.length() == 0 ? "本条未取到数值" : val.toString());
            }
            snapshot.add(one);
        }
        for (Integer idx : failed) {
            java.util.Map<String, Object> one = new java.util.LinkedHashMap<>();
            one.put("label", "查询 " + idx);
            one.put("value", "本条未取到记录（取数故障，已如实说明）");
            snapshot.add(one);
        }
        if (snapshot.isEmpty()) {
            java.util.Map<String, Object> none = new java.util.LinkedHashMap<>();
            none.put("label", "结果快照");
            none.put("value", "本次未取到记录（已实查）");
            snapshot.add(none);
        }
        return snapshot;
    }

    /** 依据快照：最多展示几条查询、每条查询最多几行（防止卡片刷屏） */
    private static final int MAX_SNAPSHOT_QUERIES = 3;
    private static final int MAX_SNAPSHOT_ROWS = 2;

    /** 依据快照里的一组行：名称在前、数字在后（对象名用行内首个字符串字段） */
    private String renderSnapshotRow(java.util.Map<?, ?> row) {
        String label = "";
        StringBuilder val = new StringBuilder();
        for (java.util.Map.Entry<?, ?> e : row.entrySet()) {
            Object v = e.getValue();
            if (v == null) {
                continue;
            }
            if (label.isEmpty() && v instanceof String && StringUtils.hasText((String) v)) {
                label = (String) v;
                continue;
            }
            if (v instanceof Number) {
                if (val.length() > 0) {
                    val.append(" · ");
                }
                val.append(friendlyKey(String.valueOf(e.getKey()))).append(" ").append(numText(v));
            }
        }
        if (label.isEmpty()) {
            return val.length() == 0 ? "（无值）" : val.toString();
        }
        return val.length() == 0 ? label : label + "：" + val;
    }

    /** 依据快照的键名中文化（未收录的保留原名，不臆造） */
    private String friendlyKey(String key) {
        if (key == null) {
            return "";
        }
        switch (key) {
            case "pass_rate":
                return "良品率";
            case "repair_rate":
                return "返修率";
            case "abandoned_rate":
            case "abandon_rate":
                return "报废率";
            case "ng_rate":
                return "不良率";
            case "total_ng":
                return "不良数";
            case "total_pass":
                return "良品数";
            case "cnt":
            case "total_cnt":
                return "总数";
            case "repair_num":
                return "返修数";
            case "abandoned_num":
                return "报废数";
            default:
                return key;
        }
    }

    /**
     * 骨架的"用户可见"清理（仅在**润色未生效**时使用）。
     *
     * <p>骨架是给模型看的中间产物，里面带的内部标记不能给用户看：
     * 段头「【第 N 项数据｜…】」是给模型做多段区分用的；「查询1返回 1 行」「（无数据）」是取数视角的措辞；
     * 「【本次主对象】…」是给模型的防错标注。
     * 实测踩到：润色被数字护栏打回后，用户直接看到「【第 1 项数据｜result】查询1返回 1 行：- abandoned_rate 0…」。
     */
    private String cleanSkeletonForUser(String skeleton) {
        if (!StringUtils.hasText(skeleton)) {
            return skeleton;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : skeleton.split("\\r?\\n")) {
            String t = line.trim();
            if (t.isEmpty()) {
                continue;
            }
            // 段头 / 主对象标注 → 去掉内部前缀，只留内容（主对象行整行去掉：它是给模型的）
            t = t.replaceAll("^【第\\s*\\d+\\s*项数据[^】]*】\\s*", "");
            if (t.startsWith("【本次主对象】")) {
                continue;
            }
            // 取数视角的措辞 → 人话
            t = t.replaceAll("查询\\s*\\d+\\s*返回\\s*\\d+\\s*行\\s*[:：]\\s*", "");
            t = t.replaceAll("查询\\s*\\d+\\s*返回\\s*\\d+\\s*行", "");
            t = t.replace("（无数据）", "本次未取到数据");
            t = t.replaceAll("^-\\s*", "");
            // 喂 LLM 的指令/口径/自检行整行剔除（兜底：正常路径已被用户版骨架挡住）
            if (t.startsWith("★") || t.startsWith("口径提示") || t.startsWith("回答要求")
                    || t.contains("本清单已给出确定主因") || t.contains("不得改写成")
                    || t.contains("禁止出现「主因不明确」")) {
                continue;
            }
            t = t.replace("**", "").replace("✗", "").replace("✓", "").trim();
            if (t.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(t);
        }
        return sb.length() > 0 ? sb.toString() : skeleton;
    }

    /**
     * 本次的**主对象**：第 1 项（取值项）取到的那个实体/日期。
     *
     * <p>用途：写进润色摘要的「【本次主对象】」标注，防止后续归因段自己另挑一个对象当主语
     * （实测：「上月哪天良品率最低？为什么？」主项取到 08-05 ✓ 归因段主因是 08-12 ✗
     * 润色后主语变成 08-12 ✗ 与"哪天最低"的答案自相矛盾）。
     *
     * <p>取法：第 1 项的第一行里，优先用日期字段，再退到业务对象名（工序/产品/员工）。
     * 取不到就返回 null（不臆造主语）。
     */
    private String subjectOf(List<NeedOutcome> results) {
        if (results == null || results.isEmpty()) {
            return null;
        }
        NeedOutcome head = results.get(0);
        if (head == null || head.vo == null || !StringUtils.hasText(head.vo.getAnswer())) {
            return null;
        }
        // ① 优先从依据快照里读（执行器/生成通道都已产出可读 label）
        java.util.Map<String, Object> ev = head.vo.getEvidence();
        if (ev != null) {
            Object snap = ev.get("snapshot");
            if (snap instanceof java.util.List && !((java.util.List<?>) snap).isEmpty()) {
                Object first = ((java.util.List<?>) snap).get(0);
                if (first instanceof java.util.Map) {
                    Object label = ((java.util.Map<?, ?>) first).get("label");
                    if (label != null && StringUtils.hasText(String.valueOf(label))
                            && !String.valueOf(label).startsWith("查询")
                            && !String.valueOf(label).startsWith("结果快照")
                            && !String.valueOf(label).equals("本次结果")) {
                        return String.valueOf(label);
                    }
                }
            }
        }
        // ② 退一步：从答案里抓日期（"2026年8月5日" / "2026-08-05"）
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d{4})\\s*年\\s*(\\d{1,2})\\s*月\\s*(\\d{1,2})\\s*日").matcher(head.vo.getAnswer());
        if (m.find()) {
            return m.group(1) + "-" + String.format("%02d", Integer.parseInt(m.group(2)))
                    + "-" + String.format("%02d", Integer.parseInt(m.group(3)));
        }
        java.util.regex.Matcher m2 = java.util.regex.Pattern
                .compile("\\d{4}-\\d{2}-\\d{2}").matcher(head.vo.getAnswer());
        if (m2.find()) {
            return m2.group();
        }
        return null;
    }

    /**
     * 算子段的人话结论（**确定性生成**，不调 LLM）。
     *
     * <p>为什么不调 LLM：合并阶段会用全部数据统一写一段，段内润色是重复劳动；
     * 而且段内润色一旦被数字护栏打回，回退的是"事实清单"（含口径/自检/回答要求等元信息，上千字），
     * 反而把重点稀释掉。这里只输出一句可读结论 + 必要解释。
     */
    private String operatorSummary(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.CompareResult r,
                                   String labelA, String labelB, String metricLabel, String dimLabel,
                                   boolean compareOnly, String facts) {
        StringBuilder out = new StringBuilder();
        if (r == null) {
            return facts;
        }
        // ① 总体：一句话说清"整体变了多少"
        out.append(labelA).append("到").append(labelB).append("，全部").append(dimLabel).append("合起来")
                .append(metricLabel).append("是 ")
                .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(r.rateA))
                .append(" → ")
                .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(r.rateB))
                .append("（")
                .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pp(r.deltaPp))
                .append("）");
        if (compareOnly) {
            out.append("。");
            return out.toString();
        }
        if (r.items == null || r.items.isEmpty()) {
            out.append("；分组数据不足，无法判断是谁带来的变化。");
            return out.toString();
        }
        // ② ★主结论用**人话口径**：按"该对象自身良品率的降幅"找下降最多的那个
        //    （历史问题：按"贡献度"排序 → 一个自身良品率反而变好、只是样本量变小的产品会被说成"下降最多" ✗
        //     实测：法兰盘DN15 自身 93.4%→97.6%（变好），却因样本从 240 件缩到 42 件而被判"下降最多" ✗
        //     而真正下降最多的是端盖K-21：97.3%→89.9%，降 7.4 个百分点 ✗ 用户看不懂 ✗）
        com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.Contribution worst = null;
        java.math.BigDecimal worstDrop = java.math.BigDecimal.ZERO;
        for (com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.Contribution c : r.items) {
            if (c == null || c.rateA == null || c.rateB == null || c.newGroup || c.goneGroup) {
                continue;   // 本期新增/上期消失：没有可比的两期，不算"下降/上升"
            }
            java.math.BigDecimal drop = c.rateA.subtract(c.rateB);         // 正数 = 下降了这么多
            if (drop.compareTo(worstDrop) > 0) {
                worstDrop = drop;
                worst = c;
            }
        }
        if (worst != null && worstDrop.compareTo(new java.math.BigDecimal("0.001")) > 0) {
            out.append("；其中").append(dimLabel).append("「").append(worst.group).append("」")
                    .append(metricLabel).append("降得最多：")
                    .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(worst.rateA))
                    .append(" → ")
                    .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(worst.rateB))
                    .append("（降 ").append(worstDrop.multiply(java.math.BigDecimal.valueOf(100))
                            .setScale(1, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString())
                    .append(" 个百分点，样本 ").append(worst.rowsA).append(" → ").append(worst.rowsB).append(" 条）");
        } else {
            out.append("；各").append(dimLabel).append("的").append(metricLabel)
                    .append("都没有下降（变化都很小或都是上升），没有降得最多的对象。");
        }
        // ③ 附注：对**总体**的影响（贡献度）——这是另一个口径，避免与上面的"自身降幅"混淆
        if (StringUtils.hasText(r.mainGroup)) {
            com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.Contribution mainRow = null;
            for (com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.Contribution c : r.items) {
                if (c != null && r.mainGroup.equals(c.group)) {
                    mainRow = c;
                    break;
                }
            }
            if (mainRow != null && mainRow.contributionPp != null) {
                out.append("。附：按「对总体变化的影响（贡献度）」看，影响最大的是「").append(r.mainGroup)
                        .append("」");
                if (mainRow.rateA != null && mainRow.rateB != null) {
                    out.append("（它自身 ")
                            .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(mainRow.rateA))
                            .append(" → ")
                            .append(com.cosmo.hhim.micro.application.service.ai.analysis.MiniAnalyst.pct(mainRow.rateB));
                    java.math.BigDecimal own = mainRow.rateA.subtract(mainRow.rateB);
                    if (own.signum() < 0) {
                        out.append("，即自身没有下降，它对总体的影响来自产量结构变化");
                    }
                    out.append("）");
                }
                out.append("；两个口径含义不同，不要混着说");
            }
        }
        out.append("。");
        return out.toString();
    }

    /**
     * 小数位收敛（**确定性收尾**）：把答案里"位数明显过多"的小数收到 1~2 位。
     *
     * <p>为什么要代码兜：模型算术时会给 39.66666667 / 92.2222222 这种数，提示词要求"按业务习惯取整"
     * **不可靠**（实测两次都还在）。这里只收敛**超过 2 位小数**的数字：
     * <ul>
     *   <li>≥4 位小数 → 保留 2 位（39.66666667 → 39.67、92.2222222 → 92.22）</li>
     *   <li>3 位小数**保留不动**：页面口径的比率就是截断 3 位（0.922 / 0.878），改了会与页面不一致 ✗</li>
     * </ul>
     * 整数与 1~3 位小数一律不碰，避免误伤 1.0000 / 97.6% / 2026-09-05 这类正常值。
     */
    private String formatDecimals(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+\\.\\d{4,}").matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String replacement = m.group();
            try {
                replacement = new java.math.BigDecimal(m.group())
                        .setScale(2, java.math.RoundingMode.HALF_UP)
                        .stripTrailingZeros().toPlainString();
            } catch (Exception ignore) {
                // 解析失败就原样保留
            }
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** 数值展示：去尾零（31.0000 → 31） */
    private String numText(Object v) {
        try {
            double d = Double.parseDouble(v.toString());
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return new java.math.BigDecimal(v.toString()).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return v.toString();
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

        String facts = sb.toString();
        // ★算子段**不再自己做一次润色**（P2 优化）：
        //   ① 省钱省时：合并阶段本来就会用全部数据写一段，段内再润色一次纯属重复（实测 2 次 LLM ≈ 1.5~2 秒）；
        //   ② 避免"段内润色被数字护栏打回 → 回退成上千字事实清单"把重点稀释掉
        //      （实测：回退后每段 1434 字，含口径/自检/回答要求等元信息）。
        //   算子段只提供两样东西：① 人话结论（summary，给合并润色当事实）；② 数字清单（给数字护栏当允许集合）。
        String summary = operatorSummary(cmp.compare, labelA, labelB, metricLabel, dimLabel,
                compareOnly, sb.toString());
        AskReplyVO vo = simpleReply(summary, code, intent.getSource());
        vo.setFallback(false);   // 结论由确定性算法给出（不是模板兜底），措辞已是人话
        // 把完整事实清单挂到 params 里：合并润色与数字护栏都用它（前端不展示 params 的实现字段）
        vo.setParams(new java.util.LinkedHashMap<>());
        vo.getParams().put("facts", facts);

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
        // 补答（多诉求的第 2..N 段）**不落库**：它的问答会并入主答案的一段输出，
        // 若各自落库，会话历史里会多出"用户没问过的子问题"和独立的 AI 消息（幽灵记录）
        if (Boolean.TRUE.equals(SUPPRESS_PERSIST.get())) {
            return;
        }
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

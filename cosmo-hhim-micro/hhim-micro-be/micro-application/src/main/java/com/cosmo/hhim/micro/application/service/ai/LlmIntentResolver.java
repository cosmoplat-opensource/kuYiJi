/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.application.dto.ai.OntologyCapabilityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * AI 问数 · LLM 意图解析器（默认实现，失败由编排层回退规则版）
 *
 * <p>本体投影成"能力清单"注入 system prompt；LLM 仅输出"意图 JSON"，
 * 不接触口径/接口/SQL。输出解析失败时返回 NOT_SUPPORTED（不抛异常）。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmIntentResolver implements IntentResolver {

    /** 一句话最多解析出的诉求数（语义层上限；执行层按同一上限循环） */
    public static final int MAX_INTENTS = 10;

    /**
     * 本次请求解析出的**数据需求清单**（needs[] 的第 2..N 项；第 1 项由 {@link #resolve} 直接返回）。
     *
     * <p>P2 重构后不再有"主诉求 / 其余诉求"之分：整句问题一次解析成 needs[]，
     * 每一项都是**一条数据需求**（不是"一个子问题"），由调度层并发取数、最后合成**一段**答案。
     *
     * <p>ThreadLocal 生命周期：解析时写入 → 调度层 {@link #takeNeeds()} 取走即清（防线程复用串味）。
     */
    private static final ThreadLocal<java.util.List<AskIntentResult>> LAST_NEEDS = new ThreadLocal<>();

    /** 取本次请求的其余数据需求（取完即清） */
    public static java.util.List<AskIntentResult> takeNeeds() {
        java.util.List<AskIntentResult> v = LAST_NEEDS.get();
        LAST_NEEDS.remove();
        return v == null ? new java.util.ArrayList<>() : v;
    }

    private final AiLlmClient aiLlmClient;
    private final OntologyService ontologyService;

    @Override
    public AskIntentResult resolve(String question) {
        return resolve(question, null);
    }

    /**
     * LLM 意图解析（带对话上下文）
     *
     * @param context 最近对话（user/ai 文本，时间升序）；LLM 据此理解追问、补全语义、识别话题切换
     */
    /**
     * 业务背景与职责边界（**单一定义**，主提示词与"问题性质判定"共用，避免两处说法漂移）。
     *
     * <p>为什么必须写：模型不知道"我们是谁、有哪些业务数据"，就无法判断"这个问题是否与本项目业务相关"，
     * 只能靠字面相似去套能力（实测踩到：「写一首关于产量的诗」被套成 SUMMARY 还真的答了数据）。
     */
    private static final String BUSINESS_CONTEXT =
            "【你在什么系统里】你是一个**企业生产管理系统**内置的问数助手。"
            + "该系统面向制造企业的车间生产管理：一线员工用手机记工/报工，班组长与审产员审核产量与质量，管理者查看生产经营数据。\n"
            + "本系统的业务数据范围：\n"
            + "  · 报工/记工：谁、哪天、哪个产品、哪道工序、报了多少良品与不良（含待审/已审）\n"
            + "  · 质量：良品率、不良数、不良类型（需质检）、返修数、报废数\n"
            + "  · 基础数据：产品清单、工序（工艺路线，含首序/尾序）\n"
            + "  · 库存：工序在制品余额、成品库存\n"
            + "  · 订单/工单：交期、延期预警\n"
            + "  · 以上都可按 时间（今日/本月/上月/自定义）、产品、工序、员工 等维度查询与对比\n"
            + "【你的唯一职责】把用户问题翻译成\"要查哪些业务数据\"的结构化意图 —— 不写 SQL、不给结论、不做创作。\n"
            + "【范围约束】只处理与上述业务数据相关的问题，只有两种情况：\n"
            + "  · biz（业务问题）：想要**数据或事实**（多少、几个、最高、最低、为什么降、有没有变化、对比、趋势…）；"
            + "口语化、省略、错别字、指代上文，只要能落到上面的数据范围，就算 biz。\n"
            + "  · non_biz（非业务问题）：文本创作（写诗/写文章/写对联/段子/文案/取名/改写）、翻译、闲聊、主观评价、"
            + "与生产经营无关的问题 —— **哪怕句子里出现\"产量/良品率/库存\"这类业务词，仍然是 non_biz**"
            + "（例：「写一首关于产量的诗」→ non_biz；「帮我翻译成英文：今天产量怎么样」→ non_biz）。\n"
            + "  拿不准时按 biz 处理（宁可多答一次，不可漏答业务问题）。";

    /** 问题性质判定用的极短提示词（独立调用：长提示词里的结构化要求容易被忽略） */
    private static final String TASK_SYSTEM =
            BUSINESS_CONTEXT + "\n\n"
            + "现在只做一件事：判断用户这句话属于哪种情况。判断依据是**用户最终要的东西是什么形态**，"
            + "不是句子里出现了哪些词：\n"
            + "  · 要「数据 / 数字 / 事实 / 统计 / 对比 / 原因」→ biz\n"
            + "  · 要「一段文字作品或语言转换」（写诗、写文章、写对联、段子、文案、取名、改写、翻译）→ non_biz\n"
            + "  · 既不要数据也不要作品（闲聊、主观评价、问你是谁、与生产经营无关）→ non_biz\n"
            + "例：「写一首关于产量的诗」→ non_biz（虽然含\"产量\"，但要的是诗）\n"
            + "例：「帮我翻译成英文：今天产量怎么样」→ non_biz（要的是译文）\n"
            + "例：「今天产量怎么样」「为什么这个月良品率降了」→ biz\n"
            + "只输出一个词（biz 或 non_biz），不要标点、不要解释。";

    /**
     * 判定问题性质：biz=与本系统业务数据相关；non_biz=不相关（创作/翻译/闲聊/主观评价…）。
     *
     * <p>独立一次极短 LLM 调用：主调用返回的 task 缺失或为 biz 时用它兜底复核。
     * 失败/异常一律回退 biz（宁可多答一次，不可把正常问数误判成非业务）。
     */
    public String classifyTask(String question) {
        try {
            String reply = aiLlmClient.chat(TASK_SYSTEM, question);
            if (!StringUtils.hasText(reply)) {
                log.info("[AI意图] 问题性质判定：LLM 无返回 → 回退 biz");
                return "biz";
            }
            String t = reply.trim().toLowerCase();
            String task;
            if (t.contains("non_biz") || t.contains("non-biz") || t.contains("nonbiz") || t.contains("非业务")) {
                task = "non_biz";
            } else {
                task = "biz";
            }
            // 原始回复必须打出来：判定错了要能一眼看出是"模型说 biz"还是"解析没认出来"
            log.info("[AI意图] 问题性质判定：原始回复=\"{}\" → task={}", reply.trim().replaceAll("\\s+", " "), task);
            return task;
        } catch (Exception e) {
            log.warn("[AI意图] 问题性质判定异常，回退 biz: {}", e.getMessage());
            return "biz";
        }
    }

    /**
     * 判定"这一问是否需要对话上下文"（P2 · needContext）。
     *
     * <p>为什么要有：每次都把最近几条历史塞进提示词，是"上一问的实体/时间被继承"这类事故的温床
     * （实测：上一问问过"法兰盘DN15/上月"，这一问明明是独立问题却被带上）、也白花 token。
     * 只有**依赖上文**的问题（指代、省略、追问）才需要历史。
     *
     * <p>失败/超时一律回退 {@code false}（不带历史）：独立问题绝不会因为"没带历史"而答错，
     * 而误带历史却会答错 —— 所以缺省值取"不带"更安全。指代类措辞由调用方做**确定性兜底**。
     */
    public boolean needContext(String question, java.util.List<Map<String, String>> context) {
        if (!StringUtils.hasText(question) || context == null || context.isEmpty()) {
            return false;
        }
        try {
            java.util.concurrent.CompletableFuture<String> f = java.util.concurrent.CompletableFuture
                    .supplyAsync(() -> aiLlmClient.chat(NEED_CONTEXT_SYSTEM, contextDigest(context) + "\n【本次问题】" + question),
                            TASK_POOL);
            String reply = f.get(8, java.util.concurrent.TimeUnit.SECONDS);
            if (!StringUtils.hasText(reply)) {
                return false;
            }
            String t = reply.trim().toLowerCase();
            // 明确说"需要"才算需要（先判 no，避免 "not needed" 被 contains("need") 误判）
            boolean no = t.contains("no") || t.contains("不需要") || t.contains("否") || t.contains("独立");
            boolean yes = t.contains("yes") || t.contains("需要") || t.contains("是") || t.contains("依赖");
            boolean need = yes && !no;
            log.info("[AI上下文] needContext 判定：原始回复=\"{}\" → {}", reply.trim().replaceAll("\\s+", " "), need);
            return need;
        } catch (Exception e) {
            log.info("[AI上下文] needContext 判定未在时限内返回 → 缺省不带历史");
            return false;
        }
    }

    /** needContext 判定提示词（独立极短调用；只看"这一问能不能脱离上文理解"） */
    private static final String NEED_CONTEXT_SYSTEM =
            "你在判断：**当前这一问能不能脱离上文独立理解**。只看这一点，不要回答业务问题。\n"
            + "两种情况：\n"
            + "  · 需要上下文（yes）：出现了指代或省略——「他/它/那个/这个/上面/刚才」「那…呢」「再」「换成」"
            + "「还有呢」「继续」「为什么」「降了多少」等，脱离上文无法知道在说什么；\n"
            + "  · 不需要上下文（no）：问题自带完整对象与时间（产品名/工序名/员工名/明确时间词），"
            + "即使有上文也应独立理解。\n"
            + "只输出一个词：yes 或 no。不要标点、不要解释。";

    private String contextDigest(java.util.List<Map<String, String>> context) {
        StringBuilder sb = new StringBuilder("【对话上下文】\n");
        int from = Math.max(0, context.size() - 4);
        for (int i = from; i < context.size(); i++) {
            Map<String, String> m = context.get(i);
            boolean user = "user".equals(m.get("role"));
            String c = m.get("content") == null ? "" : m.get("content");
            if (c.length() > 120) {
                c = c.substring(0, 120) + "…";
            }
            sb.append(user ? "用户：" : "助手：").append(c).append("\n");
        }
        return sb.toString();
    }

    /** 问题性质判定的专用线程池（小、有界；判定任务很短） */
    private static final java.util.concurrent.ExecutorService TASK_POOL =
            new java.util.concurrent.ThreadPoolExecutor(2, 8, 60L, java.util.concurrent.TimeUnit.SECONDS,
                    new java.util.concurrent.LinkedBlockingQueue<>(64),
                    runnable -> {
                        Thread t = new Thread(runnable, "ai-task-classify");
                        t.setDaemon(true);
                        return t;
                    },
                    new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());

    /** 等并行判定结果（超时/异常一律回退 biz：宁可多答一次，不可漏答业务问题） */
    private String awaitTask(java.util.concurrent.CompletableFuture<String> future) {
        try {
            String t = future.get(8, java.util.concurrent.TimeUnit.SECONDS);
            return StringUtils.hasText(t) ? t : "biz";
        } catch (Exception e) {
            log.info("[AI意图] 问题性质判定未在时限内返回，回退 biz");
            return "biz";
        }
    }

    /**
     * 调度层 LLM 决策点的**统一入口**（.14：C-1 回填纠偏 / C-2 失败重路由 / C-3 缺口补取共用）。
     *
     * <p>契约：输入 system+user，要求模型只回一个 JSON 对象；输出解析结果或 null
     * （LLM 无返回/非 JSON/异常都返回 null，**不抛异常**）——调用方按「决策缺席」处理，
     * 走原确定性路径，最差退化为旧行为。次数预算由调用方（DecisionBudget）管，这里不计数。
     */
    public com.alibaba.fastjson.JSONObject decideJson(String system, String user) {
        try {
            String reply = aiLlmClient.chat(system, user);
            if (!StringUtils.hasText(reply)) {
                log.warn("[AI决策点] LLM 无返回 → 决策缺席，走确定性路径");
                return null;
            }
            String json = extractJson(reply);
            if (json == null) {
                log.warn("[AI决策点] LLM 输出非 JSON → 决策缺席，走确定性路径｜raw={}",
                        reply.trim().replaceAll("\\s+", " "));
                return null;
            }
            return JSON.parseObject(json);
        } catch (Exception e) {
            log.warn("[AI决策点] LLM 决策调用异常 → 决策缺席，走确定性路径：{}", e.getMessage());
            return null;
        }
    }

    public AskIntentResult resolve(String question, java.util.List<Map<String, String>> context) {
        AskIntentResult r = new AskIntentResult();
        r.setSource("LLM");
        if (!StringUtils.hasText(question)) {
            r.setIntent("NOT_SUPPORTED");
            return r;
        }
        String system = buildSystemPrompt();
        // 问题性质判定与主调用**并行**跑：串行会白花 3~5 秒（实测），并行则延迟取两者最大。
        // 主调用自己给了 task 时就优先采信，并行结果丢弃（只是不再需要等它）。
        java.util.concurrent.CompletableFuture<String> taskFuture =
                java.util.concurrent.CompletableFuture.supplyAsync(() -> classifyTask(question), TASK_POOL);
        String reply = aiLlmClient.chat(system, buildUserPrompt(question, context));
        if (reply == null) {
            log.info("[AI意图] LLM 调用失败，改走规则解析");
            r.setIntent("NOT_SUPPORTED");
            // 主调用失败时仍要拿到性质判定：非业务问题不该被规则兜底套成业务指标
            String t = awaitTask(taskFuture);
            if ("non_biz".equals(t)) {
                r.setTask("non_biz");
                r.setNotSupportedReason("超出生产经营数据查询范围");
            }
            return r;
        }
        r.setRawLlm(reply);
        try {
            String json = extractJson(reply);
            if (json == null) {
                throw new IllegalStateException("LLM 输出非 JSON");
            }
            JSONObject o = JSON.parseObject(json);
            // 第一步：问题性质（语义判断，不是关键词命中）——biz=与本系统业务数据相关；non_biz=不相关
            // 主调用自己给的就是"最终判断"；没给才用并行跑的判定结果兜底（省掉串行等待）
            String task = o.getString("task");
            if (!StringUtils.hasText(task)) {
                task = awaitTask(taskFuture);
            }
            r.setTask(StringUtils.hasText(task) ? task.trim().toLowerCase() : "biz");
            // ── 数据需求解析（P2 契约）：优先读 needs[]，兼容旧的 intents[] ──
            //
            // needs[] = **数据需求清单**（不是"子问题清单"）：整句问题一次列全，每项自带 how
            // （operator/registered/self/compute）、时间窗、过滤、分组、依赖。第 1 项由本方法返回，
            // 其余交给调度层并发取数 → 最后合成**一段**答案。
            // 旧格式 intents[] 仍接受：把 route 映射成 how（self→self，其余→registered），平滑升级。
            com.alibaba.fastjson.JSONArray needs = o.getJSONArray("needs");
            com.alibaba.fastjson.JSONArray slotsJson = (needs != null && !needs.isEmpty())
                    ? needs : o.getJSONArray("intents");
            String intent = null;
            if (slotsJson != null && !slotsJson.isEmpty()) {
                JSONObject first = slotsJson.getJSONObject(0);
                applySlot(r, first);                        // 第 1 项 → 主槽位
                intent = r.getIntent();
                log.info("[AI需求] 第 1 项 {} → how={} ｜ 想要：{}", intent, r.getHow(),
                        r.getWant() == null ? "(未说明)" : r.getWant());
                // 其余需求（原顺序保留，供并发取数后按序合并）
                java.util.List<AskIntentResult> rest = new java.util.ArrayList<>();
                for (int i = 1; i < slotsJson.size() && i < MAX_INTENTS; i++) {
                    JSONObject ex = slotsJson.getJSONObject(i);
                    if (ex == null) {
                        continue;
                    }
                    AskIntentResult slot = new AskIntentResult();
                    slot.setTask(r.getTask());
                    slot.setStatScope(r.getStatScope());
                    applySlot(slot, ex);
                    if (!StringUtils.hasText(slot.getHow())) {
                        slot.setHow("registered");
                    }
                    rest.add(slot);
                    r.getExtraSlots().add(slot);            // 兼容字段：仍记录全部槽位
                    log.info("[AI需求] 第 {} 项 {} → how={} ｜ 想要：{}", i + 1, slot.getIntent(), slot.getHow(),
                            slot.getWant() == null ? "(未说明)" : slot.getWant());
                }
                if (!rest.isEmpty()) {
                    log.info("[AI需求] 本次共 {} 项数据需求（并发取数后合成一段答案）", rest.size() + 1);
                    LAST_NEEDS.set(rest);
                }
            }
            if (!StringUtils.hasText(intent)) {
                intent = o.getString("intent");
                if (!StringUtils.hasText(r.getRoute())) {
                    // 兼容旧格式（只有单个 intent 字段）：没有路由信息时视为用登记能力
                    r.setRoute("registered");
                }
                if (!StringUtils.hasText(r.getHow())) {
                    r.setHow("self".equalsIgnoreCase(r.getRoute()) ? "self" : "registered");
                }
                log.info("[AI路由] 主诉求 {} → route={} ｜ 理由：{}", intent, r.getRoute(),
                        r.getRouteReason() == null ? "(旧格式，默认 registered)" : r.getRouteReason());
            }
            r.setIntent(StringUtils.hasText(intent) ? intent.toUpperCase() : "NOT_SUPPORTED");
            // 统计口径（第一性）：production=已审产出 / submission=含未审核报工行为；只认枚举，非法回退 production
            String scope = o.getString("statScope");
            if ("submission".equals(scope) || "production".equals(scope)) {
                r.setStatScope(scope);
            } else {
                r.setStatScope("production");
            }
            // 不可答时的语义判断：原因 + 建议（"谁怎么了/想查什么"→信息不足引导；"写诗"→超范围引导）
            if ("NOT_SUPPORTED".equals(r.getIntent())) {
                r.setNotSupportedReason(o.getString("notSupportedReason"));
                r.setNotSupportedHint(o.getString("notSupportedHint"));
            }

            JSONObject ent = o.getJSONObject("entities");
            if (ent != null) {
                for (Map.Entry<String, Object> e : ent.entrySet()) {
                    if (e.getValue() != null) {
                        r.getEntities().put(e.getKey(), e.getValue().toString());
                    }
                }
            }
            JSONObject time = o.getJSONObject("time");
            if (time != null) {
                r.setTimeType(time.getString("type"));
            }
            JSONObject order = o.getJSONObject("order");
            if (order != null) {
                r.setOrderBy(order.getString("by"));
                String dir = order.getString("dir");
                if ("desc".equalsIgnoreCase(dir)) {
                    r.setOrderDir("desc");
                }
                Integer limit = order.getInteger("limit");
                if (limit != null && limit > 0 && limit <= 10) {
                    r.setLimit(limit);
                }
            }
            // groupBy：仅接受指标 dims 白名单内的粒度
            String gb = o.getString("groupBy");
            if (StringUtils.hasText(gb)) {
                JSONObject metric = ontologyService.metric(r.getIntent());
                JSONArray dims = metric != null ? metric.getJSONArray("dims") : null;
                if (dims != null && dims.contains(gb)) {
                    r.setGroupBy(gb);
                }
            }
            JSONObject clarify = o.getJSONObject("clarify");
            if (clarify != null) {
                r.setClarifyQuestion(clarify.getString("question"));
                JSONArray options = clarify.getJSONArray("options");
                if (options != null) {
                    for (int i = 0; i < options.size(); i++) {
                        r.getClarifyOptions().add(options.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[AI意图] LLM 输出解析失败: {}", e.getMessage());
            r.setIntent("NOT_SUPPORTED");
        }
        return r;
    }

    /**
     * 把一个 needs[] / intents[] 元素灌进槽位对象（两套格式共用，字段名保持兼容）。
     *
     * <p>关键映射：**how 优先，route 兜底** ——
     * needs[] 用 how（operator/registered/self/compute）表达"怎么取数"；
     * 旧 intents[] 只有 route（registered/self/both），这里映射成 how：
     * self→self，其余→registered（both 含登记基准，按 registered 执行，登记口径优先）。
     *
     * <p>.14 起改为 public：调度层决策点（C-2 失败重路由 / C-3 缺口补取）把 LLM 给出的
     * 需求 JSON 灌进槽位时复用同一映射，避免两处解析漂移。
     */
    public void applySlot(AskIntentResult slot, JSONObject item) {
        if (slot == null || item == null) {
            return;
        }
        String code = item.getString("intent");
        if (!StringUtils.hasText(code)) {
            // needs[] 允许 compute 项不带 metric（纯算术，不查库）
            code = item.getString("metric");
        }
        slot.setIntent(StringUtils.hasText(code) ? code.trim().toUpperCase() : "NOT_SUPPORTED");
        slot.setWant(item.getString("want"));
        String rt = item.getString("route");
        if (StringUtils.hasText(rt)) {
            slot.setRoute(rt.trim().toLowerCase());
        } else if (!StringUtils.hasText(slot.getRoute())) {
            slot.setRoute("registered");
        }
        String how = item.getString("how");
        if (!StringUtils.hasText(how)) {
            how = "self".equalsIgnoreCase(slot.getRoute()) ? "self" : "registered";
        }
        slot.setHow(how.trim().toLowerCase());
        slot.setRouteReason(item.getString("routeReason"));
        String gb = item.getString("groupBy");
        slot.setGroupBy(StringUtils.hasText(gb) ? gb : null);
        String sc = item.getString("statScope");
        if ("submission".equals(sc) || "production".equals(sc)) {
            slot.setStatScope(sc);
        }
        JSONObject flt = item.getJSONObject("filter");
        if (flt != null) {
            flt.forEach((k, v) -> slot.getFilter().put(k, v == null ? null : String.valueOf(v)));
        }
        JSONObject ent = item.getJSONObject("entities");
        if (ent != null) {
            ent.forEach((k, v) -> slot.getEntities().put(k, v == null ? null : String.valueOf(v)));
        }
        JSONArray dep = item.getJSONArray("dependsOn");
        if (dep != null) {
            for (Object d : dep) {
                if (d instanceof Number) {
                    slot.getDependsOn().add(((Number) d).intValue());
                }
            }
        }
        // 时间窗：needs[] 用 {type,startDate,endDate}；单日/区间都直接采信（执行层仍会用 TimeParser 复核）
        JSONObject time = item.getJSONObject("time");
        if (time != null) {
            slot.setTimeType(time.getString("type"));
            slot.setStartDate(time.getString("startDate"));
            slot.setEndDate(time.getString("endDate"));
        }
        JSONObject order = item.getJSONObject("order");
        if (order != null) {
            slot.setOrderBy(order.getString("by"));
            if ("desc".equalsIgnoreCase(order.getString("dir"))) {
                slot.setOrderDir("desc");
            }
            Integer limit = order.getInteger("limit");
            if (limit != null && limit > 0 && limit <= 10) {
                slot.setLimit(limit);
            }
        }
    }

    /** 能力清单投影 → system prompt（与本体导出脚本 capability.js 同构） */
    /** 用户侧 prompt：当前问题 + 对话上下文（LLM 理解"追问/新话题"并自行补全语义） */
    private String buildUserPrompt(String question, java.util.List<Map<String, String>> context) {
        StringBuilder sb = new StringBuilder();
        if (context != null && !context.isEmpty()) {
            sb.append("【对话上下文】（按时间先后；这是同一会话中的连续对话。"
                    + "若本次问题依赖上文（如\"那上个月呢？\"\"哪道工序呢？\"），请基于上文补全其语义；"
                    + "若问题是全新话题，请忽略上文独立解析）：\n");
            for (Map<String, String> m : context) {
                boolean user = "user".equals(m.get("role"));
                sb.append(user ? "用户：" : "助手：").append(m.get("content")).append("\n");
            }
            sb.append("\n");
        }
        sb.append("【本次问题】").append(question).append("\n");
        return sb.toString();
    }

    private String buildSystemPrompt() {
        List<OntologyCapabilityDTO> caps = ontologyService.capabilities();
        StringBuilder sb = new StringBuilder();
        sb.append(BUSINESS_CONTEXT).append("\n\n");
        sb.append("你是 Ku易记 的经营数据问答助手。你只做一件事：把用户的问题翻译成结构化的查询意图。\n")
                .append("严格遵守以下约束，直接输出 JSON，不要输出任何其他文字。\n\n")
                .append("【可查询能力】\n");
        sb.append("（下面是本系统当前**全部可用能力**，每条都标了类型。**由你判断该用哪一个**——\n")
                .append("  · 类型=指标：直接产出某个业务数值（产量/良品率/库存/记工…）\n")
                .append("  · 类型=分析算子：对数据做**分析计算**（变化归因、两期对比、集中度），本身不产出单一数值；\n")
                .append("    例：问「为什么降了/什么原因/谁拖后腿」→ 适合变化归因；问「和上月比/涨了多少」→ 适合两期对比；\n")
                .append("        问「主要集中在哪里/占比最高」→ 适合集中度分析。\n")
                .append("  · 若某个算子**并不适合**当前问题，就不要选它：改选合适的指标，或置 NOT_SUPPORTED。\n")
                .append("  · 注意：问句里出现「为什么」不代表一定要用归因算子——若用户其实只想要一个数值，就选指标。\n\n");
        for (OntologyCapabilityDTO c : caps) {
            String type = c.getType() == null || c.getType().isEmpty() ? "指标" : c.getType();
            String typeCn = "analysis".equalsIgnoreCase(type) ? "分析算子" : "指标";
            sb.append(c.getCode()).append("   [").append(typeCn).append("]")
                    .append("   名称=").append(c.getName())
                    .append("   别名=[").append(String.join(", ", c.getAliases())).append("]\n")
                    .append("   参数: ").append(c.getParams().isEmpty() ? "无" : String.join(", ", c.getParams()))
                    .append("；允许维度: ").append(c.getDims().isEmpty() ? "无" : String.join("/", c.getDims()))
                    .append("\n   示例: ").append(String.join(" | ", c.getExamples())).append("\n");
            // 算子的**能力规格**：让 LLM 看清"它能算什么、需要什么、不能算什么"，
            // 从而判断"选它之后能不能算出符合问题的结果"；算不出来就别选它（改选指标或 NOT_SUPPORTED）
            if (c.getRouting() != null && !c.getRouting().isEmpty()) {
                sb.append("   ★能力规格（请据此判断该问题是否真的适合它）：\n");
                for (String line : c.getRouting().split("\n")) {
                    sb.append("     ").append(line).append("\n");
                }
            }
        }
        sb.append("\n【约束】\n")
                .append("- 【第一步：判断问题性质 task，只有两种】task=\"biz\"（与本系统业务数据相关）或 \"non_biz\"（不相关）；\n")
                .append("    判断依据是**用户想要什么**，不是句子里有没有业务词。"
                        + "「写一首关于产量的诗」「帮我翻译成英文：今天产量怎么样」都是 non_biz（虽然含\"产量\"）——"
                        + "task=non_biz 时 intent 必须为 NOT_SUPPORTED，不要再去选能力。\n")
                .append("- 【宁可说不会，不要硬套】task=biz 但清单里没有任何语义等价的能力时，intent 置 NOT_SUPPORTED"
                        + "（系统会用生成 SQL 兜底）；**禁止**因为字面相似就套一个含义不同的能力"
                        + "（例：不能把「各个工序的返修率排名」套成「记工排名」）\n")
                .append("- 只能选择以上能力；能力之外将 intent 置为 \"NOT_SUPPORTED\"\n")
                .append("- 【禁止硬套形似指标】若问题的核心指标在【可查询能力】里没有**语义等价**项"
                        + "（如「返修率」「报废率」「一次合格率」「OEE」），必须置 NOT_SUPPORTED，"
                        + "不得映射成含义不同的形似指标（例：不能把「各个工序的返修率排名」映射成「记工排名」，"
                        + "不能把「报废率」映射成「不良品数」）。判断标准是语义等价，不是字面相似。\n")
                .append("- 【由你判断去向，不要套关键词规则】该用哪个能力，看问题真正想要什么，并对照各能力的"
                        + "「★能力规格」（能算什么/需要什么/不能算什么）判断**选它之后是否真的算得出、且算得符合问题**：\n"
                        + "    · 想要一个数值 → 选对应**指标**；\n"
                        + "    · 想要\"为什么/原因/变化来自哪里\" → 看**变化归因**的规格是否满足（需要两期 + 一个分组维度）；\n"
                        + "    · 想要\"和上期比/涨了多少\" → 看**两期对比**；想要\"主要集中在哪/占比最高\" → 看**集中度分析**；\n"
                        + "    · 规格对不上（例如没有两期范围、或算子不适用于该问题）→ **不要硬选**，改选合适的指标；\n"
                        + "    · 都不合适 → NOT_SUPPORTED。\n")
                .append("- 【一句多诉求：允许返回多个 intent】用户一句话里可能有**多个诉求**"
                        + "（例：「上个月良品率最低的产品是什么？为什么？」= ①哪个产品 ②为什么低）。\n"
                        + "    · 结构自适应：**有一个诉求就返回 1 个元素，有多个诉求就返回多个元素**（最多 3 个），按诉求顺序排列；\n"
                        + "    · 每个元素独立给出自己的 intent/groupBy/entities/time/order/statScope；\n"
                        + "    · 例：上面那句 → [{\"intent\":\"PRODUCT_PASS_RATE\",\"order\":{\"by\":\"passRate\",\"dir\":\"asc\",\"limit\":1}} , "
                        + "{\"intent\":\"ATTRIBUTION\",\"entities\":{\"metricCode\":\"PRODUCT_PASS_RATE\"},\"groupBy\":\"product\"}]\n"
                        + "    · 单个诉求时就写成只有一个元素的数组；**不要**把两个诉求硬塞进一个 intent。\n")
                .append("- 只能选择以上能力；能力之外将 intent 置为 \"NOT_SUPPORTED\"\n")
                .append("- 【语义防线】若问题整体语义属于：创作/写作（写诗、写文章、编故事、写对联、写顺口溜、写段子）、"
                        + "翻译、主观评价、闲聊——即使句中包含业务关键词（产量/良品率/库存…），也必须置 NOT_SUPPORTED"
                        + "（notSupportedReason 说明超范围），不得映射到任何指标。"
                        + "例：「写一首关于产量的诗」→ NOT_SUPPORTED（虽然含\"产量\"），不是 SUMMARY\n")
                .append("- 【本次问题优先，谨慎继承】问题常省略主语/维度（\"谁记的工\"、\"那上个月呢\"）。\n"
                        + "    · **本次问题自己写明的范围一律以本次为准**：例如问句里出现了\"法兰盘DN15\"\"车削\"\"周中龙\"，"
                        + "filter/entities 就**只填本次出现的** ✗ **绝不沿用上一句的实体**（历史 bug：上一句问过法兰盘DN15，"
                        + "这一句问\"最高最低差多少\"也把法兰盘DN15 带上了 ✗）；\n"
                        + "    · **只有明确的指代**（\"他/它/那个/那上个月呢\"）才继承上文，且只继承被指代的那一项 ✗ 不要整包继承；\n"
                        + "    · 真的指代不明时才澄清或 NOT_SUPPORTED。\n")
                .append("- 【限定对象必须能被满足】问题里出现**限定对象**（某个产品/某道工序/某个员工/某类不良）或"
                        + "**派生度量**（差值/极差/占比/合计/倍数/排名倒数）时：\n"
                        + "    · 先确认候选登记能力**支持该过滤、且直接产出该度量** ✗ 不支持就**必须 route=self** ✓；\n"
                        + "    · 典型错误（严禁）：问\"法兰盘DN15 的工序里哪道不良最多\" ✗ 却选了\"工序良品率\"（它既不能按产品过滤、"
                        + "也不产出不良数）→ 答成全厂工序良品率 ✗ **答非所问**。这种必须 route=self ✓\n")
                .append("- 【别名命中优先】别名是口语等价词，不必与问题逐字相同；**能对上别名就优先用那个能力**\n")
                .append("- 【实体写法】实体名用用户原话（不要臆造、不要翻译）；\n")
                .append("- 不允许回答工资、结算、任何金额类问题\n")
                .append("- 实体名用用户原话，不要臆造\n")
                .append("- time 只允许: today / month / year / custom / null(不关心，尽量不设)\n")
                .append("- groupBy(可选，分组粒度): 只能取【允许维度】中的值；用户问\"哪天/哪一天/按天\"类时 groupBy=day；无则省略或 null\n")
                .append("- statScope(统计口径，必填): production=已审核产出（问产量/良品率/合格/不良/完工等\"结果\"时）；"
                        + "submission=含未审核的报工行为（问\"报工/记工/报了多工/谁报工\"时）。拿不准默认 production\n")
                .append("- 【逐诉求决定用什么工具，不要迁就】对**每一个**诉求，先看清单里有没有语义相关的登记能力，"
                        + "再对照它的「★能力规格」判断**这份已登记 SQL 是否满足回答需求**（六条判据）：\n"
                        + "    ① 分组/维度一致？② 粒度一致？③ 过滤支持（如限定某产品/某工序）？"
                        + "④ 口径一致（已审 or 含未审）？⑤ 度量一致（是否直接产出差值/占比/增减）？⑥ 时间窗支持（是否支持两期对比）？\n"
                        + "    · 六条全满足 → route=registered，intent 填该能力（权威口径，与页面同源）\n"
                        + "    · **任一不满足 → route=self**（自己生成 SQL），并在 routeReason 写明哪一条不满足；\n"
                        + "      例：问「法兰盘DN15 各工序不良」→ 登记能力只支持按产品分组 ✗ 不支持按工序/按产品过滤 → route=self\n"
                        + "      例：问「最高最低差多少」→ 登记能力只给原值 ✗ 不产出差值 → 可 route=registered 取基础数据，"
                        + "差值由系统在 SQL 里算（routeReason 写明「基准=登记口径」），或 route=self 一次算完\n"
                        + "    · 一次诉求同时用两者 → route=both（登记值作基准 + 自生成作补充）\n"
                        + "    · 放弃了一个**本可满足**的登记能力 → 必须写清理由 ✗ 不允许图省事自己写\n")
                .append("- 【维度词必须落地，否则 route=self】问题里出现**分组/筛选维度词**时，必须把它们落到槽位上，二者至少其一：\n"
                        + "    · 要「按它看 / 谁最高 / 哪天最低 / 哪道工序最多」→ groupBy 填该维度"
                        + "（day=日期 / process=工序 / product=产品 / employee=员工）；\n"
                        + "    · 要「限定它 / 只看某产品、某工序、某员工」→ filter 填该对象（如 {productNameOrCode: 法兰盘DN15}）；\n"
                        + "    · 若候选登记能力**不支持**该维度或该过滤 → **必须 route=self** ✓ 由系统自行生成 SQL；\n"
                        + "    · **绝不能**因为「能力名沾边」就选它、把维度词丢掉 ✗（历史 bug：\n"
                        + "        问「上月**哪天**良品率最低」✗ 却选了月度汇总 SUMMARY → 答不出「哪天」✗；\n"
                        + "        问「主要来自**哪道工序**」✗ 该诉求整条被漏掉 ✗；\n"
                        + "        问「**车削工序**这周比上周差在哪」✗ 没按工序过滤 → 答成全厂产品 ✗）\n"
                        + "    · 「哪天/哪几日/按天」这类：groupBy=day，且必须**取极值那一行**"
                        + "（如按良品率升序取第 1 行）✓ 不能只给月度汇总 ✗\n")
                .append("- 【同一件事不要拆两次】若**第一个诉求的能力已经能按问题里的维度下钻**"
                        + "（如归因算子可按 product/process/employee/day 分解贡献 ✓），"
                        + "那么「哪个产品/哪道工序」就是它的**同一个诉求** ✗ **不要**再拆出第二个诉求来表达同一件事 ✗"
                        + "（历史 bug：问「哪个产品下降最多？主要来自哪道工序」✗ 却把后半句拆成 CONCENTRATE 集中度 ✗ "
                        + "→ 它拿到的是「全产品×各工序良品率清单」✗ 与问题无因果关联 ✗ 只好硬凑对比 ✗）。\n"
                        + "    · 只有当**真的有两件不同的事**（不同指标 ✗ 或不同对象 ✗ 或不同时间窗 ✓）时才拆成多个诉求 ✓；\n"
                        + "    · 归因类问题的下钻维度写在 **groupBy / entities** 里 ✓ 由算子一次算完 ✓ 不要再拆一遍 ✗\n")
                .append("- 【一次列全：needs 是「数据需求清单」，不是「问题清单」】用户**整个问题**需要哪些数据，"
                        + "就在 needs 里**一次列全** ✗ 不许分「主问题 / 次要问题」、不许留到下一轮再补 ✗\n"
                        + "    · 每一项 = **一条数据需求**（要什么数、按什么维度、什么时间窗、怎么取），**不是**一个子问题；\n"
                        + "    · 顺序 = 用户提问里这些数据的出现顺序（系统会按这个顺序合并成一段答案）；\n"
                        + "    · **同一件事只列一个需求**（例：「哪个产品下降最多？来自哪道工序」= 一个需求：归因 + 下钻）；\n"
                        + "    · 需求之间**默认并行取数**；只有「后一项要用前一项的结果」才填 dependsOn（下标从 0 开始）；\n"
                        + "    · 每一项都必须带**本次问题的时间窗** ✗ 严禁沿用上一问的日期\n"
                        + "      （历史 bug：上一问「上月哪天良品率最低」✗ 这一问「今天…」仍按 8月5日 取数 ✗）\n"
                        + "    · compute：**不查库**，用已经取到的数做算术（差值/合计/占比/倍数）——"
                        + "「最高最低差多少」「合计多少」「是几倍」这类只有算术、没有新数据的，就必须用 compute ✗ 别再去查一遍库 ✗\n"
                        + "      （算出来的数由系统用已有数字计算，不需要你给具体值，metric 可为 null）\n")
                .append("- 【一条需求可以包含同一口径下的多个指标】问「A 和 B 分别是多少」但 A、B 来自**同一张表、同一时间窗、"
                        + "同一分组**时（如「返修率和报废率」），**不要拆成两条需求**：合成一条需求、一次取回来即可\n"
                        + "    · 只有当两个指标**口径/维度/时间窗不同**，或需要**两个不同能力**时，才拆成两条需求。\n")
                .append("- 【主项在前，原因类需求 on dependsOn】问「X 是哪个？为什么？」这类取值 + 归因的问题：\n"
                        + "    · **第 1 项必须是取值的需求**（哪一天/哪个产品/哪道工序，如 groupBy=day 取极值那一行）；\n"
                        + "    · 第 2 项才是归因（how=operator），并且**必须** dependsOn=[0] —— 「为什么」要解释的就是第 1 项"
                        + "选出来的那个对象 ✗ 否则归因会自己另选一个对象（实测：第 1 项没抽出哪一天最低 ✗ "
                        + "归因却在讲另一个日期的贡献度 ✗ 用户看到的主语对不上 ✗）；\n"
                        + "    · **不允许**省略本该有的取值项：即使该维度没登记，也要用 registered/self 把它列出来。\n")
                .append("- 【how 由你选，只能四选一】\n"
                        + "    · operator   = 分析算子（变化归因 / 两期对比 / 集中度）；要「为什么/原因/来自哪里」用它；\n"
                        + "    · registered = 已登记指标（口径权威、与页面同源）；六条判据全满足时用它；\n"
                        + "    · self       = 登记能力不满足（要按工序过滤、派生量、反事实、按天取极值…）→ 自己生成 SQL；\n"
                        + "    · compute    = 不查库，用已取到的数算（差值/合计/占比/倍数）。\n")
                .append("- 目标 JSON 结构（**needs 是数组：一条需求就 1 个元素，多条需求就多个元素，最多 10 个**）: "
                        + "{\"task\":\"biz|non_biz\",\"needs\":[{"
                        + "\"want\":\"这条需求想要什么（人话，如「今天报工总数与良品率」）\","
                        + "\"intent\":\"能力 code|NOT_SUPPORTED\","
                        + "\"how\":\"operator|registered|self|compute\","
                        + "\"routeReason\":\"为什么这样选（不满足时写明哪条判据）\","
                        + "\"groupBy\":null,\"filter\":{},\"entities\":{},"
                        + "\"time\":{\"type\":\"today|month|year|custom\",\"startDate\":\"YYYY-MM-DD\",\"endDate\":\"YYYY-MM-DD\"},"
                        + "\"order\":{\"by\":\"totalNum\",\"dir\":\"asc|desc\",\"limit\":3},"
                        + "\"statScope\":\"production\",\"dependsOn\":[]}],"
                        + "\"clarify\":null,\"notSupportedReason\":null,\"notSupportedHint\":null}\n")
                .append("  （how 缺省视为 registered；filter 用于限定某个产品/工序/员工 ✓ entities 用于实体识别，两者都可填）\n")
                .append("- 当 intent=\"NOT_SUPPORTED\" 时：结合【对话上下文】判断原因并给出引导——"
                        + "notSupportedReason=为什么不能答（信息不足/话题超范围/指代不明），notSupportedHint=一条可问的建议或补全提示；"
                        + "不要建议能力清单之外的内容\n")
                .append("- 实体指代不清时: clarify={\"question\":\"...\",\"options\":[\"...\",\"...\"]}，intent 仍填最可能项");
        return sb.toString();
    }

    /** 从回复中提取 JSON 片段（鲁棒：截取首个 {...} 至末尾） */
    private String extractJson(String reply) {
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return reply.substring(start, end + 1);
    }
}

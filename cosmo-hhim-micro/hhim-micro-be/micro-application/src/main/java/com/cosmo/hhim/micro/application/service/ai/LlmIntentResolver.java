/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
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
            String intent = o.getString("intent");
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
        for (OntologyCapabilityDTO c : caps) {
            sb.append(c.getCode()).append("   名称=").append(c.getName())
                    .append("   别名=[").append(String.join(", ", c.getAliases())).append("]\n")
                    .append("   参数: ").append(c.getParams().isEmpty() ? "无" : String.join(", ", c.getParams()))
                    .append("；允许维度: ").append(c.getDims().isEmpty() ? "无" : String.join("/", c.getDims()))
                    .append("\n   示例: ").append(String.join(" | ", c.getExamples())).append("\n");
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
                .append("- 【归因/分析类问题】问\"为什么/原因/怎么降下来的/谁拖后腿\"时：选能力 ATTRIBUTION（变化归因），"
                        + "并把被分析的指标放进 entities.metricCode（如 PRODUCT_PASS_RATE/PROCESS_PASS_RATE/EMPLOYEE_PASS_RATE），"
                        + "groupBy 填想按哪个维度找原因（默认 process）；问\"集中在哪/主要是谁\"时选 CONCENTRATE。"
                        + "这类问题**不要**置 NOT_SUPPORTED，也不要自己给结论\n")
                .append("- 【语义防线】若问题整体语义属于：创作/写作（写诗、写文章、编故事、写对联、写顺口溜、写段子）、"
                        + "翻译、主观评价、闲聊——即使句中包含业务关键词（产量/良品率/库存…），也必须置 NOT_SUPPORTED"
                        + "（notSupportedReason 说明超范围），不得映射到任何指标。"
                        + "例：「写一首关于产量的诗」→ NOT_SUPPORTED（虽然含\"产量\"），不是 SUMMARY\n")
                .append("- 问题语义可能省略（如\"谁记的工\"=\"哪个员工记工数量最多\"，\"那上个月呢\"=\"上个月同指标数据\"）："
                        + "先结合【对话上下文】补全成完整语义，再选择能力；补全后仍无法确定时，才 NOT_SUPPORTED 或 clarify\n")
                .append("- 别名命中优先（别名是口语等价词，不必与问题逐字相同）\n")
                .append("- 【实体继承】若本次问题本身已写明维度/范围（如「车削工序的良品率」「张三这个月良品率」），"
                        + "就只填本次问题里出现的实体，不要沿用上文的其它筛选实体（如上文问过某个产品）；"
                        + "仅当本次问题依赖上文（如「那上个月呢」「那工序呢」）时才补全并继承上文实体\n")
                .append("- 不允许回答工资、结算、任何金额类问题\n")
                .append("- 实体名用用户原话，不要臆造\n")
                .append("- time 只允许: today / month / year / custom / null(不关心，尽量不设)\n")
                .append("- groupBy(可选，分组粒度): 只能取【允许维度】中的值；用户问\"哪天/哪一天/按天\"类时 groupBy=day；无则省略或 null\n")
                .append("- statScope(统计口径，必填): production=已审核产出（问产量/良品率/合格/不良/完工等\"结果\"时）；"
                        + "submission=含未审核的报工行为（问\"报工/记工/报了多工/谁报工\"时）。拿不准默认 production\n")
                .append("- 目标 JSON 结构: {\"task\":\"biz|non_biz\",\"intent\":\"code\",\"groupBy\":null,\"entities\":{},\"time\":{\"type\":\"...\"},\"order\":{\"by\":\"totalNum\",\"dir\":\"asc|desc\",\"limit\":3},\"statScope\":\"production\",\"clarify\":null,\"notSupportedReason\":null,\"notSupportedHint\":null}\n")
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

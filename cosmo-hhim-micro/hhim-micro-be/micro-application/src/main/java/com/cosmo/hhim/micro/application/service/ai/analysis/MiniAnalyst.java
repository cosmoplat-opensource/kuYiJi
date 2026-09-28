/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.application.service.ai.analysis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 迷你分析师：把「两期对比 → 找主因 → 下钻」压缩成**纯计算**的确定性算法。
 *
 * <p>设计要点（与方案一致）：
 * <ul>
 *   <li><b>不碰数据库、不调 LLM</b>：输入是取数层给的聚合行，输出是结构化事实 + 可读事实清单；</li>
 *   <li><b>贡献度严格可加</b>：{@code 贡献ᵢ = passᵢᴮ/T_B − passᵢᴬ/T_A}，因此 {@code Σ贡献ᵢ = Δ总良品率}，
 *       可以对账、可以逐项相加，不存在残差项；</li>
 *   <li><b>口径与页面一致</b>：展示用的比率一律按页面口径截断 3 位（页面 = {@code BigDecimal.divide(...,3,DOWN)}），
 *       而贡献度用未截断的原始比值计算，避免二次误差导致 Σ贡献 ≠ Δ；</li>
 *   <li><b>不下因果结论</b>：只输出"哪一组贡献最大 + 样本量够不够"，措辞分级交给上层。</li>
 * </ul>
 *
 * <p>输入行键名与登记聚合 SQL 的别名保持一致：{@code group / groupKey / passNum / totalNum / rows}。
 */
public final class MiniAnalyst {

    /**
     * 主因判定（2026-09 调整）：**贡献绝对值最大的一项即主因** ✓
     * （门槛已取消：占比 ≥60% ✗ / 样本量 ≥20 条 ✗ —— 样本大小改为在结论里如实标注 ✓）
     */
    /** 可忽略阈值：最大贡献 |贡献| < 0.5 个百分点 → 变化太小，视为无可辨认主因 */
    private static final BigDecimal NEGLIGIBLE_PP = new BigDecimal("0.5");
    /** 内部计算精度（不做展示截断，展示才截断） */
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    /** 展示口径：与页面一致（良品率截断 3 位） */
    private static final int RATE_SCALE = 3;

    private MiniAnalyst() {
    }

    // ------------------------------------------------------------------ 模型

    /** 单个分组在某一期的聚合值 */
    public static class GroupStat {
        public String group;
        public String groupKey;
        public BigDecimal passNum = BigDecimal.ZERO;
        public BigDecimal totalNum = BigDecimal.ZERO;
        public long rows;

        public BigDecimal rateExact() {
            if (totalNum == null || totalNum.signum() == 0) {
                return null;
            }
            return passNum.divide(totalNum, MC);
        }
    }

    /** 单个分组对"总良品率变化"的贡献 */
    public static class Contribution {
        public String group;
        public String groupKey;
        /** 基期/本期该组良品率（0~1，未截断） */
        public BigDecimal rateA;
        public BigDecimal rateB;
        /** 对总良品率变化的贡献（百分点，可加；正=拉高，负=拉低） */
        public BigDecimal contributionPp = BigDecimal.ZERO;
        public long rowsA;
        public long rowsB;
        /** 基期没有、本期新出现的分组 */
        public boolean newGroup;
        /** 本期消失的分组 */
        public boolean goneGroup;

        public String getGroup() {
            return group;
        }

        public BigDecimal getContributionPp() {
            return contributionPp;
        }
    }

    /** 两期对比结果 */
    public static class CompareResult {
        public BigDecimal rateA;
        public BigDecimal rateB;
        /** 总良品率变化（百分点） */
        public BigDecimal deltaPp = BigDecimal.ZERO;
        public long rowsA;
        public long rowsB;
        /** 按 |贡献| 降序 */
        public List<Contribution> items = new ArrayList<>();
        /** Σ贡献（应等于 deltaPp，用于自检与对账） */
        public BigDecimal sumContributionPp = BigDecimal.ZERO;
        /** 主因分组名（贡献占比 ≥60% 且样本量达标），否则 null */
        public String mainGroup;
        /** 主因贡献占比（0~1），主因判定依据 */
        public BigDecimal mainShare;
        /** 样本量是否足以支撑结论 */
        public boolean sufficient;
    }

    /** 集中度结果（Top-N 占比） */
    public static class Concentration {
        public List<Contribution> items = new ArrayList<>();
        public BigDecimal totalNum = BigDecimal.ZERO;
        public BigDecimal topShare;
    }

    // ------------------------------------------------------------------ 核心算法

    /**
     * 两期对比 + 贡献度分解。
     *
     * @param periodA 基期聚合行
     * @param periodB 本期聚合行
     */
    public static CompareResult compare(List<Map<String, Object>> periodA, List<Map<String, Object>> periodB) {
        CompareResult r = new CompareResult();
        Map<String, GroupStat> a = group(periodA);
        Map<String, GroupStat> b = group(periodB);

        BigDecimal passA = sum(a.values(), true);
        BigDecimal totalA = sum(a.values(), false);
        BigDecimal passB = sum(b.values(), true);
        BigDecimal totalB = sum(b.values(), false);
        r.rowsA = rows(a.values());
        r.rowsB = rows(b.values());

        if (totalA.signum() == 0 || totalB.signum() == 0) {
            // 某一期完全没有质检数：算不出变化，明确返回"数据不足"（上层据此如实告知，不猜）
            r.sufficient = false;
            return r;
        }
        r.rateA = passA.divide(totalA, MC);
        r.rateB = passB.divide(totalB, MC);
        r.deltaPp = r.rateB.subtract(r.rateA).multiply(BigDecimal.valueOf(100));

        // key 并集：只出现一期的分组也要算（新增/消失本身就是原因之一），保证可加性
        Map<String, GroupStat> union = new LinkedHashMap<>();
        union.putAll(a);
        for (Map.Entry<String, GroupStat> e : b.entrySet()) {
            union.putIfAbsent(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, GroupStat> e : union.entrySet()) {
            GroupStat sa = a.get(e.getKey());
            GroupStat sb = b.get(e.getKey());
            Contribution c = new Contribution();
            c.group = sb != null ? sb.group : sa.group;
            c.groupKey = sb != null ? sb.groupKey : sa.groupKey;
            c.rateA = sa == null ? null : sa.rateExact();
            c.rateB = sb == null ? null : sb.rateExact();
            c.rowsA = sa == null ? 0 : sa.rows;
            c.rowsB = sb == null ? 0 : sb.rows;
            c.newGroup = sa == null;
            c.goneGroup = sb == null;
            // 贡献 = 该组在两期对总分数的占比之差（以总分母为基准），Σ = Δ
            BigDecimal shareB = sb == null ? BigDecimal.ZERO : sb.passNum.divide(totalB, MC);
            BigDecimal shareA = sa == null ? BigDecimal.ZERO : sa.passNum.divide(totalA, MC);
            c.contributionPp = shareB.subtract(shareA).multiply(BigDecimal.valueOf(100));
            r.sumContributionPp = r.sumContributionPp.add(c.contributionPp);
            r.items.add(c);
        }
        r.items.sort(Comparator.comparing((Contribution c) -> c.contributionPp.abs()).reversed());

        // 主因判定（业务判据，2026-09 调整）：**贡献最大的一项即主因** ✓
        //   · 取消"贡献占比 ≥60%"门槛 ✗（真实数据里各分组贡献常互相抵消，占比会 >100%，
        //     于是永远给不出主因 → 业务上等于没用）
        //   · 取消"样本量 ≥20 条"门槛 ✗（样本大小改为**如实标注**在结论里，由业务人员判断可信度 ✓，
        //     而不是替业务拒绝回答）
        //   · 仅保留"可忽略阈值"：最大贡献绝对值 < 0.5 个百分点时视为无可辨认主因（变化太小 ✓）
        if (!r.items.isEmpty() && r.deltaPp.signum() != 0) {
            Contribution first = r.items.get(0);
            BigDecimal share = first.contributionPp.abs()
                    .divide(r.deltaPp.abs(), 4, RoundingMode.HALF_UP);
            r.mainShare = share;
            if (first.contributionPp.abs().compareTo(NEGLIGIBLE_PP) >= 0) {
                r.mainGroup = first.group;
            }
        }
        r.sufficient = (r.rowsA + r.rowsB) > 0;
        return r;
    }

    /** 取贡献最大的前 N 个分组 */
    public static List<Contribution> top(CompareResult r, int n) {
        if (r == null || r.items.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(r.items.subList(0, Math.min(n, r.items.size())));
    }

    /**
     * 集中度：Top-N 分组的分子（如良品数/不良数）占总量的比例。
     *
     * <p>用于"不良集中在哪里""记工集中在谁"这类问题。
     */
    public static Concentration concentrate(List<Map<String, Object>> rows, int topN) {
        Concentration c = new Concentration();
        Map<String, GroupStat> grouped = group(rows);
        BigDecimal total = sum(grouped.values(), true);
        c.totalNum = total;
        if (grouped.isEmpty() || total.signum() == 0) {
            return c;
        }
        for (GroupStat s : grouped.values()) {
            Contribution item = new Contribution();
            item.group = s.group;
            item.groupKey = s.groupKey;
            item.rowsA = s.rows;
            item.contributionPp = s.passNum.divide(total, MC).multiply(BigDecimal.valueOf(100));
            c.items.add(item);
        }
        c.items.sort(Comparator.comparing((Contribution x) -> x.contributionPp).reversed());
        int n = Math.min(topN <= 0 ? 3 : topN, c.items.size());
        c.items = new ArrayList<>(c.items.subList(0, n));
        BigDecimal topSum = BigDecimal.ZERO;
        for (Contribution item : c.items) {
            topSum = topSum.add(item.contributionPp);
        }
        c.topShare = topSum.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return c;
    }

    // ------------------------------------------------------------------ 事实清单（喂 LLM）

    /**
     * 生成"事实清单"文本：LLM 只能引用这里的数字，不得自行计算或编造。
     */
    public static String brief(CompareResult r, String metricLabel, String labelA, String labelB) {
        if (r == null || r.rateA == null || r.rateB == null) {
            return "数据不足：对比期存在完全没有质检数的区间，无法给出变化结论。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(metricLabel).append("：").append(labelA).append(" ")
                .append(pct(r.rateA)).append(" → ").append(labelB).append(" ").append(pct(r.rateB))
                .append("，变化 ").append(pp(r.deltaPp)).append("（样本：")
                .append(r.rowsA).append(" → ").append(r.rowsB).append(" 条）\n");
        for (Contribution c : top(r, 3)) {
            if (c.contributionPp.abs().compareTo(NEGLIGIBLE_PP) < 0) {
                continue;
            }
            sb.append("- ").append(c.group).append("：贡献 ").append(pp(c.contributionPp));
            if (c.rateA != null && c.rateB != null) {
                sb.append("（该组 ").append(pct(c.rateA)).append(" → ").append(pct(c.rateB)).append("）");
            } else if (c.newGroup) {
                sb.append("（本期新出现：上期无数据 → 变化**全部由它带来**，回答时应说明它是新增来源）");
            } else if (c.goneGroup) {
                sb.append("（本期无数据：本期没有它的记录 → **不构成本期变化的原因**，回答时应明确说明"
                        + "「本期无数据、不是原因」✗ 不要笼统说「不明确」）");
            }
            sb.append("，样本 ").append(c.rowsA).append(" → ").append(c.rowsB).append(" 条\n");
        }
        if (r.mainGroup != null) {
            Contribution main = top(r, 1).get(0);
            // 主因 = 贡献绝对值最大的一项（2026-09 起取消 60% 占比与样本量门槛 ✓）
            // 样本量**如实附在后面** ✓：由业务人员据此判断可信度，而不是替业务拒绝回答
            sb.append("主因：").append(r.mainGroup).append("（贡献 ").append(pp(main.contributionPp));
            // 占比只在"主因贡献不超过总变化太多"时报；否则报占比会出现 251% 这种让人以为算错的数字
            // （含义是：主因的绝对贡献大于总变化，说明其它分组同期反向变动抵消了部分变化）
            if (r.mainShare != null && r.mainShare.doubleValue() <= 1.2) {
                sb.append("，占总变化的 ").append(pct(r.mainShare));
            } else {
                sb.append("，其绝对贡献大于总变化——其余分组同期反向变动，抵消了部分变化");
            }
            sb.append("；该组样本 ").append(main.rowsA).append(" → ").append(main.rowsB).append(" 条")
                    // 注意措辞：**不要出现任何"请结合业务判断/样本不足"这类可被润色误读为"不明确"的话** ✗
                    // （历史 bug：写了"样本较小，结论请结合业务判断"✗ → 润色输出"主因不明确"✗）
                    .append(main.rowsB < 20 ? "（样本量仅供参考，**不影响主因结论**）" : "")
                    .append("）\n");
            // 明确指令：**主因已确定**，回答时必须写出它 ✗ 不许把"样本较小"对冲成"主因不明确"
            sb.append("★本清单已给出确定主因：回答时**必须写明主因是「").append(r.mainGroup)
                    .append("」** ✗ 不得改写成「主因不明确」✗；样本量只作为附注说明，"
                            + "不得用它否定主因结论 ✗。"
                            + "**除非本清单明确写了「无可辨认的主因」，否则回答中禁止出现「主因不明确」字样** ✗\n");
        } else {
            sb.append("主因：变化幅度过小（最大贡献不足 0.5 个百分点），无可辨认的主因；")
                    .append("各分组贡献见上，结论只能作为\"可能原因\"\n");
        }
        sb.append("口径：已审报工（submit_status=0）；比率按页面口径截断 3 位；贡献度合计等于总变化 "
                ).append(pp(r.sumContributionPp)).append("。");
        // 口径说明（防"下降最多 / 贡献最大"混用）：两者是不同口径，回答时必须讲清按哪个
        sb.append("\n口径提示：**贡献度**=该分组对【总变化】的影响（谁拖累最大看它 ✓）；"
                + "**自身降幅**=该分组自己的比率降了几个百分点（上面每行的 X% → Y% 即为其自身变化 ✓）。"
                + "两者含义不同，回答时必须写明按哪个口径 ✗ 不得混用。");
        // 防润色省略关键解释：贡献绝对值大于总变化时，必须把"反向抵消"这句一并说出来
        sb.append("\n★回答要求：若主因的绝对贡献大于总变化（如贡献 73.7 而总变化仅 4.2），"
                + "**必须保留「其绝对贡献大于总变化——其余分组同期反向变动，抵消了部分变化」这句解释** ✗ "
                + "不得省略，否则读者会以为数字算错 ✗");
        return sb.toString();
    }

    /** 集中度事实清单 */
    public static String brief(Concentration c, String metricLabel) {
        if (c == null || c.items.isEmpty()) {
            return "数据不足：没有可用于集中度分析的数据。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(metricLabel).append("：Top ").append(c.items.size()).append(" 合计占比 ")
                .append(pct(c.topShare)).append("\n");
        for (Contribution item : c.items) {
            sb.append("- ").append(item.group).append("：").append(pct(item.contributionPp))
                    .append("（样本 ").append(item.rowsA).append(" 条）\n");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ 展示格式化（与页面同口径）

    /**
     * 比率展示：口径上截断 3 位（与页面计算一致），**展示上截断 1 位**（页面观感也是 95.2% 这种）。
     *
     * <p>两步都要保留：先按口径截断避免"用四舍五入算出的数"，再按展示习惯截断避免出现 96.600%。
     */
    public static String pct(BigDecimal rate) {
        if (rate == null) {
            return "—";
        }
        BigDecimal byScope = rate.setScale(RATE_SCALE, RoundingMode.DOWN);
        BigDecimal shown = byScope.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.DOWN);
        return shown.stripTrailingZeros().toPlainString() + "%";
    }

    /** 百分点展示：保留 1 位（-1.9 个百分点） */
    public static String pp(BigDecimal pp) {
        if (pp == null) {
            return "—";
        }
        return pp.setScale(1, RoundingMode.HALF_UP).toPlainString() + " 个百分点";
    }

    // ------------------------------------------------------------------ 内部工具

    private static Map<String, GroupStat> group(List<Map<String, Object>> rows) {
        Map<String, GroupStat> map = new LinkedHashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            String group = str(row.get("group"));
            if (group == null || group.isEmpty()) {
                continue;
            }
            String key = str(row.get("groupKey"));
            final String groupKeyFinal = (key == null || key.isEmpty()) ? group : key;
            GroupStat s = map.computeIfAbsent(groupKeyFinal, k -> {
                GroupStat n = new GroupStat();
                n.group = group;
                n.groupKey = groupKeyFinal;
                return n;
            });
            s.passNum = s.passNum.add(num(row.get("passNum")));
            s.totalNum = s.totalNum.add(num(row.get("totalNum")));
            s.rows += (long) num(row.get("rows")).doubleValue();
        }
        return map;
    }

    private static BigDecimal sum(Iterable<GroupStat> stats, boolean pass) {
        BigDecimal total = BigDecimal.ZERO;
        for (GroupStat s : stats) {
            total = total.add(pass ? s.passNum : s.totalNum);
        }
        return total;
    }

    private static long rows(Iterable<GroupStat> stats) {
        long n = 0;
        for (GroupStat s : stats) {
            n += s.rows;
        }
        return n;
    }

    private static BigDecimal num(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        if (v instanceof Number) {
            return new BigDecimal(v.toString());
        }
        try {
            return new BigDecimal(v.toString().trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }
}

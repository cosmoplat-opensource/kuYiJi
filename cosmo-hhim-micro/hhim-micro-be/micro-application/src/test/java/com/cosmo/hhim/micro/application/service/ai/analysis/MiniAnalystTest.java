/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai.analysis;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MiniAnalyst 纯算法单测（不连库、不调 LLM）。
 *
 * <p>覆盖：可加性、主因判定、新增/消失分组、零分母、空输入、集中度、展示口径与页面一致。
 */
public class MiniAnalystTest {

    /** 便于构造聚合行 */
    private static Map<String, Object> row(String group, String key, String pass, String total, int rows) {
        Map<String, Object> m = new HashMap<>();
        m.put("group", group);
        m.put("groupKey", key);
        m.put("passNum", new BigDecimal(pass));
        m.put("totalNum", new BigDecimal(total));
        m.put("rows", rows);
        return m;
    }

    /** ① 可加性：Σ贡献 = Δ总良品率（这是能"对账"的根本） */
    @Test
    public void sumOfContributionsEqualsTotalDelta() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("攻丝", "G2", "21", "22", 11));
        a.add(row("车削", "G1", "40", "41", 20));
        a.add(row("下料", "G3", "12", "12", 6));

        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("攻丝", "G2", "30", "34", 17));
        b.add(row("车削", "G1", "20", "21", 10));
        b.add(row("下料", "G3", "10", "11", 5));

        MiniAnalyst.CompareResult r = MiniAnalyst.compare(a, b);

        Assert.assertNotNull(r.rateA);
        Assert.assertNotNull(r.rateB);
        // Σ贡献 == Δ（误差 1e-9 以内）
        Assert.assertEquals(0d,
                r.sumContributionPp.subtract(r.deltaPp).abs().doubleValue(), 1e-9);
    }

    /** ② 主因判定：攻丝明显变差（96.7%→88.2%），应被识别为主因 */
    @Test
    public void identifiesMainCause() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("攻丝", "G2", "40", "41", 20));
        a.add(row("车削", "G1", "40", "41", 20));
        a.add(row("下料", "G3", "30", "31", 15));

        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("攻丝", "G2", "30", "34", 24));   // 88.2%
        b.add(row("车削", "G1", "40", "41", 20));
        b.add(row("下料", "G3", "30", "31", 15));

        MiniAnalyst.CompareResult r = MiniAnalyst.compare(a, b);

        Assert.assertTrue("本期总良品率应低于基期", r.deltaPp.signum() < 0);
        Assert.assertEquals("攻丝", r.mainGroup);
        Assert.assertEquals("攻丝", MiniAnalyst.top(r, 1).get(0).group);
        Assert.assertTrue(r.mainShare.doubleValue() >= 0.6);
    }

    /** ③ 新增组 / 消失组：只出现一期的分组也要计入，且不破坏可加性 */
    @Test
    public void handlesNewAndGoneGroups() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("攻丝", "G2", "20", "21", 10));
        a.add(row("已停产工序", "G9", "10", "10", 5));   // 本期消失

        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("攻丝", "G2", "20", "21", 10));
        b.add(row("新工序", "G8", "0", "10", 5));        // 本期新增且全是不良

        MiniAnalyst.CompareResult r = MiniAnalyst.compare(a, b);

        Assert.assertEquals(0d, r.sumContributionPp.subtract(r.deltaPp).abs().doubleValue(), 1e-9);
        boolean hasNew = false;
        boolean hasGone = false;
        for (MiniAnalyst.Contribution c : r.items) {
            if ("新工序".equals(c.group)) {
                hasNew = c.newGroup;
            }
            if ("已停产工序".equals(c.group)) {
                hasGone = c.goneGroup;
            }
        }
        Assert.assertTrue("应标记本期新出现的分组", hasNew);
        Assert.assertTrue("应标记本期消失的分组", hasGone);
    }

    /** ④ 零分母：某组两期都没有质检数 → 贡献为 0，不抛异常、不影响可加性 */
    @Test
    public void zeroDenominatorGroupIsIgnored() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("攻丝", "G2", "20", "21", 10));
        a.add(row("空工序", "G0", "0", "0", 1));   // 已审但质检数=0（历史脏数据）

        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("攻丝", "G2", "15", "21", 10));
        b.add(row("空工序", "G0", "0", "0", 1));

        MiniAnalyst.CompareResult r = MiniAnalyst.compare(a, b);
        Assert.assertEquals(0d, r.sumContributionPp.subtract(r.deltaPp).abs().doubleValue(), 1e-9);
        for (MiniAnalyst.Contribution c : r.items) {
            if ("空工序".equals(c.group)) {
                Assert.assertEquals(0, c.contributionPp.signum());
                Assert.assertNull(c.rateA);
            }
        }
    }

    /** ⑤ 空输入 / 单期无数据：明确返回"数据不足"，不猜、不抛异常 */
    @Test
    public void emptyAndOneSidedInput() {
        MiniAnalyst.CompareResult empty = MiniAnalyst.compare(new ArrayList<>(), new ArrayList<>());
        Assert.assertFalse(empty.sufficient);
        Assert.assertNull(empty.rateA);
        Assert.assertTrue(empty.items.isEmpty());
        Assert.assertTrue(MiniAnalyst.brief(empty, "良品率", "8月", "9月").contains("数据不足"));

        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("攻丝", "G2", "30", "34", 17));
        MiniAnalyst.CompareResult oneSided = MiniAnalyst.compare(new ArrayList<>(), b);
        Assert.assertFalse(oneSided.sufficient);
        Assert.assertNull("基期无数据时不应给出基期良品率", oneSided.rateA);
        Assert.assertEquals(0, oneSided.deltaPp.signum());
    }

    /** ⑥ 集中度：Top-2 占比 */
    @Test
    public void concentrateTopN() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row("划伤", "NG1", "7", "7", 7));
        rows.add(row("尺寸超差", "NG2", "2", "2", 2));
        rows.add(row("毛刺", "NG3", "1", "1", 1));

        MiniAnalyst.Concentration c = MiniAnalyst.concentrate(rows, 2);
        Assert.assertEquals(2, c.items.size());
        Assert.assertEquals("划伤", c.items.get(0).group);
        Assert.assertEquals(0.9d, c.topShare.doubleValue(), 1e-6);   // (7+2)/10
    }

    /** ⑦ 展示口径与页面一致：168/182 = 0.923076… → 截断 3 位 = 92.3%（不是四舍五入的 92.3% 也要对上） */
    @Test
    public void displayMatchesPageTruncationRule() {
        Assert.assertEquals("92.3%", MiniAnalyst.pct(new BigDecimal("168").divide(new BigDecimal("182"), 10, BigDecimal.ROUND_HALF_UP)));
        // 93.75% → 截断 3 位 = 93.7%（四舍五入会得 93.8%，页面口径是截断）
        Assert.assertEquals("93.7%", MiniAnalyst.pct(new BigDecimal("0.9375")));
        Assert.assertEquals("-1.9 个百分点", MiniAnalyst.pp(new BigDecimal("-1.85")));
    }

    /** ⑧ 事实清单：必须带口径与合计，供上层措辞引用 */
    @Test
    public void briefContainsScopeAndSum() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("攻丝", "G2", "40", "41", 20));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("攻丝", "G2", "30", "34", 24));
        MiniAnalyst.CompareResult r = MiniAnalyst.compare(a, b);

        String brief = MiniAnalyst.brief(r, "良品率", "2026-08", "2026-09");
        Assert.assertTrue(brief.contains("已审报工"));
        Assert.assertTrue(brief.contains("贡献度合计"));
        Assert.assertTrue(brief.contains("攻丝"));
    }
}

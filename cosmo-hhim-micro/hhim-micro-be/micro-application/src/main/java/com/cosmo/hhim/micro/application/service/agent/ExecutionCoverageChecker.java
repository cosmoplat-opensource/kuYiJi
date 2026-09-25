/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.agent;

import com.cosmo.hhim.micro.application.dto.ai.OntologyCapabilityDTO;
import com.cosmo.hhim.micro.application.service.ai.IntentExecutor;
import com.cosmo.hhim.micro.application.service.ai.OntologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * AI 问数 · 能力覆盖自检（启动时）
 *
 * <p>登记制的固有风险是"本体承诺"与"执行器实现"漂移：本体 {@code dims} 说可以这么问，
 * 执行器却没登记实现——直到用户真的问到才暴露。本自检在启动时把差集打进日志（[AI覆盖]），
 * 让"该登记清单"在部署时就能看到，而不是靠用户踩坑。
 *
 * <p>只告警不阻断启动：覆盖缺口是业务待办（登记组合 / 登记 SQL / 从本体移除模板），不是运行故障。
 * 与 {@code IntentExecutor} 的组合台账是唯一对齐点：新增指标/组合后请同步更新台账，本自检会复核。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionCoverageChecker {

    private final OntologyService ontologyService;

    /** 时间范围维度（非分组粒度）：本体 dims 目前把 time 与分组粒度混放，这里显式跳过（待拆成 timeRange/groupBy） */
    private static final String TIME_DIM = "time";

    @PostConstruct
    public void check() {
        try {
            List<OntologyCapabilityDTO> caps = ontologyService.capabilities();
            if (caps.isEmpty()) {
                log.warn("[AI覆盖] 本体无生效指标（未配置/未发布），问数入口应视为不可用");
                return;
            }
            List<String> unregistered = new ArrayList<>();
            List<String> missingCombos = new ArrayList<>();
            for (OntologyCapabilityDTO c : caps) {
                if (!IntentExecutor.isRegistered(c.getCode())) {
                    unregistered.add(c.getCode());
                    continue;
                }
                Set<String> covered = new LinkedHashSet<>(IntentExecutor.consumedGroupBys(c.getCode()));
                covered.addAll(IntentExecutor.toleratedGroupBys(c.getCode()));
                for (String dim : c.getDims()) {
                    if (!StringUtils.hasText(dim) || TIME_DIM.equals(dim) || covered.contains(dim)) {
                        continue;
                    }
                    missingCombos.add(c.getCode() + "×" + dim);
                }
            }
            // 反向：执行器登记了但本体没有（dims 已删/指标已下架）→ 死代码，值得清理
            List<String> orphan = new ArrayList<>();
            for (String code : IntentExecutor.registeredMetrics()) {
                boolean found = false;
                for (OntologyCapabilityDTO c : caps) {
                    if (code.equals(c.getCode())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    orphan.add(code);
                }
            }

            if (unregistered.isEmpty() && missingCombos.isEmpty() && orphan.isEmpty()) {
                log.info("[AI覆盖] 本体承诺与执行器实现一致：{} 个指标，无未登记组合", caps.size());
                return;
            }
            if (!unregistered.isEmpty()) {
                log.warn("[AI覆盖] 本体已发布但执行器未登记（问到会走\"还没学会\"话术，需登记执行器）: {}", unregistered);
            }
            if (!missingCombos.isEmpty()) {
                log.warn("[AI覆盖] 本体 dims 承诺但执行器未实现（问到会给\"还没学会\"话术；"
                        + "需登记组合/登记 SQL，或从本体问题模板中移除）: {}", missingCombos);
            }
            if (!orphan.isEmpty()) {
                log.warn("[AI覆盖] 执行器已登记但本体无对应指标（死代码，建议清理）: {}", orphan);
            }
        } catch (Exception e) {
            // 自检失败不影响启动
            log.warn("[AI覆盖] 自检跳过: {}", e.getMessage());
        }
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.GuideNodeChangeStatusParam;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroExperienceGuideGroup;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroExperienceGuideNode;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExperienceGuideService;
import com.cosmo.hhim.micro.infrastructure.enums.GuideStatusFlagEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * decouple-from-ops-platform-cleanup (A.9): 体验引导（trial 专属）已下线。
 * 本实现类不再调 im-api-operation，全部 no-op。
 *
 * 真实清理（删除 controller + 9 张 micro_experience_guide_* 表）走后续 DBA 工单。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroExperienceGuideServiceImpl implements IMicroExperienceGuideService {

    @Override
    public GuideStatusFlagEnum getGuideFlag() {
        log.debug("[deprecated] getGuideFlag no-op");
        // trial 下线：返回 GUIDE_DONE（已完成的等价语义，避免触发引导）
        return GuideStatusFlagEnum.GUIDE_DONE;
    }

    @Override
    public List<MicroExperienceGuideNode> getGuideList(String groupCode) {
        log.debug("[deprecated] getGuideList({}) no-op", groupCode);
        return Collections.emptyList();
    }

    @Override
    public MicroExperienceGuideGroup getGuideGroupInfo(String groupCode) {
        log.debug("[deprecated] getGuideGroupInfo({}) no-op", groupCode);
        return null;
    }

    @Override
    public Boolean updateNodeStatus(GuideNodeChangeStatusParam param) {
        log.debug("[deprecated] updateNodeStatus no-op");
        return false;
    }

    @Override
    public Boolean resetExperienceGuide(List<String> guideGroupCodes) {
        log.debug("[deprecated] resetExperienceGuide no-op");
        return false;
    }

    @Override
    public Boolean skipGuide(List<String> guideGroupCodes) {
        log.debug("[deprecated] skipGuide no-op");
        return false;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.GuideNodeChangeStatusParam;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroExperienceGuideGroup;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroExperienceGuideNode;
import com.cosmo.hhim.micro.infrastructure.enums.GuideStatusFlagEnum;

import java.util.List;

/**
 * 体验引导节点配置Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-01
 */
public interface IMicroExperienceGuideService {

    /**
     * 获取体验引导标识
     *
     * @return
     */
    GuideStatusFlagEnum getGuideFlag();

    /**
     * 根据引导组编码查询引导节点列表
     *
     * @param groupCode
     * @return
     */
    List<MicroExperienceGuideNode> getGuideList(String groupCode);

    /**
     * 根据引导组编码查询引导组信息
     *
     * @param groupCode
     * @return
     */
    MicroExperienceGuideGroup getGuideGroupInfo(String groupCode);

    /**
     * 更新节点状态
     *
     * @param param
     */
    Boolean updateNodeStatus(GuideNodeChangeStatusParam param); 

    /**
     * 重置体验
     *
     * @return
     */
    Boolean resetExperienceGuide(List<String> guideGroupCodes); 

    /**
     * 跳过体验引导
     *
     * @return
     */
    Boolean skipGuide(List<String> guideGroupCodes); 

}

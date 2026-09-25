/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroExperienceGuideGroup;

import java.util.List;

/**
 * 体验引导节点组Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-01
 */
public interface MicroExperienceGuideGroupMapper {
    /**
     * 查询体验引导节点组
     *
     * @param groupCode 引导组编码
     * @return 体验引导节点组
     */
    MicroExperienceGuideGroup selectMicroExperienceGuideGroupByCode(String groupCode);

    /**
     * 查询体验引导节点组列表
     *
     * @param microExperienceGuideGroup 体验引导节点组
     * @return 体验引导节点组集合
     */
    public List<MicroExperienceGuideGroup> selectMicroExperienceGuideGroupList(MicroExperienceGuideGroup microExperienceGuideGroup);

    /**
     * 新增体验引导节点组
     *
     * @param microExperienceGuideGroup 体验引导节点组
     * @return 结果
     */
    public int insertMicroExperienceGuideGroup(MicroExperienceGuideGroup microExperienceGuideGroup);

    /**
     * 修改体验引导节点组
     *
     * @param microExperienceGuideGroup 体验引导节点组
     * @return 结果
     */
    int updateMicroExperienceGuideGroup(MicroExperienceGuideGroup microExperienceGuideGroup);

    /**
     * 更新所有引导组为完成状态
     *
     * @return
     */
    int updateAllGuideGroupDone(MicroExperienceGuideGroup microExperienceGuideGroup); 

    /**
     * 删除体验引导节点组
     *
     * @param id 体验引导节点组ID
     * @return 结果
     */
    public int deleteMicroExperienceGuideGroupById(Long id);

    /**
     * 批量删除体验引导节点组
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroExperienceGuideGroupByIds(Long[] ids);
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroExperienceGuideNode;

import java.util.List;

/**
 * 体验引导节点配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-01
 */
public interface MicroExperienceGuideNodeMapper {
    /**
     * 查询体验引导节点配置
     *
     * @param nodeCode
     * @return 体验引导节点配置
     */
    MicroExperienceGuideNode selectMicroExperienceGuideNodeByCode(String nodeCode);

    /**
     * 查询体验引导节点配置列表
     *
     * @param microExperienceGuideNode 体验引导节点配置
     * @return 体验引导节点配置集合
     */
    public List<MicroExperienceGuideNode> selectMicroExperienceGuideNodeList(MicroExperienceGuideNode microExperienceGuideNode);

    /**
     * 新增体验引导节点配置
     *
     * @param microExperienceGuideNode 体验引导节点配置
     * @return 结果
     */
    public int insertMicroExperienceGuideNode(MicroExperienceGuideNode microExperienceGuideNode);

    /**
     * 修改体验引导节点配置
     *
     * @param microExperienceGuideNode 体验引导节点配置
     * @return 结果
     */
    int updateMicroExperienceGuideNode(MicroExperienceGuideNode microExperienceGuideNode);

    /**
     * 按照组编码更新引导节点信息
     *
     * @param microExperienceGuideNode
     * @return
     */
    int updateMicroExperienceByGroup(MicroExperienceGuideNode microExperienceGuideNode);

    /**
     * 删除体验引导节点配置
     *
     * @param id 体验引导节点配置ID
     * @return 结果
     */
    public int deleteMicroExperienceGuideNodeById(Long id);

    /**
     * 批量删除体验引导节点配置
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroExperienceGuideNodeByIds(Long[] ids);

    /**
     * 按白名单清理数据（表名静态分发，杜绝动态 SQL 拼接）
     *
     * @param tableName 白名单内业务表名
     * @return
     */
    int dynamicDeleteData(String tableName);
}

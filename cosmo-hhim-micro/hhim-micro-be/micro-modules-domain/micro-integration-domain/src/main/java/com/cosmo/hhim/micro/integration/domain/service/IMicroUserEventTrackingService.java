/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service;

import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUseButtonEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUsePageEntity;

import java.util.List;
import java.util.Map;

/**
 * 用户行为事件埋点Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-23
 */
public interface IMicroUserEventTrackingService {
    /**
     * 查询用户行为事件埋点
     *
     * @param id 用户行为事件埋点ID
     * @return 用户行为事件埋点
     */
    public MicroUserEventTrackingEntity selectMicroUserEventTrackingById(Long id);

    /**
     * 查询用户行为事件埋点列表
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 用户行为事件埋点集合
     */
    public List<MicroUserEventTrackingEntity> selectMicroUserEventTrackingList(MicroUserEventTrackingEntity microUserEventTracking);

    /**
     * 新增用户行为事件埋点
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 结果
     */
    public int insertMicroUserEventTracking(MicroUserEventTrackingEntity microUserEventTracking);

    /**
     * 修改用户行为事件埋点
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 结果
     */
    public int updateMicroUserEventTracking(MicroUserEventTrackingEntity microUserEventTracking);

    /**
     * 批量删除用户行为事件埋点
     *
     * @param ids 需要删除的用户行为事件埋点ID
     * @return 结果
     */
    public int deleteMicroUserEventTrackingByIds(Long[] ids);

    /**
     * 删除用户行为事件埋点信息
     *
     * @param id 用户行为事件埋点ID
     * @return 结果
     */
    public int deleteMicroUserEventTrackingById(Long id);

    /**
     * 统计使用次数最多的来源页面(导航栏)
     *
     * @param microUserMostUsePageEntity
     */
    Map<String, Double> getUsingTimesByRefCondition(MicroUserMostUsePageEntity microUserMostUsePageEntity); 

    /**
     * 统计使用次数最多的来源页面(子页面)
     *
     * @param microUserMostUsePageEntity
     * @return
     */
    Map<String, Double> getUsingTimesByCurrentCondition(MicroUserMostUsePageEntity microUserMostUsePageEntity);

    /**
     * 根据用户使用情况,推荐常用的按钮
     *
     * @param buttonEntity
     * @return
     */
    List<String> findUserPersonalizedRecommendButton(MicroUserMostUseButtonEntity buttonEntity);
}

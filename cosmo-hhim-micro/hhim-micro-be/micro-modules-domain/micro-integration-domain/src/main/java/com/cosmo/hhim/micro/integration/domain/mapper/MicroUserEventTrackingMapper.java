/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.mapper;

import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUseButtonEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUsePageEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户行为事件埋点Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-23
 */
public interface MicroUserEventTrackingMapper {
    /**
     * 查询用户行为事件埋点
     *
     * @param id 用户行为事件埋点ID
     * @return 用户行为事件埋点
     */
    MicroUserEventTrackingEntity selectMicroUserEventTrackingById(Long id);

    /**
     * 查询用户行为事件埋点列表
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 用户行为事件埋点集合
     */
    List<MicroUserEventTrackingEntity> selectMicroUserEventTrackingList(MicroUserEventTrackingEntity microUserEventTracking);

    /**
     * 新增用户行为事件埋点
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 结果
     */
    int insertMicroUserEventTracking(MicroUserEventTrackingEntity microUserEventTracking);

    /**
     * 修改用户行为事件埋点
     *
     * @param microUserEventTracking 用户行为事件埋点
     * @return 结果
     */
    int updateMicroUserEventTracking(MicroUserEventTrackingEntity microUserEventTracking);

    /**
     * 删除用户行为事件埋点
     *
     * @param id 用户行为事件埋点ID
     * @return 结果
     */
    int deleteMicroUserEventTrackingById(Long id);

    /**
     * 批量删除用户行为事件埋点
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroUserEventTrackingByIds(Long[] ids);

    /**
     * 批量新增用户埋点事件
     *
     * @param list
     * @return
     */
    int insertMicroUserEventTrackingBatch(@Param("list") List<MicroUserEventTrackingEntity> list);

    /**
     * 统计使用次数最多的来源页面(导航栏)
     *
     * @param entity
     * @return
     */
    List<MicroUserEventTrackingEntity> selectUsingTimesByRefCondition(@Param("entity") MicroUserMostUsePageEntity entity);

    /**
     * 统计使用次数最多的来源页面(子页面)
     *
     * @param entity
     * @return
     */
    List<MicroUserEventTrackingEntity> selectUsingTimesByCurrentCondition(@Param("entity") MicroUserMostUsePageEntity entity);

    /**
     * 根据条件查询用户埋点行为数据
     *
     * @param buttonEntity
     * @return
     */
    List<MicroUserEventTrackingEntity> selectTrackingListByConditionList(@Param("entity") MicroUserMostUseButtonEntity buttonEntity);
}

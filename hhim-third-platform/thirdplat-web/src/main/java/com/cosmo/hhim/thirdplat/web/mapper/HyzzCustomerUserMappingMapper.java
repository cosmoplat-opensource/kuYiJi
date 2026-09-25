/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomerUserMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户租户映射Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-12-08
 */
public interface HyzzCustomerUserMappingMapper {
    /**
     * 查询用户租户映射
     *
     * @param id 用户租户映射ID
     * @return 用户租户映射
     */
    public HyzzCustomerUserMapping selectHyzzCustomerUserMappingById(Long id);

    /**
     * 查询用户租户映射列表
     *
     * @param hyzzCustomerUserMapping 用户租户映射
     * @return 用户租户映射集合
     */
    public List<HyzzCustomerUserMapping> selectHyzzCustomerUserMappingList(HyzzCustomerUserMapping hyzzCustomerUserMapping);

    /**
     * 新增用户租户映射
     *
     * @param hyzzCustomerUserMapping 用户租户映射
     * @return 结果
     */
    public int insertHyzzCustomerUserMapping(HyzzCustomerUserMapping hyzzCustomerUserMapping);

    /**
     * 修改用户租户映射
     *
     * @param hyzzCustomerUserMapping 用户租户映射
     * @return 结果
     */
    public int updateHyzzCustomerUserMapping(HyzzCustomerUserMapping hyzzCustomerUserMapping);

    /**
     * 删除用户租户映射
     *
     * @param id 用户租户映射ID
     * @return 结果
     */
    public int deleteHyzzCustomerUserMappingById(Long id);

    /**
     * 批量删除用户租户映射
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteHyzzCustomerUserMappingByIds(Long[] ids);

    /**
     * 修改映射表中userId
     * @param originUserId 原用户id
     * @param targetUserId 目标用户id
     * @return
     */
    int changeUserId(@Param("originUserId") Long originUserId, @Param("targetUserId") Long targetUserId);

    /**
     * 删除用户映射信息
     * @param customerCode
     * @param userId
     * @return
     */
    int deleteCustomerUserMapping(@Param("customerCode") String customerCode, @Param("userId") Long userId, @Param("productType") Integer productType);
}

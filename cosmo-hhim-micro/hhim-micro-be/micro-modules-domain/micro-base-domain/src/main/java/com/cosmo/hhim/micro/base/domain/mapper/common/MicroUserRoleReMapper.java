/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserRoleRe;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-22
 */
public interface MicroUserRoleReMapper {
    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    public MicroUserRoleRe selectMicroUserRoleReById(Long id);

    /**
     * 根据角色ID + 用户ID查询用户与角色的关联信息
     *
     * @param roleId
     * @param userId
     * @return
     */
    MicroUserRoleRe selectUserRoleInfoByRoleIdAndUserId(@Param("roleId") Long roleId, @Param("userId") Long userId);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param microUserRoleRe 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<MicroUserRoleRe> selectMicroUserRoleReList(MicroUserRoleRe microUserRoleRe);

    /**
     * 新增【请填写功能名称】
     *
     * @param microUserRoleRe 【请填写功能名称】
     * @return 结果
     */
    public int insertMicroUserRoleRe(MicroUserRoleRe microUserRoleRe);

    /**
     * 修改【请填写功能名称】
     *
     * @param microUserRoleRe 【请填写功能名称】
     * @return 结果
     */
    public int updateMicroUserRoleRe(MicroUserRoleRe microUserRoleRe);

    /**
     * 删除【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    public int deleteMicroUserRoleReById(Long id);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroUserRoleReByIds(Long[] ids);

    int deleteMicroUserRoleReByUser(Long userId, String tenantCode); 

    /**
     * 根据用户ID删除用户角色关联信息
     * @param userId
     * @return
     */
    int deleteMicroUserRoleReByUserId(Long userId);
}

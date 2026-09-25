/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserRoleRe;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @date 2022-10-12
 */
public interface MicroRoleMapper {

    /**
     * 根据角色编码查询角色信息
     *
     * @param roleCode
     * @return
     */
    MicroRole selectMicroRoleByRoleCode(String roleCode);

    /**
     * 查询角色列表
     *
     * @param microRole 角色
     * @return 角色集合
     */
    public List<MicroRole> selectMicroRoleList(MicroRole microRole);

    /**
     * 新增角色信息
     *
     * @param microRole
     * @return
     */
    public int insertMicroRole(MicroRole microRole);

    List<MicroRole> selectMicroUserRoleRe(MicroUserRoleRe re); 

    int replaceRole(MicroRole microRole); 
}

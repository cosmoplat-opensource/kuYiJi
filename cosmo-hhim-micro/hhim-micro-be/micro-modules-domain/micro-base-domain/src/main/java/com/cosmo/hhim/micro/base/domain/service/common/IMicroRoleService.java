/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;

import java.util.List;

/**
 * 角色Service接口
 *
 * @date 2022-10-12
 */
public interface IMicroRoleService {

    /**
     * 查询角色列表
     *
     * @param microRole 角色
     * @return 角色集合
     */
    List<MicroRole> selectMicroRoleList(MicroRole microRole);


    /**
     * 根据角色编码查询角色信息
     * @param roleCode
     * @return
     */
    MicroRole selectMicroRoleByRoleCode(String roleCode);

}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroRoleMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色Service业务层处理
 *
 * @date 2022-10-12
 */
@Service
public class MicroRoleServiceImpl implements IMicroRoleService {
    @Autowired
    private MicroRoleMapper microRoleMapper;


    /**
     * 查询角色列表
     *
     * @param microRole 角色
     * @return 角色
     */
    @Override
    public List<MicroRole> selectMicroRoleList(MicroRole microRole) {
        return microRoleMapper.selectMicroRoleList(microRole);
    }

    /**
     * 根据角色编码查询角色信息
     * @param roleCode
     * @return
     */
    @Override
    public MicroRole selectMicroRoleByRoleCode(String roleCode) {
        return microRoleMapper.selectMicroRoleByRoleCode(roleCode);
    }


}

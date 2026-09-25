/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;


import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserPlatformRe;

import java.util.List;

/**
 * 用户平台信息关联Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-21
 */
public interface MicroUserPlatformReMapper {

    /**
     * 查询用户平台信息关联列表
     *
     * @param microUserPlatformRe 用户平台信息关联
     * @return 用户平台信息关联集合
     */
    public List<MicroUserPlatformRe> selectMicroUserPlatfromReList(MicroUserPlatformRe microUserPlatformRe);

    /**
     * 新增用户平台信息关联
     *
     * @param microUserPlatformRe 用户平台信息关联
     * @return 结果
     */
    public int insertMicroUserPlatfromRe(MicroUserPlatformRe microUserPlatformRe);

    /**
     * 修改用户平台信息关联
     *
     * @param microUserPlatformRe 用户平台信息关联
     * @return 结果
     */
    public int updateMicroUserPlatfromRe(MicroUserPlatformRe microUserPlatformRe);

    /**
     * 删除用户平台信息关联
     *
     * @param id 用户平台信息关联ID
     * @return 结果
     */
    public int deleteMicroUserPlatfromReById(Long id);

    /**
     * 批量删除用户平台信息关联
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroUserPlatfromReByIds(Long[] ids);

    /**
     * 根据用户ID删除用户平台关联信息
     * @param userId
     * @return
     */
    int deleteMicroUserPlatfromReByUserId(Long userId);
}

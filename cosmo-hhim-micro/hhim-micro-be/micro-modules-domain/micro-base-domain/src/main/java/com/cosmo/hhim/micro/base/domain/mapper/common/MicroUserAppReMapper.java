/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserAppRe;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户与小程序映射关系Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-10
 */
public interface MicroUserAppReMapper {
    /**
     * 查询用户与小程序映射关系
     *
     * @param appCode
     * @param userId
     * @return 用户与小程序映射关系
     */
    MicroUserAppRe selectMicroUserAppReByAppCodeAndUserId(@Param("appCode") String appCode, @Param("userId") Long userId);

    /**
     * 查询用户与小程序映射关系列表
     *
     * @param microUserAppRe 用户与小程序映射关系
     * @return 用户与小程序映射关系集合
     */
    public List<MicroUserAppRe> selectMicroUserAppReList(MicroUserAppRe microUserAppRe);

    /**
     * 新增用户与小程序映射关系
     *
     * @param microUserAppRe 用户与小程序映射关系
     * @return 结果
     */
    public int insertMicroUserAppRe(MicroUserAppRe microUserAppRe);

    /**
     * 修改用户与小程序映射关系
     *
     * @param microUserAppRe 用户与小程序映射关系
     * @return 结果
     */
    public int updateMicroUserAppRe(MicroUserAppRe microUserAppRe);

    /**
     * 根据用户Id修改用户应用关联信息
     *
     * @param microUserAppRe
     * @return
     */
    int updateMicroUserAppReByUserId(MicroUserAppRe microUserAppRe);

    /**
     * 删除用户与小程序映射关系
     *
     * @param id 用户与小程序映射关系ID
     * @return 结果
     */
    public int deleteMicroUserAppReById(Long id);

    /**
     * 批量删除用户与小程序映射关系
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroUserAppReByIds(Long[] ids);

    /**
     * 根据用户ID删除用户应用关联信息
     * @param userId
     * @return
     */
    int deleteMicroUserAppReByUserId(Long userId);
}

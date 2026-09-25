/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.*;

import java.util.List;

/**
 * 用户Service接口
 *
 * @date 2022-10-11
 */
public interface IMicroUserService {
    /**
     * 查询用户
     *
     * @param id 用户ID
     * @return 用户
     */
    MicroUser selectMicroUserById(Long id);

    /**
     * 查询用户列表
     *
     * @param microUser 用户
     * @return 用户集合
     */
    List<MicroUser> selectMicroUserList(MicroUserQueryParam microUser);

    /**
     * 查询用户列表（包含管理员）
     * @param microUser
     * @return
     */
    List<MicroUser> selectMicroAllUserList(MicroUserQueryParam microUser);

    /**
     * 查询当前租户下查询当前租户下指定应用的所有的用户所有的用户（注意：包含已停用的用户）
     *
     * @return
     */
    List<MicroUser> selectAllUser(String appCode); 

    /**
     * 根据用户账号查询角色信息列表
     *
     * @param userId
     * @return
     */
    List<MicroRole> selectMicroRolesByUserId(Long userId);

    /**
     * 新增用户
     *
     * @param microUser 用户
     * @return 结果
     */
    int insertMicroUser(MicroUser microUser);

    /**
     * 持久化不存在的用户相关信息（用户基本信息、用户角色信息、用户与角色关联信息、用户与平台关联信息）
     *
     * @return
     */
    MicroUserCompleteInfo saveIfAbsentMicroUserCompleteInfo(MicroUserCompleteInfo microUserCompleteInfo); 

    /**
     * 新增用户平台关联信息
     *
     * @param microUserPlatformRe
     */
    void saveToUpdateMicroUserPlatformInfo(MicroUserPlatformRe microUserPlatformRe);

    /**
     * 组装用户完整信息
     *
     * @param microProcessBuyInfo
     * @return
     */
    MicroUserCompleteInfo genMicroUserCompleteInfo(MicroProcessBuyInfo microProcessBuyInfo, String appSign); 

    /**
     * 修改用户
     *
     */
    int updateMicroUser(MicroUserEditEntity editEntity); 

    /**
     * 批量删除用户
     *
     * @param ids 需要删除的用户ID
     * @return 结果
     */
    int deleteMicroUserByIds(Long[] ids);

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     * @return 结果
     */
    int deleteMicroUserById(Long id);

    /**
     * 根据用户名或手机号查询用户基本信息
     *
     * @param username
     * @param phoneNum
     * @return
     */
    MicroUser selectUserBaseInfo(String username, String phoneNum);

    /**
     * 根据用户名或手机号查询用户完整信息
     *
     * @param username
     * @param phoneNum
     * @return
     */
    MicroUser findMicroUserCompleteInfo(String username, String phoneNum);

    MicroUserBusinessEntity getBusinessInfo(); 

    /**
     * 刷新用户有效期
     *
     * @param renewalInfo
     */
    void refreshUserValidDate(MicroProcessRenewalInfo renewalInfo);

    /**
     * 刷新用户名
     *
     * @param microUserModifyInfo
     */
    void refreshMicroUserName(MicroUserModifyInfo microUserModifyInfo);

    /**
     * 删除用户
     * @param userId
     */
    boolean removeUser(Long userId); 

    /**
     * 清理redis中用户的token
     */
    boolean clearRedisToken(String username); 

    // decouple-from-ops-platform-cleanup (C.19): 注册时显式写角色关联
    /**
     * 按 (tenantCode, roleCode) 查角色列表
     */
    List<MicroRole> selectMicroRoleList(MicroRole microRole); 

    /**
     * INSERT micro_role（useGeneratedKeys 回填 id）
     */
    int insertMicroRole(MicroRole microRole); 

    /**
     * INSERT micro_user_role_re
     */
    int insertMicroUserRoleRe(MicroUserRoleRe microUserRoleRe); 

}

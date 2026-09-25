/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroSimpleUserInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserBusinessEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserQueryParam;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户Mapper接口
 *
 * @date 2022-10-11
 */
public interface MicroUserMapper {
    /**
     * 查询用户
     *
     * @param id 用户ID
     * @return 用户
     */
    MicroUser selectMicroUserByIdAndAppCode(@Param("id") Long id, @Param("appCode") String appCode); 

    /**
     * 根据用户Id查询角色信息
     *
     * @param userId
     * @return
     */
    List<MicroRole> selectRoleInfoByUserId(Long userId);

    /**
     * 根据用户ID查询用户信息
     * @param id
     * @return
     */
    MicroUser selectMicroUserById(Long id);

    /**
     * 根据手机号或账号查询用户基本信息
     *
     * @param userName
     * @param phoneNumber
     * @return
     */
    MicroUser selectUserInfoByUserNameOrPhoneNumber(@Param("userName") String userName, @Param("phoneNumber") String phoneNumber);

    /**
     * 查询用户列表
     *
     * @param microUser 用户
     * @return 用户集合
     */
    List<MicroUser> selectMicroNotManagerList(MicroUserQueryParam microUser);

    /**
     * 查询用户列表（包含管理员）
     * @param microUser
     * @return
     */
    List<MicroUser> selectMicroList(MicroUserQueryParam microUser);

    /**
     * 新增用户
     *
     * @param microUser 用户
     * @return 结果
     */
    int insertMicroUser(MicroUser microUser);

    /**
     * 修改用户
     *
     * @param microUser 用户
     * @return 结果
     */
    int updateMicroUser(MicroUser microUser);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 结果
     */
    int deleteMicroUserById(Long id);

    /**
     * 批量删除用户
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroUserByIds(Long[] ids);

    /**
     * 根据用户名或手机号查询用户基本信息
     *
     * @param username
     * @param phoneNum
     * @return
     */
    MicroUser selectUserBaseInfo(@Param("username") String username, @Param("phoneNum") String phoneNum);

    /**
     * 根据用户名查询用户完整信息
     *
     * @param username
     * @param phoneNum
     * @param userId
     * @return
     */
    MicroUser selectUserCompleteInfo(@Param("username") String username, @Param("phoneNum") String phoneNum,
                                     @Param("userId") String userId, @Param("appCode") String appCode, @Param("appSign") String appSign,
                                     @Param("tenantCode") String tenantCode);

    MicroUserBusinessEntity getBusinessInfo(); 

    /**
     * 更新用户有效状态
     *
     * @param user
     * @return
     */
    int updateAppRe(@Param("user") MicroUser user);

    /**
     * 根据roleCode获取userName
     *
     * @param roleCode
     * @return
     */
    @MapKey("userId")
    Map<String, MicroSimpleUserInfo> selectMicroUserByRoleCode(@Param("roleCode") String roleCode, @Param("appCode") String appCode); 

    /**
     * 根据角色编码批量查询用户手机号
     * @param roleCodeList
     * @return
     */
    List<String> selectMicroUserPhoneByRoleCodes(List<String> roleCodeList);

    /**
     * 根据角色查询用户基本信息
     * @param roleCode
     * @return
     */
    List<MicroUser> selectMicroUserInfoByRoleCode(String roleCode);

    /**
     * 根据用户名list获取多个用户的信息
     *
     * @param userIdList
     * @return
     */
    List<MicroUser> selectMicroUserListByUserIdList(Set<String> userIdList);

    /**
     * 根据应用编码查询应用下拥有的用户信息列表
     *
     * @param appCode
     * @return
     */
    List<MicroUser> selectMicroUserListByAppCode(String appCode);

    /**
     * 根据角色查询用户的平台openId集合
     *
     * @param roleCodeList
     * @return
     */
    List<String> selectPlatformOpenIdsByRoles(@Param("roleCodeList") List<String> roleCodeList, 
                                              @Param("appCode") String appCode,
                                              @Param("appSign") String appSign,
                                              @Param("platformType") String platformType);

    /**
     * 根据用户ID查询用户的平台openId集合
     * @param userIds
     * @param appSign
     * @param platformType
     * @return
     */
    List<String> selectPlatformOpenIdsByUserIds(@Param("userIds") List<String> userIds, @Param("appSign") String appSign, @Param("platformType") String platformType);

    /**
     * 查询租户下所有用户完整信息
     *
     * @return
     */
    List<MicroUser> selectAllUserCompleteInfo();
}

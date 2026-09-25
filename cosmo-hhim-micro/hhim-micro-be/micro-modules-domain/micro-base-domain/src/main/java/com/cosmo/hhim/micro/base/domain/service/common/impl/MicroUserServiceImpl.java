/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.DataSourceInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessBuyInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessRenewalInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserAppRe;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserQueryParam;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserBusinessEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserCompleteInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserEditEntity;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserModifyInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserPlatformRe;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserRoleRe;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroRoleMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserAppReMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserPlatformReMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserRoleReMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.ActiveFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.cosmo.hhim.micro.infrastructure.events.FlushCacheUserInfoEvent;
import com.cosmo.hhim.micro.infrastructure.events.UserRecommendStatisticsEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户Service业务层处理
 *
 * @date 2022-10-11
 */
@Slf4j
@Service
public class MicroUserServiceImpl implements IMicroUserService {
    @Autowired
    private MicroUserMapper microUserMapper;
    @Autowired
    private MicroUserPlatformReMapper microUserPlatformReMapper;
    @Autowired
    private MicroRoleMapper microRoleMapper;
    @Autowired
    private MicroUserRoleReMapper microUserRoleReMapper;
    @Autowired
    private MicroUserAppReMapper microUserAppReMapper;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ApplicationEventPublisher publisher;

    private static final String OPERATOR = "system-sync";

    /**
     * 查询用户
     *
     * @param id 用户ID
     * @return 用户
     */
    @Override
    public MicroUser selectMicroUserById(Long id) {
        String appCode = remoteQueryAppCode(SecurityUtils.getApplicationSign());
        return microUserMapper.selectMicroUserByIdAndAppCode(id, appCode);
    }

    /**
     * 查询用户列表
     *
     * @param microUser 用户
     * @return 用户
     */
    @Override
    public List<MicroUser> selectMicroUserList(MicroUserQueryParam microUser) {
        String appCode = this.remoteQueryAppCode(SecurityUtils.getApplicationSign());
        if (StringUtils.hasText(appCode)) {
            microUser.setAppCode(appCode);
        }
        List<MicroUser> userList = microUserMapper.selectMicroNotManagerList(microUser);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }
        return userList.stream().filter(u -> !Arrays.asList(u.getRoleCode().split(",")).contains(RoleCodeEnum.MANAGER.getCode())).collect(Collectors.toList());
    }

    /**
     * 查询用户列表（包含管理员）
     * @param microUser
     * @return
     */
    @Override
    public List<MicroUser> selectMicroAllUserList(MicroUserQueryParam microUser) {
        String appCode = this.remoteQueryAppCode(SecurityUtils.getApplicationSign());
        if (StringUtils.hasText(appCode)) {
            microUser.setAppCode(appCode);
        }
        List<MicroUser> userList = microUserMapper.selectMicroList(microUser);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }

        return userList;
    }

    /**
     * 查询当前租户下指定应用的所有的用户（注意：包含已停用的用户）
     *
     * @return
     */
    @Override
    public List<MicroUser> selectAllUser(String appCode) {
        return microUserMapper.selectMicroUserListByAppCode(appCode);
    }

    /**
     * 根据用户账号查询角色信息列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<MicroRole> selectMicroRolesByUserId(Long userId) {
        return microUserMapper.selectRoleInfoByUserId(userId);
    }

    /**
     * 新增用户
     *
     * @param microUser 用户
     * @return 结果
     */
    @Override
    public int insertMicroUser(MicroUser microUser) {
        return microUserMapper.insertMicroUser(microUser);
    }

    // decouple-from-ops-platform-cleanup (C.19): 注册时显式写角色关联 
    @Override
    public List<MicroRole> selectMicroRoleList(MicroRole microRole) {
        return microRoleMapper.selectMicroRoleList(microRole);
    }

    @Override
    public int insertMicroRole(MicroRole microRole) {
        return microRoleMapper.insertMicroRole(microRole);
    }

    @Override
    public int insertMicroUserRoleRe(MicroUserRoleRe microUserRoleRe) {
        return microUserRoleReMapper.insertMicroUserRoleRe(microUserRoleRe);
    }

    /**
     * 持久化不存在的用户相关信息（用户基本信息、用户角色信息、用户与角色关联信息、用户与平台关联信息）
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MicroUserCompleteInfo saveIfAbsentMicroUserCompleteInfo(MicroUserCompleteInfo microUserCompleteInfo) {

        // 用户信息
        Long userId = null;
        List<Long> roleIds = Lists.newArrayList();
        if (null != microUserCompleteInfo.getMicroUser()) {
            MicroUser microUser = microUserCompleteInfo.getMicroUser();
            MicroUser microUserResult = microUserMapper.selectUserInfoByUserNameOrPhoneNumber(microUser.getUserName(), microUser.getPhonenumber());

            if (null == microUserResult) { // 用户本地库不存在 
                microUserMapper.insertMicroUser(microUser);
                userId = microUser.getId();

                // 角色信息列表
                if (!CollectionUtils.isEmpty(microUserCompleteInfo.getMicroRoles())) {
                    for (MicroRole microRole : microUserCompleteInfo.getMicroRoles()) {
                        Long roleId = null;
                        MicroRole microRoleResult = microRoleMapper.selectMicroRoleByRoleCode(microRole.getRoleCode());
                        if (null == microRoleResult) {
                            microRoleMapper.insertMicroRole(microRole);
                            roleId = microRole.getId();
                        } else {
                            roleId = microRoleResult.getId();
                        }

                        if (null != roleId) {
                            roleIds.add(roleId);
                        }
                    }
                }

                // 用户与角色关联信息
                if (null != userId && !CollectionUtils.isEmpty(roleIds)) {
                    for (Long roleId : roleIds) {
                        MicroUserRoleRe microUserRoleReResult = microUserRoleReMapper.selectUserRoleInfoByRoleIdAndUserId(roleId, userId);
                        if (null == microUserRoleReResult) {
                            MicroUserRoleRe microUserRoleRe = new MicroUserRoleRe();
                            microUserRoleRe.setUserId(userId);
                            microUserRoleRe.setRoleId(roleId);
                            microUserRoleReMapper.insertMicroUserRoleRe(microUserRoleRe);
                        }
                    }
                }
            } else {
                userId = microUserResult.getId();
            }
        }

        // 用户与应用关联信息
        if (null != userId && !CollectionUtils.isEmpty(microUserCompleteInfo.getMicroUserAppRes())) {
            for (MicroUserAppRe microUserAppRe : microUserCompleteInfo.getMicroUserAppRes()) {
                MicroUserAppRe userAppRe = microUserAppReMapper.selectMicroUserAppReByAppCodeAndUserId(microUserAppRe.getAppCode(), userId);
                if (null == userAppRe) {
                    microUserAppRe.setUserId(userId);
                    microUserAppReMapper.insertMicroUserAppRe(microUserAppRe);
                }
            }
        }

        // 用户与平台关联信息
        if (null != userId && null != microUserCompleteInfo.getMicroUserPlatformRe()) {
            MicroUserPlatformRe microUserPlatformRe = microUserCompleteInfo.getMicroUserPlatformRe();
            microUserPlatformRe.setUserId(userId);
            saveToUpdateMicroUserPlatformInfo(microUserPlatformRe);
        }

        return microUserCompleteInfo;
    }

    /**
     * 新增用户平台关联信息
     *
     * @param microUserPlatformRe
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveToUpdateMicroUserPlatformInfo(MicroUserPlatformRe microUserPlatformRe) {
        MicroUserPlatformRe queryParam = new MicroUserPlatformRe();
        queryParam.setUserId(microUserPlatformRe.getUserId());
        queryParam.setApplicationSign(microUserPlatformRe.getApplicationSign());
        queryParam.setPlatformType(microUserPlatformRe.getPlatformType());
        queryParam.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
        List<MicroUserPlatformRe> platformReList = microUserPlatformReMapper.selectMicroUserPlatfromReList(queryParam);
        if (CollectionUtils.isEmpty(platformReList)) {
            microUserPlatformReMapper.insertMicroUserPlatfromRe(microUserPlatformRe);
        } else {
            if (!platformReList.get(0).getOpenId().equals(microUserPlatformRe.getOpenId())) { // userId所对应的openId不一致则更新 
                MicroUserPlatformRe updateParam = new MicroUserPlatformRe();
                updateParam.setUserId(microUserPlatformRe.getUserId());
                updateParam.setPlatformType(microUserPlatformRe.getPlatformType());
                updateParam.setApplicationSign(microUserPlatformRe.getApplicationSign());
                updateParam.setOpenId(microUserPlatformRe.getOpenId());
                microUserPlatformReMapper.updateMicroUserPlatfromRe(updateParam);
            }
        }
    }

    /**
     * 组装用户完整信息
     *
     * @param microProcessBuyInfo
     * @return
     */
    @Override
    public MicroUserCompleteInfo genMicroUserCompleteInfo(MicroProcessBuyInfo microProcessBuyInfo, String appSign) {
        MicroUserCompleteInfo userCompleteInfo = new MicroUserCompleteInfo();

        Date curDate = DateUtils.getNowDate();


        MicroProcessBuyInfo.UserInfo userInfo = microProcessBuyInfo.getData();
        DataSourceInfo dataSourceInfo = microProcessBuyInfo.getMqDataSources().get(0);
        String customerCode = dataSourceInfo.getCustomerCode();

        // 用户基本信息
        MicroUser microUser = new MicroUser();
        microUser.setTenantCode(customerCode);
        microUser.setTenantName(userInfo.getCustomerName());
        microUser.setNickName(userInfo.getNickName());
        microUser.setUserName(userInfo.getUserName());
        microUser.setPhonenumber(userInfo.getPhonenumber());
        microUser.setCreatedBy(OPERATOR);
        microUser.setCreatedDate(curDate);
        userCompleteInfo.setMicroUser(microUser);

        // 用户角色信息
        MicroRole microRole = new MicroRole();
        microRole.setTenantCode(customerCode);
        microRole.setRoleCode(RoleCodeEnum.MANAGER.getCode()); // 天云同步过来的用户默认为管理员角色 
        microRole.setRoleName(RoleCodeEnum.MANAGER.getDesc());
        microRole.setStatus(ActiveFlagEnum.NORMAL.getCode());
        microRole.setCreatedBy(OPERATOR);
        microRole.setCreatedDate(curDate);
        userCompleteInfo.setMicroRoles(Arrays.asList(microRole));

        // 用户应用信息
        MicroUserAppRe microUserAppRe = new MicroUserAppRe();
        microUserAppRe.setTenantCode(customerCode);
        microUserAppRe.setUserName(userInfo.getUserName());
        microUserAppRe.setUserStatus(ActiveFlagEnum.NORMAL.getCode());
        microUserAppRe.setValidDate(userInfo.getValidDate());
        microUserAppRe.setAppCode(userInfo.getAppId());
        microUserAppRe.setCreatedBy(OPERATOR);
        microUserAppRe.setCreatedDate(curDate);
        userCompleteInfo.setMicroUserAppRes(Arrays.asList(microUserAppRe));

        // 用户平台信息 bugfix：保证新购后首次管理员登录查询用户完整信息成功
        MicroUserPlatformRe microUserPlatformRe = new MicroUserPlatformRe();
        microUserPlatformRe.setTenantCode(customerCode);
        microUserPlatformRe.setOpenId("xxx");
        microUserPlatformRe.setApplicationSign(appSign);
        microUserPlatformRe.setPlatformType(CommonConstant.DeviceType.WXMINIAPP.getKey());
        microUserPlatformRe.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
        microUserPlatformRe.setCreatedBy(OPERATOR);
        microUserPlatformRe.setCreatedDate(curDate);
        userCompleteInfo.setMicroUserPlatformRe(microUserPlatformRe);

        return userCompleteInfo;
    }

    /**
     * 修改用户
     *
     * @param editEntity 用户
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMicroUser(MicroUserEditEntity editEntity) {
        MicroUserRoleRe re = new MicroUserRoleRe();
        Long editId = editEntity.getId();
        re.setUserId(editId);
        //如果和自身角色一致,则不变,如果变更角色,强制下线,更新用户
        List<MicroRole> roles = microRoleMapper.selectMicroUserRoleRe(re);
        String roleCode;
        boolean managerOrAuditor = false;
        boolean sameRole = false;
        for (MicroRole r : roles) {
            roleCode = r.getRoleCode();
            if (roleCode.equals(editEntity.getRoleCode())) {
                sameRole = true;
            }
        }
        String operatorUserId = String.valueOf(SecurityUtils.getUserId());
        String applicationSign = SecurityUtils.getApplicationSign();
        String appCode = this.remoteQueryAppCode(applicationSign);
        String opTenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        MicroUser operatorCache = microUserMapper.selectUserCompleteInfo(null, null, operatorUserId, appCode, applicationSign, opTenantCode);
        if (operatorCache == null || CollectionUtils.isEmpty(operatorCache.getMicroRoles())) {
            throw new CustomException("操作人用户信息不存在，请重新登录");
        }
        List<MicroRole> operatorRoles = operatorCache.getMicroRoles();
        for (MicroRole role : operatorRoles) {
            roleCode = role.getRoleCode();
            if (roleCode.equals(RoleCodeEnum.MANAGER.getCode()) || roleCode.equals(RoleCodeEnum.AUDITOR.getCode())) {
                managerOrAuditor = true;
            }
        }
        // 如果role!=null 说明 原有角色和当前角色一致
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        boolean forceLogout = false;
        String operatorId = String.valueOf(SecurityUtils.getUserId());
        if (managerOrAuditor) {
            if (CollectionUtils.isEmpty(roles) || !sameRole) {
                MicroRole existRole = microRoleMapper.selectMicroRoleByRoleCode(editEntity.getRoleCode());
                MicroRole microRole = new MicroRole();
                if (existRole == null) {
                    microRole.setTenantCode(tenantCode);
                    microRole.setRoleCode(editEntity.getRoleCode());
                    microRole.setRoleName(RoleCodeEnum.getEnum(editEntity.getRoleCode()).getDesc());
                    microRole.setStatus(ActiveFlagEnum.NORMAL.getCode());
                    microRole.setCreatedBy(operatorId);
                    microRole.setLastUpdBy(operatorId);
                    //插入
                    microRoleMapper.insertMicroRole(microRole);
                } else {
                    microRole.setId(existRole.getId());
                }
                microUserRoleReMapper.deleteMicroUserRoleReByUser(editId, tenantCode);
                MicroUserRoleRe roleRe = new MicroUserRoleRe();
                roleRe.setTenantCode(tenantCode);
                roleRe.setRoleId(microRole.getId());
                roleRe.setUserId(editId);
                microUserRoleReMapper.insertMicroUserRoleRe(roleRe);
                forceLogout = true;
            }
        }
        MicroUser user = new MicroUser();
        String modifiedUser = editEntity.getUserName();
        editEntity.setUserName(null);
        BeanUtils.copyProperties(editEntity, user);
        int i = microUserMapper.updateMicroUser(user);
        // 更新用户有效状态
        if (managerOrAuditor) {
            //如果修改的用户状态不为空&&被修改的人不能是操作人自己
            if (!StringUtils.isEmpty(user.getStatus()) && !String.valueOf(editId).equals(operatorId)) {
                MicroUser verifyUser = microUserMapper.selectMicroUserByIdAndAppCode(editId, appCode);
                if (!verifyUser.getStatus().equals(user.getStatus())) {
                    forceLogout = true;
                    microUserMapper.updateAppRe(user);
                }
            }
            if (forceLogout) {
                forceLogout(modifiedUser);
            }
            // 推送统计时间周期刷新事件
            publisher.publishEvent(new UserRecommendStatisticsEvent(editId, tenantCode));
        }
        publisher.publishEvent(new FlushCacheUserInfoEvent(tenantCode));
        return i;
    }

    /**
     * 强行登出
     *
     * @param modifiedUser
     */
    private void forceLogout(String modifiedUser) {
        String redisKey = Constants.LOGIN_MICRO_TOKEN_KEY + modifiedUser;
        Set<String> match = redisCache.match(redisKey);
        match.forEach(m -> redisCache.deleteObject(m));
    }

    /**
     * 批量删除用户
     *
     * @param ids 需要删除的用户ID
     * @return 结果
     */
    @Override
    public int deleteMicroUserByIds(Long[] ids) {
        return microUserMapper.deleteMicroUserByIds(ids);
    }

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     * @return 结果
     */
    @Override
    public int deleteMicroUserById(Long id) {
        return microUserMapper.deleteMicroUserById(id);
    }

    /**
     * 根据用户名或手机号查询用户基本信息
     *
     * @param username
     * @param phoneNum
     * @return
     */
    @Override
    public MicroUser selectUserBaseInfo(String username, String phoneNum) {
        return microUserMapper.selectUserBaseInfo(username, phoneNum);
    }

    /**
     * 根据用户名或手机号查询用户完整信息
     *
     * @param username
     * @return
     */
    @Override
    public MicroUser findMicroUserCompleteInfo(String username, String phoneNum) {
        if (StringUtils.hasText(username)) { // 优先使用username查询，不存在则使用phoneNum
            phoneNum = null;
        }
        String appSign = SecurityUtils.getApplicationSign();
        String appCode = this.remoteQueryAppCode(appSign);
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        return microUserMapper.selectUserCompleteInfo(username, phoneNum, null, appCode, appSign, tenantCode);
    }

    @Override
    public MicroUserBusinessEntity getBusinessInfo() {
        return microUserMapper.getBusinessInfo();
    }

    /**
     * 刷新用户有效期
     *
     * @param renewalInfo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshUserValidDate(MicroProcessRenewalInfo renewalInfo) {
        Date validDate = new Date(renewalInfo.getCustomerValidDate());

        // 1.刷新DB中的用户有效期
        List<MicroUser> microUsers = microUserMapper.selectMicroUserListByAppCode(renewalInfo.getAppid());
        if (!CollectionUtils.isEmpty(microUsers)) {
            for (MicroUser user : microUsers) {
                // 更新用户信息表
                MicroUser microUser = new MicroUser();
                microUser.setId(user.getId());
                microUser.setValidDate(validDate);
                microUserMapper.updateMicroUser(microUser);
            }
        }

        // 更新用户应用关联表
        MicroUserAppRe microUserAppRe = new MicroUserAppRe();
        microUserAppRe.setAppCode(renewalInfo.getAppid());
        microUserAppRe.setValidDate(validDate);
        microUserAppReMapper.updateMicroUserAppRe(microUserAppRe);

        log.info("刷新DB---用户有效期完成！");

        // 查询应用标识
        String applicationSign = remoteQueryAppSign(renewalInfo.getAppid());

        // 2.刷新redis token中的用户有效期
        if (!CollectionUtils.isEmpty(microUsers)) {
            List<String> userNameList = microUsers.stream().map(MicroUser::getUserName).collect(Collectors.toList());
            for (String userName : userNameList) {
                String matchKey = Constants.LOGIN_MICRO_TOKEN_KEY + userName + ":" + applicationSign;
                Set<String> matchKeys = redisCache.scan(matchKey);
                for (String key : matchKeys) {
                    MicroUserCompleteInfo microUserCompleteInfo = redisCache.getCacheObject(key);
                    Long expireTime = redisCache.getKeyTTL(key, TimeUnit.SECONDS);

                    boolean matchFlag = false;
                    for (MicroUserAppRe userAppRe : microUserCompleteInfo.getMicroUserAppRes()) {
                        if (userAppRe.getAppCode().equals(renewalInfo.getAppid())) {
                            matchFlag = true;
                            userAppRe.setValidDate(validDate);
                        }
                    }

                    if (matchFlag) {
                        redisCache.setCacheObject(key, microUserCompleteInfo, expireTime.intValue(), TimeUnit.SECONDS);
                        log.info("刷新redis token---中的用户有效期完成！---> key：{}", key);
                    }
                }
            }
        }
    }

    /**
     * 刷新用户名
     *
     * @param microUserModifyInfo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshMicroUserName(MicroUserModifyInfo microUserModifyInfo) {
        String newUserName = microUserModifyInfo.getNewName();

        // 1.刷新DB中的用户名
        MicroUser microUser = microUserMapper.selectUserInfoByUserNameOrPhoneNumber(null, microUserModifyInfo.getPhonenumber());
        if (null != microUser && null != microUser.getId()) {
            // micro_user
            MicroUser updateMicroUser = new MicroUser();
            updateMicroUser.setUserName(newUserName);
            updateMicroUser.setId(microUser.getId());
            microUserMapper.updateMicroUser(updateMicroUser);

            // micro_user_app_re
            MicroUserAppRe microUserAppRe = new MicroUserAppRe();
            microUserAppRe.setUserId(microUser.getId());
            microUserAppRe.setUserName(newUserName);
            microUserAppReMapper.updateMicroUserAppReByUserId(microUserAppRe);
        }
        log.info("刷新DB---用户名刷新完成！");

        // 2.刷新redis token中的用户名
        if (null != microUser && StringUtils.hasText(microUser.getUserName())) {
            String matchKey = Constants.LOGIN_MICRO_TOKEN_KEY + microUser.getUserName();
            Set<String> matchKeys = redisCache.scan(matchKey);
            for (String key : matchKeys) {
                MicroUserCompleteInfo microUserCompleteInfo = redisCache.getCacheObject(key);
                Long expireTime = redisCache.getKeyTTL(key, TimeUnit.SECONDS);

                // 更新username
                microUserCompleteInfo.getMicroUser().setUserName(newUserName);

                // 修改key中的username
                String newKey = key.replaceFirst(microUser.getUserName(), newUserName);
                redisCache.setCacheObject(newKey, microUserCompleteInfo, expireTime.intValue(), TimeUnit.SECONDS);

                // 删除原先的token key
                redisCache.deleteObject(key);

                log.info("刷新redis token---中的用户名完成！---> oldKey：{}, newKey:{}", key, newKey);
            }
        }

    }

    /**
     * 删除用户
     *
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeUser(Long userId) {

        // 0.管理员不允许退出租户
        List<MicroRole> microRoles = microUserMapper.selectRoleInfoByUserId(userId);
        if (!CollectionUtils.isEmpty(microRoles)) {
            long count = microRoles.stream().filter(e -> RoleCodeEnum.MANAGER.getCode().equals(e.getRoleCode())).count();
            if (count > 0) {
                log.error("企业主不允许退出租户！userId:{}", userId);
                throw new CustomException("企业主不可退出租户");
            }
        }

        MicroUser microUser = microUserMapper.selectMicroUserById(userId);
        if (null == microUser) {
            throw new CustomException("用户不存在！");
        }
        String userName = microUser.getUserName();
        String tenantCode = microUser.getTenantCode();
        // 校验被删用户归属当前租户，防止越权删除其他租户的用户
        String currentTenant = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        if (currentTenant != null && !tenantCode.equals(currentTenant)) {
            throw new CustomException("无权删除其他租户的用户");
        }

        // 1.删除微应用库中的用户信息
        microUserMapper.deleteMicroUserById(userId);
        microUserAppReMapper.deleteMicroUserAppReByUserId(userId);
        microUserPlatformReMapper.deleteMicroUserPlatfromReByUserId(userId);
        microUserRoleReMapper.deleteMicroUserRoleReByUserId(userId);


        // 2.清理redis缓存信息
        // 2.1 清理用户缓存
        redisCache.deleteObject(CommonConstants.USER_CACHE_INFO + SecurityUtils.getApplicationSign() + ":" + tenantCode + ":" + userId);

        // 2.2 清理用户token
        String matchKey = Constants.LOGIN_MICRO_TOKEN_KEY + userName;
        Set<String> matchKeys = redisCache.scan(matchKey);
        for (String key : matchKeys) {
            redisCache.deleteObject(key);
            log.info("删除redis token完成！---> key:{}", key);
        }

        return true;
    }

    /**
     * 清理redis中用户的token
     */
    @Override
    public boolean clearRedisToken(String username) {
        if (StringUtils.hasText(username)) {
            String matchKey = Constants.LOGIN_MICRO_TOKEN_KEY + username;
            Set<String> matchKeys = redisCache.scan(matchKey);
            for (String key : matchKeys) {
                // 删除原先的token key
                redisCache.deleteObject(key);

                log.info("删除redis token完成！---> key:{}", key);
            }
        }
        return true;
    }

    /**
     * 调用三方平台根据应用标识查询应用编码
     *
     * @param appSign
     * @return
     */
    public String remoteQueryAppCode(String appSign) {
        Map<String, Object> cacheMap = redisCache.getCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY);
        return CollectionUtils.isEmpty(cacheMap) ? "" : (String) cacheMap.get(appSign);
    }

    /**
     * 调用三方平台根据应用编码查询应用标识
     *
     * @param appCode
     * @return
     */
    private String remoteQueryAppSign(String appCode) {
        Map<String, String> cacheMap = redisCache.getCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY);
        String appSign = cacheMap.entrySet().stream()
                .filter(entry -> appCode.equals(entry.getValue()))
                .findFirst().map(Map.Entry::getKey)
                .orElse(null);
        if (StringUtils.isEmpty(appSign)) {
            log.error("无法获取应用配置信息!appCode is {},cacheMap is {}", appCode, cacheMap);
            throw new CustomException("无法获取应用配置信息！");
        }
        return appSign;
    }

}

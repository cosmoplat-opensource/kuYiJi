/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzWechatConfigMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzWechatUserBindMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzWechatUserMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzWechatUserService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 微信用户信息Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Service
@Slf4j
public class HyzzWechatUserServiceImpl implements IHyzzWechatUserService {
    @Autowired
    private HyzzWechatUserMapper hyzzWechatUserMapper;

    @Autowired
    private HyzzWechatUserBindMapper hyzzWechatUserBindMapper;

    @Autowired
    private HyzzWechatConfigMapper hyzzWechatConfigMapper;

    /**
     * 添加微信用户信息
     *
     * @param paramList
     * @return
     */
    @Transactional
    @Override
    public APIResponse<Boolean> batchAddWechatUserInfo(List<WechatUserInfoParam> paramList) {
        if(CollectionUtils.isEmpty(paramList)){
            return APIResponse.success(false);
        }
        for(WechatUserInfoParam param : paramList){
            addWechatUserInfo(param);
        }
        return APIResponse.success(true);
    }

    /**
     * 添加微信用户信息
     *
     * @param param
     * @return
     */
    @Transactional
    @Override
    public APIResponse<Boolean> addWechatUserInfo(WechatUserInfoParam param) {
        // 微信用户不存在新增，存在更新
        HyzzWechatUser hyzzWechatUser = hyzzWechatUserMapper.selectHyzzWechatUserByOpenId(param.getOpenid());
        HyzzWechatUser wechatUser = new HyzzWechatUser();
        BeanUtils.copyProperties(param, wechatUser);
        if (null == hyzzWechatUser) {
            wechatUser.setCreateTime(DateUtils.getNowDate());
            hyzzWechatUserMapper.insertHyzzWechatUser(wechatUser);
        } else {
            wechatUser.setUpdateTime(DateUtils.getNowDate());
            hyzzWechatUserMapper.updateHyzzWechatUser(wechatUser);
        }

        // 微信用户与账号绑定信息不存在则新增，存在更新
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }

        HyzzWechatUserBind hyzzWechatUserBind = hyzzWechatUserBindMapper
                .selectUserBindInfo(param.getOpenid(), param.getUserName(), param.getUserIdentity(), tenantCode);
        HyzzWechatUserBind wechatUserBind = new HyzzWechatUserBind();
        BeanUtils.copyProperties(param, wechatUserBind);
        wechatUserBind.setTenantCode(tenantCode);
        if (null == hyzzWechatUserBind) {
            wechatUserBind.setCreateTime(DateUtils.getNowDate());
            hyzzWechatUserBindMapper.insertHyzzWechatUserBind(wechatUserBind);
        } else {
            WechatUserBindUpdateParam updateParam = new WechatUserBindUpdateParam();
            BeanUtils.copyProperties(wechatUserBind, updateParam);
            updateParam.setUpdateTime(DateUtils.getNowDate());
            updateParam.setTenantCodeCondition(tenantCode);
            updateParam.setOpenidCondition(param.getOpenid());
            updateParam.setUserNameCondition(param.getUserName());
            updateParam.setUserIdentityCondition(param.getUserIdentity());
            hyzzWechatUserBindMapper.updateHyzzWechatUserBind(updateParam);
        }

        return APIResponse.success(true);
    }

    /**
     * 根据openid查询微信用户以及所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @Override
    public APIResponse<WechatUserInfoResult> queryWechatUserInfoByOpenId(String openid) {
        return APIResponse.success(hyzzWechatUserMapper.selectWechatUserDetailInfoByOpenId(openid));
    }

    /**
     * 根据openid和租户编码查询所绑定的用户信息
     *
     * @param openid
     * @return
     */
    @Override
    public APIResponse<List<HyzzWechatUserBind>> queryWxBindUserByOpenIdAndTenant(String openid) {
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }
        return APIResponse.success(hyzzWechatUserBindMapper.selectBindUsersByOpenidAndTenant(openid, tenantCode));
    }

    /**
     * 根据openid查询所绑定的用户信息
     *
     * @param openid
     * @return
     */
    @Override
    public APIResponse<List<HyzzWechatUserBind>> queryWxBindUserByOpenId(String openid) {
        return APIResponse.success(hyzzWechatUserBindMapper.selectBindUsersByOpenid(openid));
    }

    /**
     * 根据openid查询微信用户基本信息
     *
     * @param openid
     * @return
     */
    @Override
    public APIResponse<HyzzWechatUser> queryWxUserBasicInfoByOpenId(String openid) {
        return APIResponse.success(hyzzWechatUserMapper.selectHyzzWechatUserByOpenId(openid));
    }

    /**
     * 根据openid + username + userIdentify + tenantCode查询所绑定的用户信息
     *
     * @return
     */
    @Override
    public APIResponse<HyzzWechatUserBind> queryWechatUserBindInfo(WechatUserDetailInfoParam param) {
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }
        String openid = param.getOpenid();
        String username = param.getUsername();
        String userIdentity = param.getUserIdentity();
        return APIResponse.success(hyzzWechatUserBindMapper.selectUserBindInfo(openid, username, userIdentity, tenantCode));
    }

    /**
     * 根据用户账号查询所绑定的微信信息
     *
     * @param usernames
     * @return
     */
    @Override
    public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByUsernames(List<String> usernames) {
        List<HyzzWechatUserBind> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(usernames)) {
            return APIResponse.success(result);
        }
        for (String username : usernames) {
            String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
            if (!StringUtils.hasText(tenantCode)) {
                throw new CustomException("租户信息获取失败!");
            }
            List<HyzzWechatUserBind> hyzzWechatUserBinds = hyzzWechatUserBindMapper.selectHyzzWechatUserBindsByUsername(username, tenantCode);
            if (!CollectionUtils.isEmpty(hyzzWechatUserBinds)) {
                result.addAll(hyzzWechatUserBinds);
            }
        }

        return APIResponse.success(result);
    }

    /**
     * 根据用户账号查询所绑定的微信信息（所有租户下查询）
     * @param usernames
     * @return
     */
    @Override
    public APIResponse<List<HyzzWechatUserBind>> queryAllTenantWechatUserBindInfoByUsernames(List<String> usernames) {
        List<HyzzWechatUserBind> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(usernames)) {
            return APIResponse.success(result);
        }
        for (String username : usernames) {
            List<HyzzWechatUserBind> hyzzWechatUserBinds = hyzzWechatUserBindMapper.selectWechatUserBindsByUsername(username);
            if (!CollectionUtils.isEmpty(hyzzWechatUserBinds)) {
                result.addAll(hyzzWechatUserBinds);
            }
        }

        return APIResponse.success(result);
    }


    /**
     * 根据用户账号 + 身份查询所绑定的微信信息
     *
     * @param username
     * @param userIdentity
     * @return
     */
    @Override
    public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByCondition(String username, String userIdentity) {
        List<HyzzWechatUserBind> result = Lists.newArrayList();
        if(!StringUtils.hasText(username) || !StringUtils.hasText(userIdentity)){
            return APIResponse.success(result);
        }

        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }

        List<HyzzWechatUserBind> hyzzWechatUserBinds = hyzzWechatUserBindMapper.selectHyzzWechatUserBindsByCondition(username, userIdentity, tenantCode);
        if (!CollectionUtils.isEmpty(hyzzWechatUserBinds)) {
            result.addAll(hyzzWechatUserBinds);
        }

        return APIResponse.success(result);
    }

    /**
     * 根据openId和账户信息解绑微信和账号绑定关系
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<Boolean> unbindWechatUser(WechatUnbindParam param) {
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }
        hyzzWechatUserBindMapper.deleteHyzzWechatUserBind(param, tenantCode);
        return APIResponse.success(true);
    }

    /**
     * 根据openId和账户信息以及租户对微信账号信息解绑
     * @param param
     * @return
     */
    @Override
    public APIResponse<Boolean> unbindTenantWechatUser(WechatUnbindTenantParam param) {
        hyzzWechatUserBindMapper.deleteHyzzWechatUserBind(param, param.getCustomerCode());
        return APIResponse.success(true);
    }

    /**
     * 根据指定账号的微信绑定信息
     *
     * @param username
     * @return
     */
    @Override
    public APIResponse<Boolean> unbindWechatUserByUserName(String username) {
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }
        hyzzWechatUserBindMapper.deleteHyzzWechatUserBindByUsername(username, tenantCode);
        return APIResponse.success(true);
    }

    /**
     * 更新最新登录的时间（返回上次登录的时间）
     *
     * @return
     */
    @Override
    public APIResponse<HyzzWechatUser> updateLoginTime(String openid) {
        HyzzWechatUser hyzzWechatUser = hyzzWechatUserMapper.selectHyzzWechatUserByOpenId(openid);
        HyzzWechatUser wechatUser = new HyzzWechatUser();
        wechatUser.setLastLoginTime(DateUtils.getNowDate());
        wechatUser.setOpenid(openid);
        hyzzWechatUserMapper.updateHyzzWechatUser(wechatUser);
        return APIResponse.success(hyzzWechatUser);
    }

    /**
     * 更新微信用户绑定信息
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<Boolean> updateWechatUserBindInfo(WechatUserBindUpdateParam param) {
        String tenantCode = (String) ThreadLocalCache.getCache(Constant.TENANT_CODE);
        if (!StringUtils.hasText(tenantCode)) {
            throw new CustomException("租户信息获取失败!");
        }
        param.setTenantCodeCondition(tenantCode);
        param.setUpdateTime(DateUtils.getNowDate());
        int i = hyzzWechatUserBindMapper.updateHyzzWechatUserBind(param);
        return APIResponse.success(i > 0);
    }

    /**
     * 根据微信公众号AppId查询租户信息
     * @param appId
     * @return
     */
    @Override
    public APIResponse<String> getTenantByAppId(String appId) {
        HyzzWechatConfig hyzzWechatConfig = hyzzWechatConfigMapper.selectHyzzWechatConfigByAppId(appId);
        if(null == hyzzWechatConfig){
            return null;
        }
        return APIResponse.success(hyzzWechatConfig.getCustomerCode());
    }

    /**
     * 更新微信用户及绑定关系openId
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public APIResponse<Boolean> updateWechatUserOpenId(WechatUserOpenIdUpdateParam param) {
        //校验参数
        if(param == null || StringUtils.isEmpty(param.getOldOpenId()) || StringUtils.isEmpty(param.getNewOpenId())){
            log.warn("请求参数:{},用户旧openid和新openid不能为空", JSON.toJSONString(param));
            throw new CustomException("用户旧openid和新openid以及用户账号不能为空");
        }
        //更新微信用户信息openId
        hyzzWechatUserMapper.updateHyzzWechatUserOpenId(param);
        //更新微信用户绑定关系openId(此处只根据oldOpenId更新 因司机身份和平台身份得userName不一样 需统一进行更新 所以筛选条件不能加userName)
        hyzzWechatUserBindMapper.updateWechatUserBindOpenId(param);
        return APIResponse.success(true);
    }

    @Override
    public APIResponse<List<HyzzWechatUserBind>> selectHyzzWechatUserBindList(HyzzWechatUserBind hyzzWechatUserBind) {
        List<HyzzWechatUserBind> result = Lists.newArrayList();
        List<HyzzWechatUserBind> hyzzWechatUserBinds = hyzzWechatUserBindMapper.selectHyzzWechatUserBindList(hyzzWechatUserBind);
        if (!CollectionUtils.isEmpty(hyzzWechatUserBinds)) {
            result.addAll(hyzzWechatUserBinds);
        }
        return APIResponse.success(result);
    }


}

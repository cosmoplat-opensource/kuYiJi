/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzWechatUserBind;
import com.cosmo.hhim.thirdplat.api.operation.domain.WechatUnbindParam;
import com.cosmo.hhim.thirdplat.api.operation.domain.WechatUserBindUpdateParam;
import com.cosmo.hhim.thirdplat.api.operation.domain.WechatUserOpenIdUpdateParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 微信与企业用户绑定信息Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface HyzzWechatUserBindMapper {

    /**
     * openid + username + userIdentity + tenantCode确定微信用户绑定信息
     *
     * @param openid
     * @param username
     * @param userIdentity
     * @param tenantCode
     * @return
     */
    HyzzWechatUserBind selectUserBindInfo(@Param("openid") String openid, @Param("username") String username,
                                          @Param("userIdentity") String userIdentity, @Param("tenantCode") String tenantCode);

    /**
     * 根据openid和租户编码查询所绑定的用户信息列表
     *
     * @param openid
     * @param tenantCode
     * @return
     */
    List<HyzzWechatUserBind> selectBindUsersByOpenidAndTenant(@Param("openid") String openid, @Param("tenantCode") String tenantCode);


    /**
     * 根据openid查询所绑定的用户信息列表
     *
     * @param openid
     * @return
     */
    List<HyzzWechatUserBind> selectBindUsersByOpenid(String openid);

    /**
     * 根据username和租户编码查询所绑定的用户信息列表
     *
     * @param username
     * @param tenantCode
     * @return
     */
    List<HyzzWechatUserBind> selectHyzzWechatUserBindsByUsername(@Param("username") String username, @Param("tenantCode") String tenantCode);

    /**
     * 根据username查询所绑定的用户信息列表
     * @param username
     * @return
     */
    List<HyzzWechatUserBind> selectWechatUserBindsByUsername(String username);

    /**
     * 根据账号 + 身份查询所绑定的用户信息列表
     *
     * @param username
     * @param tenantCode
     * @return
     */
    List<HyzzWechatUserBind> selectHyzzWechatUserBindsByCondition(@Param("username") String username,
                                                                  @Param("userIdentity") String userIdentity,
                                                                  @Param("tenantCode") String tenantCode);

    /**
     * 查询微信与企业用户绑定信息列表
     *
     * @param hyzzWechatUserBind 微信与企业用户绑定信息
     * @return 微信与企业用户绑定信息集合
     */
    public List<HyzzWechatUserBind> selectHyzzWechatUserBindList(HyzzWechatUserBind hyzzWechatUserBind);

    /**
     * 新增微信与企业用户绑定信息
     *
     * @param hyzzWechatUserBind 微信与企业用户绑定信息
     * @return 结果
     */
    public int insertHyzzWechatUserBind(HyzzWechatUserBind hyzzWechatUserBind);

    /**
     * 修改微信与企业用户绑定信息
     *
     * @param param 微信与企业用户绑定信息
     * @return 结果
     */
    int updateHyzzWechatUserBind(WechatUserBindUpdateParam param);

    /**
     * 删除微信与企业用户绑定信息
     *
     * @return
     */
    int deleteHyzzWechatUserBind(@Param("param") WechatUnbindParam param, @Param("tenantCode") String tenantCode);

    /**
     * 删除指定账号的微信与企业用户的绑定信息
     *
     * @param username
     * @param tenantCode
     * @return
     */
    int deleteHyzzWechatUserBindByUsername(@Param("username") String username, @Param("tenantCode") String tenantCode);

    /**
     * 更新微信用户绑定关系openId
     * @param param
     * @return
     */
    int updateWechatUserBindOpenId(WechatUserOpenIdUpdateParam param);
}

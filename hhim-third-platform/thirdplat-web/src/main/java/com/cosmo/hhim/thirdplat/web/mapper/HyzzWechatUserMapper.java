/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzWechatUser;
import com.cosmo.hhim.thirdplat.api.operation.domain.WechatUserInfoResult;
import com.cosmo.hhim.thirdplat.api.operation.domain.WechatUserOpenIdUpdateParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 微信用户信息Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface HyzzWechatUserMapper {
    /**
     * 查询微信用户信息
     *
     * @param openid 微信用户信息ID
     * @return 微信用户信息
     */
    HyzzWechatUser selectHyzzWechatUserByOpenId(String openid);

    /**
     * 根据openid查询微信用户以及所绑定的账户信息列表
     * @param openid
     * @return
     */
    WechatUserInfoResult selectWechatUserDetailInfoByOpenId(String openid);

    /**
     * 查询微信用户信息列表
     *
     * @param hyzzWechatUser 微信用户信息
     * @return 微信用户信息集合
     */
    public List<HyzzWechatUser> selectHyzzWechatUserList(HyzzWechatUser hyzzWechatUser);

    /**
     * 新增微信用户信息
     *
     * @param hyzzWechatUser 微信用户信息
     * @return 结果
     */
    public int insertHyzzWechatUser(HyzzWechatUser hyzzWechatUser);

    /**
     * 修改微信用户信息
     *
     * @param hyzzWechatUser 微信用户信息
     * @return 结果
     */
    int updateHyzzWechatUser(HyzzWechatUser hyzzWechatUser);

    /**
     * 修改微信用户openId
     *
     * @param param
     * @return 结果
     */
    int updateHyzzWechatUserOpenId(WechatUserOpenIdUpdateParam param);

    /**
     * 删除微信用户信息
     *
     * @param id 微信用户信息ID
     * @return 结果
     */
    public int deleteHyzzWechatUserById(Long id);

    /**
     * 批量删除微信用户信息
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteHyzzWechatUserByIds(Long[] ids);
}

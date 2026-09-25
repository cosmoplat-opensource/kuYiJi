/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 微信用户信息对象 hyzz_wechat_user
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzWechatUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 用户的标识，对当前公众号唯一
     */
    private String openid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户的性别（1：男性，2：女性，0：未知）
     */
    private Integer sex;

    /**
     * 普通用户个人资料填写的城市
     */
    private String city;

    /**
     * 用户个人资料填写的省份
     */
    private String province;

    /**
     * 国家，如中国为CN
     */
    private String country;

    /**
     * 用户头像地址
     */
    private String headImgUrl;

    /**
     * 用户是否订阅该公众号标识(0：此用户没有关注该公众号拉取不到其余信息，1：已关注可正常拉取用户信息)
     */
    private String subscribe;

    /**
     * 用户的语言，简体中文为zh_CN
     */
    private String language;

    /**
     * 用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date subscribeTime;

    /**
     * 只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。
     */
    private String unionid;

    /**
     * 用户所在的分组ID（暂时兼容用户分组旧接口）
     */
    private String groupid;

    /**
     * 用户被打上的标签ID列表
     */
    private String tagidList;

    /**
     * 返回用户关注的渠道来源，ADD_SCENE_SEARCH 公众号搜索，ADD_SCENE_ACCOUNT_MIGRATION 公众号迁移，ADD_SCENE_PROFILE_CARD 名片分享，ADD_SCENE_QR_CODE 扫描二维码，ADD_SCENE_PROFILE_LINK 图文页内名称点击，ADD_SCENE_PROFILE_ITEM 图文页右上角菜单，ADD_SCENE_PAID 支付后关注，ADD_SCENE_WECHAT_ADVERTISEMENT 微信广告，ADD_SCENE_REPRINT 他人转载 ，ADD_SCENE_LIVESTREAM 视频号直播， ADD_SCENE_CHANNELS 视频号, ADD_SCENE_OTHERS 其他
     */
    private String subscribeScene;

    /**
     * 二维码扫码场景（开发者自定义）
     */
    private String qrScene;

    /**
     * 二维码扫码场景描述（开发者自定义）
     */
    private String qrSceneStr;

    /**
     * 上次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 上次登录IP
     */
    private String lastLoginIp;

}

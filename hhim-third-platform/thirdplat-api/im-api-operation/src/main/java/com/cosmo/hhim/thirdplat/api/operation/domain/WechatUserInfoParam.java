/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-07
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatUserInfoParam {

    /**
     * 用户的标识，对当前公众号唯一
     */
    @NotBlank(message = "用户微信唯一标识不允许为空！")
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
     * 用户账号
     */
    @NotBlank(message = "用户账号不允许为空！")
    private String userName;

    /**
     * 用户身份(0：司机，1：供应商，2：仓管员，3：TE货代)
     */
    @NotBlank(message = "用户身份不允许为空！")
    private String userIdentity;

    /**
     * 司机名
     */
    private String driverName;

}

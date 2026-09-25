/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 微信小程序配置对象 hyzz_micro_miniapp_config
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-20
 */
@Data
public class HyzzMicroMiniappConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 小程序AppId
     */
    private String appId;

    /**
     * 小程序AppSecret
     */
    private String appSecret;

    /**
     * 小程序名称
     */
    private String miniAppName;

    /**
     * 应用标识（micro_process:在制品库存-微应用）
     */
    private String applicationSign;

    /**
     * 平台类型（wechat：微信，cosmo：卡奥斯）
     */
    private String platformType;

    /**
     * 是否删除 0-正常，1-删除
     */
    private String deleted;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;


}

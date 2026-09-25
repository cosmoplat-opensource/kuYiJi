/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 微信公众号配置信息对象 hyzz_wechat_config
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzWechatConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 公众号AppId
     */
    private String appId;

    /**
     * 公众号密钥
     */
    private String appSecret;

    /**
     * 公众号名称
     */
    private String appName;

    /**
     * 公众号类型
     */
    private String appType;

    /**
     * 主体信息
     */
    private String mainInfo;

    // 微信服务器接入认证token
    private String serverToken;

    // 微信服务器接入消息密钥
    private String serverEncodeAeskey;

    /**
     * 所属企业编码
     */
    private String customerCode;

    /**
     * 搜索值
     */
    private String searchValue;

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


    /**
     * 开始日期
     */
    private Date beginDate;

    /**
     * 结束日期
     */
    private Date endDate;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

}

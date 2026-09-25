/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 用户查询入参（避免将 MicroUser 实体直接作为查询绑定对象，防止敏感字段被直接绑定/篡改）
 */
@Data
public class MicroUserQueryParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 更新者
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    private Date lastUpdDate;

    /**
     * 关键字（模糊匹配用户名/昵称）
     */
    private String key;

    /**
     * 应用编码（服务层注入，用于按应用过滤）
     */
    private String appCode;
}

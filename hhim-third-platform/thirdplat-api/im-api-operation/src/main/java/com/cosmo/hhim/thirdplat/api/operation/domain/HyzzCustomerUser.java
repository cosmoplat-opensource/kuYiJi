/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * 客户用户信息对象 hyzz_customer_user
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzCustomerUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 客户编号
     */
    private String customerCode;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户类型（20企业用户）
     */
    private String userType;

    /**
     * 账号类型（1-企业管理员；2普通账号）
     */
    private String accountType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 密码
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JSONField(serialize = false)
    private transient String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    @JSONField(format = "yyyy-MM-dd")
    private Date loginDate;

    private String roleIds;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 主账号标识（0否 ，1是）
     */
    private String mainAccountFlag;

    /**
     * 账号有效期
     */
    private Date validDate;

    private int isAuthentication;

    private Long uucUserId;

    private boolean uucActiveUpdateFlag = false;

    private String newUsername;

    private String playUser;
}

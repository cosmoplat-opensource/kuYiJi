/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserAppRe;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserPlatformRe;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户对象 micro_user
 *
 * @date 2022-10-11
 */
@Data
public class MicroUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 账号
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
    private String roleCode;
    private String roleName;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;
    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phonenumber;

    /**
     * 账号有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date validDate;

    /**
     * 账号状态(0正常 1停用)
     */
    private String status;

    private String remark;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;
    /**
     * 搜索key
     */
    private String key;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

    // 用户角色信息 
    private List<MicroRole> microRoles;

    // 用户与平台关联信息 
    private MicroUserPlatformRe microUserPlatformRe;

    // 用户应用关联信息 
    private List<MicroUserAppRe> microUserAppRes;

    // 应用编码 
    private String appCode;

    /**
     * 是否进行个性化推荐
     * 关闭 0
     * 开启 1
     * 展示个性化推荐弹窗 2
     * 不展示个性化推荐弹窗 3
     */
    private Integer personalRecommend;
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 用户对象 micro_user
 *
 * @date 2022-10-11
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroUserEditEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户昵称
     */
    private String nickName;
    private String userName;

    /**
     * 用户性别（0男 1女 2未知）
     */
    private String sex;
    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 账号状态(0正常 1停用)
     * 状态不允许通过编辑接口修改（updateMicroUser 不更新该字段），屏蔽直接绑定
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String status;

    private String remark;

    private String roleCode;

    /**
     * 最后修改人
     */
    private String lastUpdBy;
}

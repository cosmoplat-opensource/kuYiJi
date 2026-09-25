/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 注册码与客户关联对象 portal_user_register_relation
 * 
 * @author cosmo-hhim-open Team
 * @date 2022-07-08
 */
@Data
public class PortalUserRegisterRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 注册ID */
    private Long registerId;

    /** 注册码 */
    private String registerCode;

    /** 有效期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone ="GMT+8")
    private Date dueTime;

    /** 客户编码 */
    private String customerCode;

    /** 数据库 */
    private String databasename;

    /** 数据源 */
    private String datasource;

    /** 状态（0：启用，1：停用） */
    private String disable;

    /** 在用标志（1-在用；0-停用） */
    private String activeFlag;

    /** 操作人姓名 */
    private String createByName;

}

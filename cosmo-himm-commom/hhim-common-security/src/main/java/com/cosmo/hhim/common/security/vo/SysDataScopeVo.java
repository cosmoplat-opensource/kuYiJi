/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.vo;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.security.pojo.WmsBaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户数据权限对象 sys_data_scope
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-04-01
 */
@ApiModel(description = "用户数据权限对象 sys_data_scope")
public class SysDataScopeVo extends WmsBaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户id */
    @Excel(name = "用户id")
    @ApiModelProperty(value = "用户id",name = "userId")
    private Long userId;

    /** 数据权限业务id */
    @Excel(name = "数据权限业务id")
    @ApiModelProperty(value = "数据权限业务id",name = "scopeId")
    private String scopeId;

    /** 权限类型 */
    @Excel(name = "权限类型")
    @ApiModelProperty(value = "权限类型",name = "scopeType")
    private String scopeType;

    /** 数据权限业务id+'_'+权限类型 */
    private String key;

    /** 权限label */
    private String label;

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public void setScopeType(String scopeType)
    {
        this.scopeType = scopeType;
    }

    public String getScopeType() 
    {
        return scopeType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("scopeId", getScopeId())
            .append("scopeType", getScopeType())
            .append("createId", getCreateId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateId", getUpdateId())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .append("key", getKey())
            .append("label", getLabel())
            .toString();
    }
}

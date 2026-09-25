/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.third;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 【请填写功能名称】对象 hyzz_third_interface_method_config
 * 
 * @author cosmo-hhim-open Team
 * @date 2022-07-18
 */
public class ThirdInterfaceMethodConfig implements Serializable
{

    /** $column.columnComment */
    private Long id;

    /** 方法名 */

    private String method;

    /** 方法版本 */
    private String version;

    /** 租户编码 */
    private String tenantCode;

    /** 租户名称 */
    private String tenantName;

    /** 是否有序(0无序,1有序) */
    private String orderly;

    /** 是否启用状态(0停用,1启用) */
    private String status;

    /** 激活标记(0逻辑删除 1正常状态) */
    private String activeFlag;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private Date createdDate;

    /** 更新人 */
    private String lastUpdBy;

    /** 更新时间 */
    private Date lastUpdDate;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setMethod(String method) 
    {
        this.method = method;
    }

    public String getMethod() 
    {
        return method;
    }
    public void setVersion(String version) 
    {
        this.version = version;
    }

    public String getVersion() 
    {
        return version;
    }
    public void setTenantCode(String tenantCode) 
    {
        this.tenantCode = tenantCode;
    }

    public String getTenantCode() 
    {
        return tenantCode;
    }
    public void setTenantName(String tenantName) 
    {
        this.tenantName = tenantName;
    }

    public String getTenantName() 
    {
        return tenantName;
    }
    public void setOrderly(String orderly) 
    {
        this.orderly = orderly;
    }

    public String getOrderly() 
    {
        return orderly;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setActiveFlag(String activeFlag) 
    {
        this.activeFlag = activeFlag;
    }

    public String getActiveFlag() 
    {
        return activeFlag;
    }
    public void setCreatedBy(String createdBy) 
    {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() 
    {
        return createdBy;
    }
    public void setCreatedDate(Date createdDate) 
    {
        this.createdDate = createdDate;
    }

    public Date getCreatedDate() 
    {
        return createdDate;
    }
    public void setLastUpdBy(String lastUpdBy) 
    {
        this.lastUpdBy = lastUpdBy;
    }

    public String getLastUpdBy() 
    {
        return lastUpdBy;
    }
    public void setLastUpdDate(Date lastUpdDate) 
    {
        this.lastUpdDate = lastUpdDate;
    }

    public Date getLastUpdDate() 
    {
        return lastUpdDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("method", getMethod())
            .append("version", getVersion())
            .append("tenantCode", getTenantCode())
            .append("tenantName", getTenantName())
            .append("orderly", getOrderly())
            .append("status", getStatus())
            .append("activeFlag", getActiveFlag())
            .append("createdBy", getCreatedBy())
            .append("createdDate", getCreatedDate())
            .append("lastUpdBy", getLastUpdBy())
            .append("lastUpdDate", getLastUpdDate())
            .toString();
    }
}

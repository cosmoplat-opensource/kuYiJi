/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.pojo;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * cim数据权限对象 portal_cim_data_authority
 * 
 * @author cosmo-hhim-open Team
 * @date 2021-07-14
 */
@ApiModel(value="cim数据权限对象 portal_cim_data_authority",description="cim数据权限对象 portal_cim_data_authority")
public class PortalCimDataAuthority extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @ApiModelProperty(value = "$column.columnComment", name = "rowId", example = "")
    private Long rowId;

    /** 用户登录名 */
    @Excel(name = "用户登录名")
    @ApiModelProperty(value = "用户登录名", name = "userName", example = "")
    private String userName;

    /** 权限类型（工厂--F，车间-W，产线--L） */
    @Excel(name = "权限类型", readConverterExp = "工=厂--F，车间-W，产线--L")
    @ApiModelProperty(value = "权限类型（工厂--F，车间-W，产线--L）", name = "dataType", example = "")
    private String dataType;

    /** 编码 */
    @Excel(name = "编码")
    @ApiModelProperty(value = "编码", name = "dataCode", example = "")
    private String dataCode;

    /** 名称 */
    @Excel(name = "名称")
    @ApiModelProperty(value = "名称", name = "dataName", example = "")
    private String dataName;

    public void setRowId(Long rowId) 
    {
        this.rowId = rowId;
    }

    public Long getRowId() 
    {
        return rowId;
    }
    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }
    public void setDataType(String dataType) 
    {
        this.dataType = dataType;
    }

    public String getDataType() 
    {
        return dataType;
    }
    public void setDataCode(String dataCode) 
    {
        this.dataCode = dataCode;
    }

    public String getDataCode() 
    {
        return dataCode;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("rowId", getRowId())
            .append("userName", getUserName())
            .append("dataType", getDataType())
            .append("dataCode", getDataCode())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("dataName", getDataName())
            .toString();
    }
}

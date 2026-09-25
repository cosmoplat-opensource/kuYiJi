/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.pojo;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.security.utils.SecurityUtils;

/**
 * 数据权限对象 portal_data_authority
 *
 * @author cosmo-hhim-open Team
 * @date 2021-07-28
 */
public class SysDataAuthority extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long rowId;

    /** 用户主键 */
    @Excel(name = "用户主键")
    private Long userId;

    /** 用户登录名 */
    @Excel(name = "用户登录名")
    private String userName;

    /** 权限类型 生产（工厂--F，车间-W，产线--L）
        wms（AREA -库区，FAC-工厂，WH-仓库）
    */
    @Excel(name = "权限类型 生产", readConverterExp = "工=厂--F，车间-W，产线--L")
    private String dataType;

    /** 编码 */
    @Excel(name = "编码")
    private String dataCode;

    /** 名称 */
    @Excel(name = "名称")
    private String dataName;

    /** 应用类型 */
    @Excel(name = "应用类型")
    private String appType;

    private Long createId;
    private Boolean delFlag;

    public void setRowId(Long rowId)
    {
        this.rowId = rowId;
    }

    public Long getRowId()
    {
        return rowId;
    }
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
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

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public Long getCreateId() {
        return createId;
    }

    public void setCreateId(Long createId) {
        this.createId = createId;
    }

    public Boolean getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Boolean delFlag) {
        this.delFlag = delFlag;
    }

    public void setCreateInfo() {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getCreateBy())) {
                this.setCreateBy(username);
            }

            if (this.createId == null) {
                this.setCreateId(SecurityUtils.getUserId());
            }

            if (this.delFlag == null) {
                this.setDelFlag(false);
            }
        }

        this.setCreateTime(DateUtils.getNowDate());
        this.setDelFlag(false);
    }

    @Override
    public String toString() {
        return "PortalDataAuthority{" +
                "rowId=" + rowId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", dataType='" + dataType + '\'' +
                ", dataCode='" + dataCode + '\'' +
                ", dataName='" + dataName + '\'' +
                ", appType='" + appType + '\'' +
                '}';
    }
}

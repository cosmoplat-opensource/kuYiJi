/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 体验数据管理对象 hyzz_data_manage
 * 
 * @author cosmo-hhim-open Team
 * @date 2022-03-23
 */
public class HyzzDataManage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long rowId;

    /** 行业大类（数据字典data_trade_class） */
    private String tradeCode;

    private String tradeName;

    /** 制造类型（数据字典data_make） */
    private String makeCode;

    private String makeName;

    /** 体验企业模板编码 */
    private String customerCode;

    /** 体验企业模板名称 */
    private String customerName;

    /** 同步人 */
    private String syncBy;

    /** 同步时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date syncTime;

    /** 脚本地址 */
    private String scriptUrl;

    /** 在用标志（1在用；0停用） */
    private String activeFlag;

    /**
     * 状态（0：生成中；1：成功；2：失败）
     */
    private Integer status;

    public void setRowId(Long rowId) 
    {
        this.rowId = rowId;
    }

    public Long getRowId() 
    {
        return rowId;
    }
    public void setTradeCode(String tradeCode) 
    {
        this.tradeCode = tradeCode;
    }

    public String getTradeCode() 
    {
        return tradeCode;
    }
    public void setMakeCode(String makeCode) 
    {
        this.makeCode = makeCode;
    }

    public String getMakeCode() 
    {
        return makeCode;
    }
    public void setCustomerCode(String customerCode) 
    {
        this.customerCode = customerCode;
    }

    public String getCustomerCode() 
    {
        return customerCode;
    }
    public void setCustomerName(String customerName) 
    {
        this.customerName = customerName;
    }

    public String getCustomerName() 
    {
        return customerName;
    }
    public void setSyncBy(String syncBy) 
    {
        this.syncBy = syncBy;
    }

    public String getSyncBy() 
    {
        return syncBy;
    }
    public void setSyncTime(Date syncTime) 
    {
        this.syncTime = syncTime;
    }

    public Date getSyncTime() 
    {
        return syncTime;
    }
    public void setScriptUrl(String scriptUrl) 
    {
        this.scriptUrl = scriptUrl;
    }

    public String getScriptUrl() 
    {
        return scriptUrl;
    }
    public void setActiveFlag(String activeFlag) 
    {
        this.activeFlag = activeFlag;
    }

    public String getActiveFlag() 
    {
        return activeFlag;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getMakeName() {
        return makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("rowId", getRowId())
            .append("tradeCode", getTradeCode())
            .append("makeCode", getMakeCode())
            .append("customerCode", getCustomerCode())
            .append("customerName", getCustomerName())
            .append("syncBy", getSyncBy())
            .append("syncTime", getSyncTime())
            .append("scriptUrl", getScriptUrl())
            .append("remark", getRemark())
            .append("activeFlag", getActiveFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

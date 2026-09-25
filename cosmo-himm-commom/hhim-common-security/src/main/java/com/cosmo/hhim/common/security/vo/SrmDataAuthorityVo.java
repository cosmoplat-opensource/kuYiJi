/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.vo;

import java.io.Serializable;

/**
 * srm 数据权限vo
 *
 * @author cosmo-hhim-open Team
 */
public class SrmDataAuthorityVo implements Serializable {

    /** 权限类型 : srm(SUPPLIER-供应商) 目前只有一个 */
    private String dataType;

    /** 权限编码 */
    private String dataCode;

    /** 权限名称 */
    private String dataName;

    /**
     * 企业简称
     */
    private String shortName;

    /**
     * 企业地址
     */
    private String address;

    /** 应用类型 */
    private String appType;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataCode() {
        return dataCode;
    }

    public void setDataCode(String dataCode) {
        this.dataCode = dataCode;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    @Override
    public String toString() {
        return "SrmDataAuthorityVo{" +
                "dataType='" + dataType + '\'' +
                ", dataCode='" + dataCode + '\'' +
                ", dataName='" + dataName + '\'' +
                ", shortName ='" + shortName + '\'' +
                ", address ='" + address + '\'' +
                ", appType='" + appType + '\'' +
                '}';
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.vo;

import java.io.Serializable;

/**
 * @description: itpm 数据权限vo
 * @classname: ItpmDataAuthorityVo
 * @date: 2021/12/1 18:09
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
public class ItpmDataAuthorityVo implements Serializable {

    private static final long serialVersionUID = 1822937779031424827L;

    /** 权限类型 : itpm(DEVICE-设备) 目前只有一个 */
    private String dataType;

    /** 权限编码 */
    private String dataCode;

    /** 权限名称 */
    private String dataName;

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

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }
}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.vo;

import java.io.Serializable;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public class PortalFactoryTreeVO implements Serializable {

    /**
     * 类型 工厂--F，车间-W，产线--L，工位--WS，设备--D
     */
    private String dataType;

    /**
     * 编码
     */
    private String dataCode;

    /**
     * 名称
     */
    private String dataName;

    /**
     * 子节点
     */
    private List<PortalFactoryTreeVO> childList;

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

    public List<PortalFactoryTreeVO> getChildList() {
        return childList;
    }

    public void setChildList(List<PortalFactoryTreeVO> childList) {
        this.childList = childList;
    }
}

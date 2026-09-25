/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.vo;


import com.cosmo.hhim.common.security.pojo.PortalCimDataAuthority;

import java.io.Serializable;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
public class CimDataAuthorityVO implements Serializable {

    /**
     * CIM数据权限列表
     */
    private List<PortalCimDataAuthority> authorityList;

    public List<PortalCimDataAuthority> getAuthorityList() {
        return authorityList;
    }

    public void setAuthorityList(List<PortalCimDataAuthority> authorityList) {
        this.authorityList = authorityList;
    }

    @Override
    public String toString() {
        return "CimDataAuthorityVO{" +
                "authorityList=" + authorityList +
                '}';
    }
}

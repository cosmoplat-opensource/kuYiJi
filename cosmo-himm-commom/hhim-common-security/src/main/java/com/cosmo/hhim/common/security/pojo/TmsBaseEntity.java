/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.pojo;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.security.utils.SecurityUtils;

/**
 * @author cosmo-hhim-open Team
 */
public class TmsBaseEntity extends BaseEntity {

    /**
     * $column.columnComment
     */
    private Long id;
    private String activeFlag;//'激活标记'


    public String getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(String activeFlag) {
        this.activeFlag = activeFlag;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void setCreateInfo() {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getCreateBy())) {
                this.setCreateBy(username);
            }
            if (this.activeFlag == null) {
                this.setActiveFlag("1");
            }
        }
        this.setCreateTime(DateUtils.getNowDate());
    }

    public void setWechatMpCreateInfo(String username) {
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getCreateBy())) {
                this.setCreateBy(username);
            }
            if (this.activeFlag == null) {
                this.setActiveFlag("1");
            }
        }
        this.setCreateTime(DateUtils.getNowDate());
    }

    public void setUpdateInfo() {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getUpdateBy())) {
                this.setUpdateBy(username);
            }
        }
        this.setUpdateTime(DateUtils.getNowDate());
    }

    public void setWechatMpUpdateInfo(String username) {
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getUpdateBy())) {
                this.setUpdateBy(username);
            }
        }
        this.setUpdateTime(DateUtils.getNowDate());
    }
}

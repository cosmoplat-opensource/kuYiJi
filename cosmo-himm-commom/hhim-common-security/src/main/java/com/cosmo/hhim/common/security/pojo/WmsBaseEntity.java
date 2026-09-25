/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.pojo;


import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.security.utils.SecurityUtils;


public class WmsBaseEntity extends BaseEntity {

    /**
     * $column.columnComment
     */
    private Long id;
    /**
     * 创建人id
     */
    private Long createId;

    /**
     * 最后更新人id
     */
    private Long updateId;

    /**
     * 删除标识
     */
    private Boolean delFlag;

    /**
     * 物料编码或名称
     */
    private String matCodeOrName;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateId() {
        return createId;
    }

    public void setCreateId(Long createId) {
        this.createId = createId;
    }

    public Long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Long updateId) {
        this.updateId = updateId;
    }

    public Boolean getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Boolean delFlag) {
        this.delFlag = delFlag;
    }

    public String getMatCodeOrName() {
        return matCodeOrName;
    }

    public void setMatCodeOrName(String matCodeOrName) {
        this.matCodeOrName = matCodeOrName;
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
    }

    public void setUpdateInfo() {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            this.setUpdateBy(username);
            if (this.updateId == null) {
                this.setUpdateId(SecurityUtils.getUserId());
            }
        }
        this.setUpdateTime(DateUtils.getNowDate());
    }
}

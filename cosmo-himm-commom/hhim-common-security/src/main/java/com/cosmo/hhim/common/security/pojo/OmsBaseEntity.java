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
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class OmsBaseEntity extends BaseEntity {

    /**
     * $column.columnComment
     */
    private Long id;
    private String activeFlag;//'激活标记'
    private String remarks;//'备注'
    private String lastUpdBy;//'最后修改人'

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;//'最后修改时间'

    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;
    /**
     * 创建人id
     */
    //private Long createId;

    /**
     * 最后更新人id
     */
    //private Long updateId;

    public String getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(String activeFlag) {
        this.activeFlag = activeFlag;
    }

    public String getLastUpdBy() {
        return lastUpdBy;
    }

    public void setLastUpdBy(String lastUpdBy) {
        this.lastUpdBy = lastUpdBy;
    }

    public Date getLastUpdDate() {
        return lastUpdDate;
    }

    public void setLastUpdDate(Date lastUpdDate) {
        this.lastUpdDate = lastUpdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    /* public Long getCreateId() {
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
    }*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 删除标识
     */
    // private Boolean delFlag;

    public void setCreateInfo() {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getCreateBy())) {
                this.setCreateBy(username);
                this.setCreatedBy(username);
            }
            /*if (this.createId == null) {
                this.setCreateId(SecurityUtils.getUserId());
            }*/
            if (this.activeFlag == null) {
                this.setActiveFlag("1");
            }
        }
        this.setCreateTime(DateUtils.getNowDate());
        this.setCreatedDate(DateUtils.getNowDate());
    }

    public void setUpdateInfo() {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username)) {
            if (StringUtils.isBlank(this.getUpdateBy())) {
                this.setUpdateBy(username);
                this.setLastUpdBy(username);
            }
            /*if (this.updateId == null) {
                this.setUpdateId(SecurityUtils.getUserId());
            }*/
        }
        this.setUpdateTime(DateUtils.getNowDate());
        this.setLastUpdDate(DateUtils.getNowDate());
    }
}

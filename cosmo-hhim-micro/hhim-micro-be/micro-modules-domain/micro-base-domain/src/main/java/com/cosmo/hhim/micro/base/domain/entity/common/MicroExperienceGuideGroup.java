/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 体验引导节点组对象 micro_experience_guide_group
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-01
 */
@Data
public class MicroExperienceGuideGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 节点组编码
     */
    private String groupCode;

    /**
     * 节点组名称
     */
    private String groupName;

    /**
     * 节点组状态（0：未完成, 1：已完成）
     */
    private String status;

    /**
     * 可用标识（0：正常，1：停用）
     */
    private String activeFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

    /**
     * 租户编码
     */
    private String tenantCode;

    private String remark;
}

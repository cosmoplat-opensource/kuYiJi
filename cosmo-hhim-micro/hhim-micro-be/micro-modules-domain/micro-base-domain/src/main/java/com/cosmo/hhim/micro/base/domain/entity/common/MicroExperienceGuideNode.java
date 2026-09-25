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
 * 体验引导节点配置对象 micro_experience_guide_node
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-01
 */
@Data
public class MicroExperienceGuideNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 节点编码
     */
    private String nodeCode;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点所属组编码
     */
    private String nodeGroupCode;

    /**
     * 节点排序
     */
    private Long nodeSort;

    /**
     * 跳转页面
     */
    private String skipPage;

    // 操作耗时 
    private String elapsedTime;

    /**
     * 节点状态（0：未完成，1：已完成）
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

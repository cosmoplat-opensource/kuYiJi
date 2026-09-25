/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 通知配置对象 micro_notice_config
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-06
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroNoticeConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 业务标识(10:生产日报，20:生产周报)
     */
    private String businessSign;

    /**
     * 通知角色编码（多个用,分割）
     */
    private String noticeRoles;

    /**
     * 通知用户(多个用,分割)
     */
    private String noticeUsers;

    /**
     * 应用标识(micro_process:KU易记，micro_plan:工易派)
     */
    private String appSign;

    /**
     * 通知渠道(多个用,分割)（10:微信公众号，20:微信小程序，30:SMS）
     */
    private String noticeChannels;

    /**
     * 通知条件配置（格式：key1:value1,key2:value2…）
     */
    private String noticeConditions;

    // 备注 
    private String remark;

    /**
     * 可用标识（0：正常，1：停用）
     */
    private String activeFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;


}

/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 微信消息推送记录对象 micro_wechat_push_record
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
@Data
public class MicroWechatPushRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 推送用户列表(多个用,分割)
     */
    private String toUsers;

    /**
     * 推送标题
     */
    private String title;

    /**
     * 推送内容(json格式)
     */
    private String content;

    /**
     * 推送跳转URL
     */
    private String skipUrl;

    /**
     * 推送使用的微信模版ID
     */
    private String wechatModelId;

    /**
     * 推送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pushTime;

    /**
     * 推送完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pushFinishTime;

    /**
     * 推送状态(0：待推送，1：已推送)
     */
    private String pushStatus;

    /**
     * 推送结果（0：失败，1：成功）
     */
    private String pushResult;

    /**
     * 模版类型（0：小程序，1：公众号）
     */
    private String templateType;

    /**
     * 服务标识（在制品库存微应用:micro）
     */
    private String serviceSign;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

    /**
     * 租户编码
     */
    private String tenantCode;

}

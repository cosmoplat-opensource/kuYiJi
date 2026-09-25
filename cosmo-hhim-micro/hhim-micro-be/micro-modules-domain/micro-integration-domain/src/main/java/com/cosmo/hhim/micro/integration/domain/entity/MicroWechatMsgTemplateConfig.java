/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 微信模版消息配置对象 micro_wechat_msg_template_config
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
@Data
public class MicroWechatMsgTemplateConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 模版类型（10：公众号，20：小程序）
     */
    private String templateType;

    /**
     * 消息类型（10：生产日报，20：生产周报）
     */
    private String messageType;

    /**
     * 消息类型clazz（包全路径）
     */
    private String messageClazz;

    /**
     * 微信模版ID
     */
    private String templateId;

    /**
     * 消息标题
     */
    private String messageTitle;

    /**
     * 点击跳转链接
     */
    private String skipUrl;

    /**
     * 服务标识（micro_process：KU易记，micro_plan：工易派）
     */
    private String serviceSign;

    /**
     * 可用标识（0：正常，1：停用）
     */
    private String activeFlag;

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
    @Excel(name = "更新者")
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

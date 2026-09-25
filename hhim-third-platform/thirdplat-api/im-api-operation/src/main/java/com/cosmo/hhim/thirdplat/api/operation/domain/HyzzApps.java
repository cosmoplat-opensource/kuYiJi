/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * 应用信息对象 hyzz_apps
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzApps extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 应用ID */
    private Long appsId;

    /** 应用编码 */
    @NotBlank(message = "应用编码不能为空")
    private String appsCode;

    /** 应用名称 */
    @NotBlank(message = "应用名称不能为空")
    private String appsName;

    /** 应用类别(自研/外购) */
    @NotBlank(message = "应用类别不能为空")
    private String appsCategory;

    /** 应用类型（预留） */
    private String appsType;

    /** 应用概述 */
    private String appsDesc;

    /** APP链接地址 */
    private String appUrl;

    /** APP应用图标 */
    private String appIcon;

    /** APP广告图（弃用-存放于附件表中） */
    private String appAdvert;

    /** PC链接地址 */
    private String pcUrl;

    /** PC应用图标 */
    private String pcIcon;

    /** PC广告图（弃用-存放于附件表中） */
    private String pcAdvert;

    /** 详细说明 */
    private String appsDetail;

    /** 应用状态(1-上架、0-下架) */
    private String appsStatus;

    /** 排序 */
    private Integer applySort;

    /** 有效期（月） */
    private Integer validMonth;

    /** 上线时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date issueTime;

    /** 上线人 */
    private String issueBy;

    /** 下线时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date downTime;

    /** 下线人 */
    private String downBy;

    /** 是否开放申请 */
    private String isOpen;

    /** 是否需要付款 */
    private String isPay;

    /** 是否需申请审核 */
    private String isCheck;

    /**
     * app广告图
     */
    private List<HyzzFiles> appAdList;

    /**
     * pc广告图
     */
    private List<HyzzFiles> pcAdList;

    /**
     * 验证重复
     */
    private Long validAppId;

    /**
     * 是否必须需要
     */
    private String isMust;
}

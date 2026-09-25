/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户行为事件埋点对象 micro_user_event_tracking
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-23
 */
@Data
public class MicroUserEventTrackingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 事件中文名称
     */
    private String eventName;

    /**
     * 事件类型,TP(time on page)页面停留时长事件,EP(expose)曝光PV事件,CLICK点击事件
     */
    private String eventType;

    /**
     * 事件内容
     */
    private String eventContent;

    /**
     * 针对页面停留时长、曝光事件类埋点-停留时长秒,针对点击事件是次数
     */
    private BigDecimal duration;
    /**
     * 权重
     */
    private BigDecimal weight;

    /**
     * 用户体验或正式EXPERIENCE/OFFICIAL
     */
    private String userType;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 事件来源PAGE/VIDEO/IMAGE
     */
    private String eventSource;

    /**
     * 用户手机号
     */
    private Long phonenumber;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 用户位置
     */
    private String userLocation;
    /**
     * UA
     */
    private String userAgent;

    /**
     * 当前页面
     */
    private String currentPage;

    /**
     * 上一级页面
     */
    private String referPage;

    /**
     * 20230101
     */
    private Long createdDay;

    /**
     * 202301
     */
    private Long createdMonth;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 应用标识
     */
    private String applicationSign;
}

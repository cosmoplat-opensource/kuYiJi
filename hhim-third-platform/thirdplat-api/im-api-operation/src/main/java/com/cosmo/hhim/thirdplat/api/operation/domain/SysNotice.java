/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

import java.util.Date;

/**
 * 通知公告表 sys_notice
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class SysNotice extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     */
    private String noticeType;

    /**
     * 公告内容
     */
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     */
    private String status;

    /**
     * 公告开始时间
     */
    private Date noticeStartDate;

    /**
     * 公告结束时间
     */
    private Date noticeEndDate;

    /**
     * 公告发送渠道（0-平台内部；1-企业门户；2-渠道商）-预留，默认企业门户
     */
    private String noticePlace;

    /**
     * 下发状态（1-下发中；0-未下发；2-已下发）
     */
    private String isIssued;

}

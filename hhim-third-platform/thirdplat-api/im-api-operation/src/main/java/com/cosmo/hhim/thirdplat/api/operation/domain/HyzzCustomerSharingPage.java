/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 客户页面分享对象 hyzz_customer_sharing_page
 * 
 * @author cosmo-hhim-open Team
 * @date 2022-06-23
 */
@Data
public class HyzzCustomerSharingPage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 客户编码 */
    private String customerCode;

    /** 状态(10：可分享，20：不可分享) */
    private String status;

    /** 标识（UUID） */
    private String markId;

    /** 类型 */
    private String type;

    /** 类型名称 */
    private String typeName;

    /** 页面内容 */
    private String pageContent;

    /** 提取码 */
    private String extractedCode;

    /** 截至日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dueDate;

    /** 分享路径 */
    private String url;

    /** 在用标志（1在用；0停用） */
    private String activeFlag;

    /** 创建者昵称 */
    private String createByName;

    /**
     * 业务单号
     */
    private String orderNo;

    /**
     * 校验类型(0:无，1：提取码)
     */
    private String validType;

    private Integer pageNum;
    private Integer pageSize;
    private String orderByColumn;
    private String isAsc = "asc";
}

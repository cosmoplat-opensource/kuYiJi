/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

/**
 * 意见反馈日志对象 hyzz_portal_suggestion_log
 * 
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzPortalSuggestionLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 编码 */
    private String orderNo;

    /** 操作名称(创建，处理，完成) */
    private String operateName;

    /** 操作结果 */
    private String operateResult;

    /** 在用标志（1在用；0停用） */
    private String activeFlag;

    /** 创建者昵称 */
    private String createByName;


}

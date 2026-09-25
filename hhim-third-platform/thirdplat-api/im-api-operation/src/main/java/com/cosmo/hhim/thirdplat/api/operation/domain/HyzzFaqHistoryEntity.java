/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 门户的意见反馈对象 hyzz_portal_suggestion
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzFaqHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 问题内容
     */
    private String question;
    /**
     * 回答
     */
    private String answer;
    /**
     * 引用ID
     */
    private Long refId;
    /**
     * 排序时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sortDate;
}

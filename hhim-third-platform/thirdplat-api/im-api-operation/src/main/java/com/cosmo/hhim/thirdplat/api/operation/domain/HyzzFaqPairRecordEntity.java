/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 智能客服问答对对象 hyzz_faq_pair_record
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
@Data
public class HyzzFaqPairRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 问题
     */
    private String question;

    /**
     * 回答
     */
    private String answer;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 权重
     */
    private BigDecimal weight;

    /**
     * 回答类型,文本TEXT,链接LINK,图片PICTURE,视频VIDEO
     */
    private String answerType;


    /**
     * 客户产品类型(0:MOM,1:微应用)
     */
    private Long customerSign;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;
    /**
     * 索引列表
     */
    private List<String> indexList;

    private Double similarity;

}
